from fastapi import APIRouter, Depends, HTTPException, status, Query
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime

from app.database import get_db
from app.schemas import UserProfile, UserUpdate, KYCSubmission, BaseResponse, UserDashboard, AutoRechargeSettings, UserVehicleCreate, UserVehicleResponse, UserVehicleUpdate, SupportTicketCreate, SupportTicketResponse
from app.services.auth import get_current_user
from app.services.payment import get_wallet_summary
from app.models import User, KYCStatus, UserVehicle, SupportTicket, NotificationEvent

router = APIRouter()
security = HTTPBearer()

@router.get("/profile", response_model=UserProfile)
async def get_user_profile(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get current user's profile."""

    user = get_current_user(db, credentials.credentials)
    return user

@router.put("/profile", response_model=UserProfile)
async def update_user_profile(
    profile_data: UserUpdate,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Update current user's profile."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Update only provided fields
    for field, value in profile_data.dict(exclude_unset=True).items():
        setattr(user, field, value)
    
    db.commit()
    db.refresh(user)
    
    return user

@router.post("/kyc/submit", response_model=BaseResponse)
async def submit_kyc(
    kyc_data: KYCSubmission,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Submit KYC documents for verification."""
    
    user = get_current_user(db, credentials.credentials)
    
    if user.kyc_status == KYCStatus.VERIFIED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="KYC already verified"
        )
    
    # Update KYC information
    user.aadhaar_number = kyc_data.aadhaar_number
    user.pan_number = kyc_data.pan_number
    user.driving_license = kyc_data.driving_license
    user.kyc_status = KYCStatus.PENDING
    
    db.commit()
    
    return BaseResponse(
        success=True,
        message="KYC documents submitted successfully. Verification is in progress."
    )

@router.get("/dashboard", response_model=UserDashboard)
async def get_user_dashboard(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get user dashboard with wallet summary and recent transactions."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Get wallet summary
    wallet_summary = get_wallet_summary(db, user.id)
    
    # Get QR codes
    from app.services.qr_service import get_user_qr_codes
    qr_codes = get_user_qr_codes(db, user.id, active_only=True)
    
    return UserDashboard(
        wallet_balance=wallet_summary["balance"],
        total_transactions=len(wallet_summary["recent_transactions"]),
        total_spent=wallet_summary["total_spent"],
        recent_transactions=wallet_summary["recent_transactions"],
        qr_codes=qr_codes
    )

@router.post("/deactivate", response_model=BaseResponse)
async def deactivate_account(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Deactivate user account."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Check if user has pending transactions or positive balance
    if user.wallet and user.wallet.balance > 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Cannot deactivate account with positive wallet balance. Please withdraw funds first."
        )
    
    user.is_active = False
    db.commit()
    
    return BaseResponse(
        success=True,
        message="Account deactivated successfully"
    )

@router.get("/kyc/status")
async def get_kyc_status(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get current KYC status."""
    
    user = get_current_user(db, credentials.credentials)
    
    return {
        "kyc_status": user.kyc_status,
        "aadhaar_number": user.aadhaar_number,
        "pan_number": user.pan_number,
        "driving_license": user.driving_license
    } 

@router.get("/auto-recharge-settings", response_model=AutoRechargeSettings)
async def get_auto_recharge_settings(
	credentials: HTTPAuthorizationCredentials = Depends(security),
	db: Session = Depends(get_db)
):
	user = get_current_user(db, credentials.credentials)
	return AutoRechargeSettings(
		enabled=user.auto_recharge_enabled,
		threshold=user.auto_recharge_threshold,
		amount=user.auto_recharge_amount,
		payment_method=user.auto_recharge_payment_method or "razorpay"
	)

@router.put("/auto-recharge-settings", response_model=BaseResponse)
async def update_auto_recharge_settings(
	settings_payload: AutoRechargeSettings,
	credentials: HTTPAuthorizationCredentials = Depends(security),
	db: Session = Depends(get_db)
):
	user = get_current_user(db, credentials.credentials)
	user.auto_recharge_enabled = settings_payload.enabled
	user.auto_recharge_threshold = settings_payload.threshold
	user.auto_recharge_amount = settings_payload.amount
	user.auto_recharge_payment_method = settings_payload.payment_method
	db.commit()
	return BaseResponse(success=True, message="Auto-recharge settings updated")

# ── Multi-Vehicle ──

@router.get("/vehicles", response_model=List[UserVehicleResponse])
async def get_my_vehicles(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    user = get_current_user(db, credentials.credentials)
    vehicles = db.query(UserVehicle).filter(UserVehicle.user_id == user.id, UserVehicle.is_active == True).all()
    return vehicles

@router.post("/vehicles", response_model=BaseResponse)
async def add_vehicle(
    vehicle_data: UserVehicleCreate,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    user = get_current_user(db, credentials.credentials)
    if vehicle_data.is_primary:
        db.query(UserVehicle).filter(UserVehicle.user_id == user.id).update({"is_primary": False})
    vehicle = UserVehicle(user_id=user.id, **vehicle_data.dict())
    db.add(vehicle)
    db.commit()
    return BaseResponse(success=True, message="Vehicle added")

@router.put("/vehicles/{vehicle_id}", response_model=BaseResponse)
async def update_vehicle(
    vehicle_id: int,
    vehicle_data: UserVehicleUpdate,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    user = get_current_user(db, credentials.credentials)
    vehicle = db.query(UserVehicle).filter(UserVehicle.id == vehicle_id, UserVehicle.user_id == user.id).first()
    if not vehicle:
        raise HTTPException(status_code=404, detail="Vehicle not found")
    if vehicle_data.is_primary:
        db.query(UserVehicle).filter(UserVehicle.user_id == user.id).update({"is_primary": False})
    for field, value in vehicle_data.dict(exclude_unset=True).items():
        setattr(vehicle, field, value)
    db.commit()
    return BaseResponse(success=True, message="Vehicle updated")

@router.delete("/vehicles/{vehicle_id}", response_model=BaseResponse)
async def remove_vehicle(
    vehicle_id: int,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    user = get_current_user(db, credentials.credentials)
    vehicle = db.query(UserVehicle).filter(UserVehicle.id == vehicle_id, UserVehicle.user_id == user.id).first()
    if not vehicle:
        raise HTTPException(status_code=404, detail="Vehicle not found")
    vehicle.is_active = False
    db.commit()
    return BaseResponse(success=True, message="Vehicle removed")

# ── Support Tickets (customer-facing) ──

@router.post("/support-tickets", response_model=BaseResponse)
async def create_support_ticket(
    ticket_data: SupportTicketCreate,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    user = get_current_user(db, credentials.credentials)
    ticket = SupportTicket(user_id=user.id, **ticket_data.dict())
    db.add(ticket)
    db.commit()
    return BaseResponse(success=True, message="Support ticket created")

@router.get("/support-tickets")
async def get_my_tickets(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    user = get_current_user(db, credentials.credentials)
    tickets = db.query(SupportTicket).filter(SupportTicket.user_id == user.id).order_by(SupportTicket.created_at.desc()).all()
    return {"tickets": tickets}

# ── Notifications ──

@router.get("/notifications")
async def get_my_notifications(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    user = get_current_user(db, credentials.credentials)
    notifs = db.query(NotificationEvent).filter(NotificationEvent.user_id == user.id).order_by(NotificationEvent.created_at.desc()).limit(50).all()
    return {"notifications": notifs}

@router.post("/notifications/{notif_id}/read", response_model=BaseResponse)
async def mark_notification_read(
    notif_id: int,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    user = get_current_user(db, credentials.credentials)
    notif = db.query(NotificationEvent).filter(NotificationEvent.id == notif_id, NotificationEvent.user_id == user.id).first()
    if notif:
        notif.is_read = True
        notif.read_at = datetime.utcnow()
        db.commit()
    return BaseResponse(success=True, message="Notification marked as read") 