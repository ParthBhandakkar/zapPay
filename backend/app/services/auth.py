import asyncio
import logging
import random
import string
from datetime import datetime, timedelta
from typing import Optional

from jose import JWTError, jwt
from passlib.context import CryptContext
from sqlalchemy.orm import Session

from app.config import settings
from app.models import OTP, User
from app.schemas import TokenData

logger = logging.getLogger("zappay.services.auth")

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)


def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=settings.access_token_expire_minutes))
    to_encode.update({
        "exp": expire,
        "iat": datetime.utcnow(),
        "iss": settings.jwt_issuer,
        "type": "access",
    })
    return jwt.encode(to_encode, settings.secret_key, algorithm=settings.algorithm)


def create_refresh_token(data: dict) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(days=settings.refresh_token_expire_days)
    to_encode.update({
        "exp": expire,
        "iat": datetime.utcnow(),
        "iss": settings.jwt_issuer,
        "type": "refresh",
    })
    return jwt.encode(to_encode, settings.secret_key, algorithm=settings.algorithm)


def verify_token(token: str, credentials_exception) -> TokenData:
    try:
        payload = jwt.decode(
            token,
            settings.secret_key,
            algorithms=[settings.algorithm],
            options={"verify_aud": False},
        )
        user_id_str = payload.get("sub")
        phone_number: str = payload.get("phone_number")
        token_type = payload.get("type", "access")

        if user_id_str is None:
            raise credentials_exception

        return TokenData(user_id=int(user_id_str), phone_number=phone_number)
    except (JWTError, ValueError, AssertionError) as e:
        logger.warning("Token verification failed: %s", e)
        raise credentials_exception


def verify_refresh_token(token: str, credentials_exception) -> TokenData:
    try:
        payload = jwt.decode(
            token,
            settings.secret_key,
            algorithms=[settings.algorithm],
            options={"verify_aud": False},
        )
        if payload.get("type") != "refresh":
            raise credentials_exception
        user_id_str = payload.get("sub")
        if user_id_str is None:
            raise credentials_exception
        return TokenData(user_id=int(user_id_str), phone_number=payload.get("phone_number"))
    except (JWTError, ValueError) as e:
        logger.warning("Refresh token verification failed: %s", e)
        raise credentials_exception


def authenticate_user(db: Session, phone_number: str, password: str) -> Optional[User]:
    clean_phone = phone_number.replace(" ", "").replace("-", "")
    user = db.query(User).filter(User.phone_number == clean_phone).first()
    if not user or not verify_password(password, user.password_hash):
        return None
    return user


def generate_otp() -> str:
    return "".join(random.choices(string.digits, k=6))


def create_otp(db: Session, phone_number: str, otp_type: str = "login") -> str:
    db.query(OTP).filter(
        OTP.phone_number == phone_number,
        OTP.otp_type == otp_type,
        OTP.is_used == False,
    ).delete()

    otp_code = generate_otp()
    expires_at = datetime.utcnow() + timedelta(minutes=10)
    otp_record = OTP(phone_number=phone_number, otp_code=otp_code, otp_type=otp_type, expires_at=expires_at)
    db.add(otp_record)
    db.commit()
    return otp_code


def verify_otp(db: Session, phone_number: str, otp_code: str, otp_type: str = "login") -> bool:
    otp_record = db.query(OTP).filter(
        OTP.phone_number == phone_number,
        OTP.otp_code == otp_code,
        OTP.otp_type == otp_type,
        OTP.is_used == False,
        OTP.expires_at > datetime.utcnow(),
    ).first()
    if not otp_record:
        return False
    otp_record.is_used = True
    otp_record.used_at = datetime.utcnow()
    db.commit()
    return True


def send_otp_sms(phone_number: str, otp_code: str) -> bool:
    from app.services.notifications import send_otp_sms as async_send
    try:
        loop = asyncio.get_event_loop()
        if loop.is_running():
            asyncio.create_task(async_send(phone_number, otp_code))
            return True
        return loop.run_until_complete(async_send(phone_number, otp_code))
    except RuntimeError:
        return asyncio.run(async_send(phone_number, otp_code))


def get_current_user(db: Session, token: str) -> User:
    from fastapi import HTTPException, status
    exc = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    token_data = verify_token(token, exc)
    user = db.query(User).filter(User.id == token_data.user_id).first()
    if not user:
        raise exc
    return user
