import logging

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.schemas import QRCodeGenerate, QRCodeResponse, QRCodeValidateRequest, BaseResponse
from app.services.auth import get_current_user
from app.services.qr_service import (
    deactivate_qr_code,
    generate_user_qr_code,
    get_qr_code_image,
    get_user_qr_codes,
    validate_qr_code,
)

logger = logging.getLogger("zappay.routers.qr")

router = APIRouter()
security = HTTPBearer()

@router.get("/my-codes", response_model=List[QRCodeResponse])
async def get_my_qr_codes(
    active_only: bool = True,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get all QR codes for the current user."""
    
    user = get_current_user(db, credentials.credentials)
    qr_codes = get_user_qr_codes(db, user.id, active_only)
    
    return qr_codes

@router.post("/generate", response_model=QRCodeResponse)
async def generate_qr_code(
    qr_data: QRCodeGenerate,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Generate a new QR code for the current user."""
    
    user = get_current_user(db, credentials.credentials)
    
    qr_code = generate_user_qr_code(
        db=db,
        user_id=user.id,
        qr_type=qr_data.qr_type,
        validity_hours=qr_data.validity_hours,
        vehicle_id=qr_data.vehicle_id
    )
    
    # Ensure expires_at is ISO formatted with Z for UTC
    response_data = QRCodeResponse.from_orm(qr_code)
    if qr_code.expires_at:
        # Force UTC timezone awareness if naive
        if qr_code.expires_at.tzinfo is None:
             from datetime import timezone
             response_data.expires_at = qr_code.expires_at.replace(tzinfo=timezone.utc)
        else:
             response_data.expires_at = qr_code.expires_at
    
    logger.info("Generated QR for User %d. Expires: %s", user.id, response_data.expires_at)
    return response_data

@router.get("/{qr_code_id}/image")
async def get_qr_code_image_endpoint(
    qr_code_id: int,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get QR code image as base64 string."""
    
    user = get_current_user(db, credentials.credentials)
    
    base64_image = get_qr_code_image(qr_code_id, user.id, db)
    
    if not base64_image:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="QR code image not found"
        )
    
    return {
        "qr_code_id": qr_code_id,
        "image_data": base64_image,
        "format": "base64"
    }

@router.post("/{qr_code_id}/deactivate", response_model=BaseResponse)
async def deactivate_qr_code_endpoint(
    qr_code_id: int,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Deactivate a specific QR code."""
    
    user = get_current_user(db, credentials.credentials)
    
    success = deactivate_qr_code(db, qr_code_id, user.id)
    
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="QR code not found"
        )
    
    return BaseResponse(
        success=True,
        message="QR code deactivated successfully"
    )

@router.get("/validate/{qr_data}")
async def quick_validate_qr_code(
    qr_data: str,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Quick validate QR code with minimal data (for pump terminal displays)."""
    
    validation_result = validate_qr_code(db, qr_data)
    
    if not validation_result:
        return {
            "valid": False,
            "message": "Invalid or expired QR code"
        }
    
    user, qr_record = validation_result
    
    return {
        "valid": True,
        "user_name": user.full_name,
        "wallet_balance": user.wallet.balance if user.wallet else 0.0,
        "vehicle_number": user.vehicle_number
    } 