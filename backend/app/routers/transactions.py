from fastapi import APIRouter, Depends, HTTPException, status, Query
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from typing import Optional

from app.database import get_db
from app.models import User, Transaction, TransactionType, TransactionStatus
from app.schemas import (
    FuelPurchaseRequest, TransactionResponse, TransactionHistory, BaseResponse
)
from app.services.auth import get_current_user
from app.services.payment import (
    process_fuel_purchase, get_transaction_history, refund_transaction
)

router = APIRouter()
security = HTTPBearer()

@router.post("/fuel-purchase", response_model=BaseResponse)
async def purchase_fuel(
    purchase_data: FuelPurchaseRequest,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Process fuel purchase using QR code scan."""
    
    # This endpoint is typically called by pump operators, but we'll verify the request
    # In production, you might want additional validation for pump operators
    
    result = process_fuel_purchase(
        db=db,
        qr_code=purchase_data.qr_code,
        pump_id=purchase_data.pump_id,
        fuel_type=purchase_data.fuel_type,
        fuel_quantity=purchase_data.fuel_quantity,
        fuel_rate=purchase_data.fuel_rate
    )
    
    if not result["success"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=result["message"]
        )
    
    return BaseResponse(
        success=True,
        message=result["message"],
        data=result
    )

@router.get("/history", response_model=TransactionHistory)
async def get_user_transaction_history(
    page: int = Query(1, ge=1),
    page_size: int = Query(10, ge=1, le=50),
    transaction_type: Optional[TransactionType] = None,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get paginated transaction history for the current user."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Build query
    query = db.query(Transaction).filter(Transaction.user_id == user.id)
    
    if transaction_type:
        query = query.filter(Transaction.transaction_type == transaction_type)
    
    # Get total count
    total_count = query.count()
    
    # Get paginated results
    offset = (page - 1) * page_size
    transactions = query.order_by(Transaction.created_at.desc()).offset(offset).limit(page_size).all()
    
    total_pages = (total_count + page_size - 1) // page_size
    
    return TransactionHistory(
        transactions=transactions,
        total_count=total_count,
        page=page,
        page_size=page_size,
        total_pages=total_pages
    )

@router.get("/{transaction_id}", response_model=TransactionResponse)
async def get_transaction_details(
    transaction_id: str,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get details of a specific transaction."""
    
    user = get_current_user(db, credentials.credentials)
    
    transaction = db.query(Transaction).filter(
        Transaction.transaction_id == transaction_id,
        Transaction.user_id == user.id
    ).first()
    
    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found"
        )
    
    return transaction

@router.post("/{transaction_id}/refund", response_model=BaseResponse)
async def request_refund(
    transaction_id: str,
    reason: str = "User requested refund",
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Request refund for a transaction (Admin/Pump owner functionality)."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Verify transaction belongs to user or user has admin rights
    transaction = db.query(Transaction).filter(
        Transaction.transaction_id == transaction_id
    ).first()
    
    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found"
        )
    
    # Only allow refunds for the transaction owner or admins
    if transaction.user_id != user.id and user.role.value != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to refund this transaction"
        )
    
    result = refund_transaction(db, transaction_id, reason)
    
    if not result["success"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=result["message"]
        )
    
    return BaseResponse(
        success=True,
        message=result["message"],
        data={"refunded_amount": result["refunded_amount"]}
    )

@router.get("/pump/{pump_id}/history")
async def get_pump_transaction_history(
    pump_id: int,
    page: int = Query(1, ge=1),
    page_size: int = Query(10, ge=1, le=50),
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get transaction history for a specific pump (Pump owner/operator only)."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Verify user is associated with the pump or is admin
    from app.models import PumpOperator, PetrolPump
    
    # Check if user is pump operator/owner or admin
    is_authorized = False
    
    if user.role.value == "admin":
        is_authorized = True
    elif user.role.value in ["pump_operator", "pump_owner"]:
        # Check if user is associated with this pump
        pump_association = db.query(PumpOperator).filter(
            PumpOperator.user_id == user.id,
            PumpOperator.pump_id == pump_id,
            PumpOperator.is_active == True
        ).first()
        
        if pump_association:
            is_authorized = True
        
        # Also check if user is the pump owner
        pump = db.query(PetrolPump).filter(PetrolPump.id == pump_id).first()
        if pump and user.role.value == "pump_owner":
            # Additional check can be added here for pump ownership
            is_authorized = True
    
    if not is_authorized:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to view this pump's transactions"
        )
    
    # Get transactions for the pump
    offset = (page - 1) * page_size
    
    transactions = db.query(Transaction).filter(
        Transaction.pump_id == pump_id
    ).order_by(Transaction.created_at.desc()).offset(offset).limit(page_size).all()
    
    total_count = db.query(Transaction).filter(Transaction.pump_id == pump_id).count()
    total_pages = (total_count + page_size - 1) // page_size
    
    return {
        "transactions": transactions,
        "total_count": total_count,
        "page": page,
        "page_size": page_size,
        "total_pages": total_pages
    }

@router.get("/stats/summary")
async def get_transaction_stats(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get transaction statistics for the current user."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Get various statistics
    from sqlalchemy import func
    from datetime import datetime, timedelta
    
    # Total transactions
    total_transactions = db.query(Transaction).filter(Transaction.user_id == user.id).count()
    
    # Total spent
    total_spent = db.query(func.sum(Transaction.amount)).filter(
        Transaction.user_id == user.id,
        Transaction.transaction_type == TransactionType.FUEL_PURCHASE,
        Transaction.status == TransactionStatus.COMPLETED
    ).scalar() or 0.0
    
    # This month's spending
    current_month_start = datetime.utcnow().replace(day=1, hour=0, minute=0, second=0, microsecond=0)
    monthly_spent = db.query(func.sum(Transaction.amount)).filter(
        Transaction.user_id == user.id,
        Transaction.transaction_type == TransactionType.FUEL_PURCHASE,
        Transaction.status == TransactionStatus.COMPLETED,
        Transaction.completed_at >= current_month_start
    ).scalar() or 0.0
    
    # This week's transactions
    week_start = datetime.utcnow() - timedelta(days=7)
    weekly_transactions = db.query(Transaction).filter(
        Transaction.user_id == user.id,
        Transaction.created_at >= week_start
    ).count()
    
    # Most frequent fuel type
    fuel_type_stats = db.query(
        Transaction.fuel_type,
        func.count(Transaction.fuel_type).label('count')
    ).filter(
        Transaction.user_id == user.id,
        Transaction.fuel_type.isnot(None),
        Transaction.status == TransactionStatus.COMPLETED
    ).group_by(Transaction.fuel_type).order_by(func.count(Transaction.fuel_type).desc()).first()
    
    most_used_fuel = fuel_type_stats.fuel_type if fuel_type_stats else None
    
    return {
        "total_transactions": total_transactions,
        "total_spent": total_spent,
        "monthly_spent": monthly_spent,
        "weekly_transactions": weekly_transactions,
        "most_used_fuel": most_used_fuel
    } 