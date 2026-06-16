from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy.orm import Session
from datetime import timedelta

from app.database import get_db
from app.schemas import UserCreate, UserLogin, Token, OTPRequest, OTPVerify, RegisterOTPStart, RegisterOTPComplete
from app.services.auth import (
	authenticate_user, get_password_hash, create_access_token, create_refresh_token,
	create_otp, verify_otp
)
from app.services.notifications import send_otp_sms, send_email
from app.models import User, UserRole
from app.config import settings
from app.services.payment import get_or_create_wallet

router = APIRouter()

@router.post("/register")
async def register_user(payload: UserCreate, db: Session = Depends(get_db)):
	# Normalize phone number (remove spaces, dashes)
	clean_phone = payload.phone_number.replace(" ", "").replace("-", "")
	
	# Check phone
	existing_phone = db.query(User).filter(User.phone_number == clean_phone).first()
	if existing_phone:
		raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Phone number already registered")
	
	# Check email
	if payload.email:
		existing_email = db.query(User).filter(User.email == payload.email).first()
		if existing_email:
			raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Email already registered")

	user = User(
		phone_number=clean_phone,
		email=payload.email,
		full_name=payload.full_name,
		password_hash=get_password_hash(payload.password),
		role=payload.role,
		is_active=True,
		is_verified=False
	)
	db.add(user)
	db.commit()
	db.refresh(user)
	# Auto-create wallet on registration
	get_or_create_wallet(db, user.id)
	return {"success": True, "message": "Registration successful", "data": {"user_id": user.id}}

@router.post("/login", response_model=Token)
async def login(payload: UserLogin, db: Session = Depends(get_db)):
	phone = payload.phone_number or payload.username
	user = authenticate_user(db, phone, payload.password)
	if not user:
		raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
	expires = timedelta(minutes=settings.access_token_expire_minutes)
	access = create_access_token({"sub": str(user.id), "phone_number": user.phone_number}, expires)
	refresh = create_refresh_token({"sub": str(user.id), "phone_number": user.phone_number})
	return Token(access_token=access, refresh_token=refresh, token_type="bearer", expires_in=int(expires.total_seconds()))

@router.post("/send-otp")
async def send_otp_endpoint(payload: OTPRequest, db: Session = Depends(get_db)):
	# If this is a login OTP, check if user exists first
	if payload.otp_type == "login":
		user = db.query(User).filter(User.phone_number == payload.phone_number).first()
		if not user:
			raise HTTPException(
				status_code=status.HTTP_404_NOT_FOUND, 
				detail="User not registered. Please register first."
			)

	otp_code = create_otp(db, payload.phone_number, payload.otp_type)
	await send_otp_sms(payload.phone_number, otp_code)
	
	# Send to email if user exists
	user = db.query(User).filter(User.phone_number == payload.phone_number).first()
	if user and user.email:
		await send_email(
			user.email, 
			"ZapPay Login OTP", 
			f"Your OTP for ZapPay login is: {otp_code}. Valid for 10 minutes."
		)

	resp = {"success": True, "message": "OTP sent"}
	if settings.debug:
		resp["otp_debug"] = otp_code
	return resp

@router.post("/login/otp", response_model=Token)
async def login_with_otp(payload: OTPVerify, db: Session = Depends(get_db)):
	if not verify_otp(db, payload.phone_number, payload.otp_code, payload.otp_type):
		raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired OTP")
	user = db.query(User).filter(User.phone_number == payload.phone_number).first()
	if not user:
		raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
	expires = timedelta(minutes=settings.access_token_expire_minutes)
	access = create_access_token({"sub": str(user.id), "phone_number": user.phone_number}, expires)
	refresh = create_refresh_token({"sub": str(user.id), "phone_number": user.phone_number})
	return Token(access_token=access, refresh_token=refresh, token_type="bearer", expires_in=int(expires.total_seconds()))

@router.post("/register/otp/start")
async def register_otp_start(payload: RegisterOTPStart, db: Session = Depends(get_db)):
	# If user already exists, block or allow login via OTP
	existing = db.query(User).filter(User.phone_number == payload.phone_number).first()
	if existing:
		raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="User already exists")
	# Create OTP for registration
	otp_code = create_otp(db, payload.phone_number, otp_type="registration")
	await send_otp_sms(payload.phone_number, otp_code)
	
	if payload.email:
		await send_email(
			payload.email,
			"ZapPay Registration OTP",
			f"Your OTP for ZapPay registration is: {otp_code}. Valid for 10 minutes."
		)

	resp = {"success": True, "message": "OTP sent for registration"}
	if settings.debug:
		resp["otp_debug"] = otp_code
	return resp

@router.post("/register/otp/complete", response_model=Token)
async def register_otp_complete(payload: RegisterOTPComplete, db: Session = Depends(get_db)):
	# Verify OTP
	if not verify_otp(db, payload.phone_number, payload.otp_code, otp_type="registration"):
		raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired OTP")
	# Check again if user exists
	existing = db.query(User).filter(User.phone_number == payload.phone_number).first()
	if existing:
		raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="User already exists")
	# Create user
	password_hash = get_password_hash(payload.password) if payload.password else get_password_hash("changeme123")
	user = User(
		phone_number=payload.phone_number,
		email=payload.email,
		full_name=payload.full_name,
		password_hash=password_hash,
		role=payload.role,
		is_active=True,
		is_verified=False
	)
	db.add(user)
	db.commit()
	db.refresh(user)
	# Create wallet for customers
	try:
		get_or_create_wallet(db, user.id)
	except Exception:
		pass
	# Return tokens
	expires = timedelta(minutes=settings.access_token_expire_minutes)
	access = create_access_token({"sub": str(user.id), "phone_number": user.phone_number}, expires)
	refresh = create_refresh_token({"sub": str(user.id), "phone_number": user.phone_number})
	return Token(access_token=access, refresh_token=refresh, token_type="bearer", expires_in=int(expires.total_seconds()))

@router.post("/refresh", response_model=Token)
async def refresh_token(refresh_token: str, db: Session = Depends(get_db)):
	# Minimal refresh verification; in production validate "type":"refresh"
	from jose import jwt
	from jose.exceptions import JWTError
	try:
		payload = jwt.decode(refresh_token, settings.secret_key, algorithms=[settings.algorithm])
		user_id = payload.get("sub")
		if not user_id:
			raise HTTPException(status_code=401, detail="Invalid token")
		user = db.query(User).filter(User.id == int(user_id)).first()
		if not user:
			raise HTTPException(status_code=401, detail="Invalid token")
		expires = timedelta(minutes=settings.access_token_expire_minutes)
		access = create_access_token({"sub": str(user.id), "phone_number": user.phone_number}, expires)
		new_refresh = create_refresh_token({"sub": str(user.id), "phone_number": user.phone_number})
		return Token(access_token=access, refresh_token=new_refresh, token_type="bearer", expires_in=int(expires.total_seconds()))
	except JWTError:
		raise HTTPException(status_code=401, detail="Invalid token") 