from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas import UserProfile, UserUpdate, KYCSubmission, BaseResponse, UserDashboard, AutoRechargeSettings
from app.services.auth import get_current_user
from app.services.payment import get_wallet_summary
from app.models import User, KYCStatus, UserVehicle, Vehicle, NotificationEvent, SupportTicket

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


@router.get("/vehicles")
async def get_my_vehicles(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    user = get_current_user(db, credentials.credentials)
    uvs = db.query(UserVehicle).filter(
        UserVehicle.user_id == user.id,
        UserVehicle.is_active == True,
    ).order_by(UserVehicle.is_primary.desc(), UserVehicle.id.asc()).all()

    if not uvs:
        v2_list = db.query(Vehicle).filter(Vehicle.owner_user_id == user.id).all()
        for idx, v2 in enumerate(v2_list):
            db.add(UserVehicle(
                user_id=user.id,
                vehicle_number=v2.vrn_number,
                vehicle_type="Car",
                nickname=v2.make_model,
                is_primary=(idx == 0),
                is_active=True,
            ))
        if not v2_list and user.vehicle_number:
            db.add(UserVehicle(
                user_id=user.id,
                vehicle_number=user.vehicle_number,
                vehicle_type=user.vehicle_type or "Car",
                nickname="Primary Vehicle",
                is_primary=True,
                is_active=True,
            ))
        db.commit()
        uvs = db.query(UserVehicle).filter(
            UserVehicle.user_id == user.id,
            UserVehicle.is_active == True,
        ).order_by(UserVehicle.is_primary.desc(), UserVehicle.id.asc()).all()

    return [
        {
            "id": v.id,
            "user_id": v.user_id,
            "vehicle_number": v.vehicle_number,
            "vehicle_type": v.vehicle_type or "Car",
            "nickname": v.nickname,
            "is_primary": bool(v.is_primary),
            "is_active": bool(v.is_active),
        }
        for v in uvs
    ]


@router.post("/vehicles", response_model=BaseResponse)
async def add_user_vehicle(
    payload: dict,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    user = get_current_user(db, credentials.credentials)
    vnum = (payload.get("vehicle_number") or "").strip().upper()
    if not vnum:
        raise HTTPException(status_code=400, detail="vehicle_number required")
    is_primary = bool(payload.get("is_primary", False))
    if is_primary:
        db.query(UserVehicle).filter(UserVehicle.user_id == user.id).update({"is_primary": False})
    db.add(UserVehicle(
        user_id=user.id,
        vehicle_number=vnum,
        vehicle_type=payload.get("vehicle_type") or "Car",
        nickname=payload.get("nickname"),
        is_primary=is_primary,
        is_active=True,
    ))
    db.commit()
    return BaseResponse(success=True, message="Vehicle added successfully")


@router.put("/vehicles/{vehicle_id}", response_model=BaseResponse)
async def update_user_vehicle(
    vehicle_id: int,
    payload: dict,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    user = get_current_user(db, credentials.credentials)
    uv = db.query(UserVehicle).filter(UserVehicle.id == vehicle_id, UserVehicle.user_id == user.id).first()
    if not uv:
        raise HTTPException(status_code=404, detail="Vehicle not found")
    if payload.get("is_primary"):
        db.query(UserVehicle).filter(UserVehicle.user_id == user.id).update({"is_primary": False})
    for k in ("vehicle_number", "vehicle_type", "nickname", "is_primary"):
        if k in payload and payload[k] is not None:
            setattr(uv, k, payload[k])
    db.commit()
    return BaseResponse(success=True, message="Vehicle updated successfully")


@router.delete("/vehicles/{vehicle_id}", response_model=BaseResponse)
async def delete_user_vehicle(
    vehicle_id: int,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    user = get_current_user(db, credentials.credentials)
    uv = db.query(UserVehicle).filter(UserVehicle.id == vehicle_id, UserVehicle.user_id == user.id).first()
    if not uv:
        raise HTTPException(status_code=404, detail="Vehicle not found")
    uv.is_active = False
    db.commit()
    return BaseResponse(success=True, message="Vehicle removed successfully")


@router.get("/notifications")
async def get_my_notifications(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    user = get_current_user(db, credentials.credentials)
    notifs = db.query(NotificationEvent).filter(NotificationEvent.user_id == user.id).order_by(NotificationEvent.created_at.desc()).limit(50).all()
    return {
        "notifications": [
            {
                "id": n.id,
                "user_id": n.user_id,
                "notification_type": n.notification_type or "info",
                "title": n.title or "ZapPay Notification",
                "body": n.body,
                "is_read": bool(n.is_read),
                "is_sent": bool(n.is_sent),
                "created_at": n.created_at.isoformat() if n.created_at else None,
            }
            for n in notifs
        ]
    }


@router.post("/notifications/{notif_id}/read", response_model=BaseResponse)
async def mark_notification_read(
    notif_id: int,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    user = get_current_user(db, credentials.credentials)
    n = db.query(NotificationEvent).filter(NotificationEvent.id == notif_id, NotificationEvent.user_id == user.id).first()
    if n:
        n.is_read = True
        db.commit()
    return BaseResponse(success=True, message="Notification marked as read")


@router.get("/support-tickets")
async def get_support_tickets(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    user = get_current_user(db, credentials.credentials)
    tickets = db.query(SupportTicket).filter(SupportTicket.user_id == user.id).order_by(SupportTicket.created_at.desc()).all()
    return {
        "tickets": [
            {
                "id": t.id,
                "user_id": t.user_id,
                "subject": t.subject,
                "description": t.description,
                "category": t.category,
                "priority": t.priority.value if hasattr(t.priority, "value") else str(t.priority),
                "status": t.status.value if hasattr(t.status, "value") else str(t.status),
                "resolution": t.resolution,
                "created_at": t.created_at.isoformat() if t.created_at else None,
                "updated_at": t.updated_at.isoformat() if t.updated_at else None,
            }
            for t in tickets
        ],
        "total_count": len(tickets),
        "page": 1,
        "page_size": 20,
    }


@router.post("/support-tickets", response_model=BaseResponse)
async def create_support_ticket(
    payload: dict,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    user = get_current_user(db, credentials.credentials)
    t = SupportTicket(
        user_id=user.id,
        subject=payload.get("subject") or "Support Request",
        description=payload.get("description"),
        category=payload.get("category") or "other",
    )
    db.add(t)
    db.commit()
    return BaseResponse(success=True, message="Support ticket submitted")
