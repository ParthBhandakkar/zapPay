from fastapi import APIRouter, Depends, HTTPException, status, Query
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime, timedelta

from app.database import get_db
from app.schemas import (
    UserProfile, PetrolPumpResponse, BaseResponse, AdminDashboard,
    TransactionResponse, SettlementResponse
)
from app.services.auth import get_current_user
from app.models import (
    User, UserRole, PetrolPump, Transaction, Settlement, KYCStatus,
    TransactionStatus, TransactionType
)
from app.services.payment import calculate_pump_settlement

router = APIRouter()
security = HTTPBearer()

def verify_admin_access(user: User):
    """Verify user has admin access."""
    if user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Admin access required"
        )

@router.get("/dashboard", response_model=AdminDashboard)
async def get_admin_dashboard(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get admin dashboard with system-wide statistics."""
    
    user = get_current_user(db, credentials.credentials)
    verify_admin_access(user)
    
    from sqlalchemy import func
    
    # System-wide statistics
    total_users = db.query(User).count()
    total_pumps = db.query(PetrolPump).count()
    total_transactions = db.query(Transaction).count()
    
    # Total revenue
    total_revenue = db.query(func.sum(Transaction.amount)).filter(
        Transaction.status == TransactionStatus.COMPLETED
    ).scalar() or 0.0
    
    # Today's statistics
    today_start = datetime.utcnow().replace(hour=0, minute=0, second=0, microsecond=0)
    
    active_users_today = db.query(User).filter(
        User.last_login >= today_start
    ).count()
    
    transactions_today = db.query(Transaction).filter(
        Transaction.created_at >= today_start
    ).count()
    
    revenue_today = db.query(func.sum(Transaction.amount)).filter(
        Transaction.status == TransactionStatus.COMPLETED,
        Transaction.completed_at >= today_start
    ).scalar() or 0.0
    
    return AdminDashboard(
        total_users=total_users,
        total_pumps=total_pumps,
        total_transactions=total_transactions,
        total_revenue=total_revenue,
        active_users_today=active_users_today,
        transactions_today=transactions_today,
        revenue_today=revenue_today
    )

@router.get("/users", response_model=List[UserProfile])
async def get_all_users(
    role: Optional[UserRole] = None,
    kyc_status: Optional[KYCStatus] = None,
    is_active: Optional[bool] = None,
    page: int = Query(1, ge=1),
    page_size: int = Query(10, ge=1, le=100),
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get all users with optional filtering."""
    
    user = get_current_user(db, credentials.credentials)
    verify_admin_access(user)
    
    query = db.query(User)
    
    if role:
        query = query.filter(User.role == role)
    
    if kyc_status:
        query = query.filter(User.kyc_status == kyc_status)
    
    if is_active is not None:
        query = query.filter(User.is_active == is_active)
    
    # Pagination
    offset = (page - 1) * page_size
    users = query.offset(offset).limit(page_size).all()
    
    return users

@router.post("/users/{user_id}/verify-kyc", response_model=BaseResponse)
async def verify_user_kyc(
    user_id: int,
    approve: bool,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Approve or reject user KYC."""
    
    admin_user = get_current_user(db, credentials.credentials)
    verify_admin_access(admin_user)
    
    user = db.query(User).filter(User.id == user_id).first()
    
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
    
    if approve:
        user.kyc_status = KYCStatus.VERIFIED
        user.is_verified = True
        message = "KYC approved successfully"
    else:
        user.kyc_status = KYCStatus.REJECTED
        message = "KYC rejected"
    
    db.commit()
    
    return BaseResponse(
        success=True,
        message=message
    )

@router.post("/users/{user_id}/toggle-active", response_model=BaseResponse)
async def toggle_user_active_status(
    user_id: int,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Activate or deactivate a user account."""
    
    admin_user = get_current_user(db, credentials.credentials)
    verify_admin_access(admin_user)
    
    user = db.query(User).filter(User.id == user_id).first()
    
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
    
    user.is_active = not user.is_active
    db.commit()
    
    status_text = "activated" if user.is_active else "deactivated"
    
    return BaseResponse(
        success=True,
        message=f"User {status_text} successfully"
    )

@router.get("/pumps", response_model=List[PetrolPumpResponse])
async def get_all_pumps(
    is_verified: Optional[bool] = None,
    is_active: Optional[bool] = None,
    page: int = Query(1, ge=1),
    page_size: int = Query(10, ge=1, le=100),
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get all pumps with optional filtering."""
    
    user = get_current_user(db, credentials.credentials)
    verify_admin_access(user)
    
    query = db.query(PetrolPump)
    
    if is_verified is not None:
        query = query.filter(PetrolPump.is_verified == is_verified)
    
    if is_active is not None:
        query = query.filter(PetrolPump.is_active == is_active)
    
    # Pagination
    offset = (page - 1) * page_size
    pumps = query.offset(offset).limit(page_size).all()
    
    return pumps

@router.post("/pumps/{pump_id}/verify", response_model=BaseResponse)
async def verify_pump(
    pump_id: int,
    approve: bool,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Approve or reject pump verification."""
    
    user = get_current_user(db, credentials.credentials)
    verify_admin_access(user)
    
    pump = db.query(PetrolPump).filter(PetrolPump.id == pump_id).first()
    
    if not pump:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Pump not found"
        )
    
    pump.is_verified = approve
    db.commit()
    
    status_text = "verified" if approve else "rejected"
    
    return BaseResponse(
        success=True,
        message=f"Pump {status_text} successfully"
    )

@router.get("/transactions")
async def get_all_transactions(
    transaction_type: Optional[TransactionType] = None,
    status: Optional[TransactionStatus] = None,
    pump_id: Optional[int] = None,
    user_id: Optional[int] = None,
    date_from: Optional[datetime] = None,
    date_to: Optional[datetime] = None,
    page: int = Query(1, ge=1),
    page_size: int = Query(10, ge=1, le=100),
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get all transactions with filtering options."""
    
    user = get_current_user(db, credentials.credentials)
    verify_admin_access(user)
    
    query = db.query(Transaction)
    
    if transaction_type:
        query = query.filter(Transaction.transaction_type == transaction_type)
    
    if status:
        query = query.filter(Transaction.status == status)
    
    if pump_id:
        query = query.filter(Transaction.pump_id == pump_id)
    
    if user_id:
        query = query.filter(Transaction.user_id == user_id)
    
    if date_from:
        query = query.filter(Transaction.created_at >= date_from)
    
    if date_to:
        query = query.filter(Transaction.created_at <= date_to)
    
    # Get total count
    total_count = query.count()
    
    # Pagination
    offset = (page - 1) * page_size
    transactions = query.order_by(Transaction.created_at.desc()).offset(offset).limit(page_size).all()
    
    total_pages = (total_count + page_size - 1) // page_size
    
    return {
        "transactions": transactions,
        "total_count": total_count,
        "page": page,
        "page_size": page_size,
        "total_pages": total_pages
    }

@router.post("/settlements/generate")
async def generate_settlements(
    pump_id: Optional[int] = None,
    date_from: datetime = None,
    date_to: datetime = None,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Generate settlements for pumps."""
    
    user = get_current_user(db, credentials.credentials)
    verify_admin_access(user)
    
    # Default to previous day if dates not provided
    if not date_from or not date_to:
        yesterday = datetime.utcnow() - timedelta(days=1)
        date_from = yesterday.replace(hour=0, minute=0, second=0, microsecond=0)
        date_to = yesterday.replace(hour=23, minute=59, second=59, microsecond=999999)
    
    # Get pumps to generate settlements for
    if pump_id:
        pumps = [db.query(PetrolPump).filter(PetrolPump.id == pump_id).first()]
        if not pumps[0]:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Pump not found"
            )
    else:
        pumps = db.query(PetrolPump).filter(
            PetrolPump.is_active == True,
            PetrolPump.is_verified == True
        ).all()
    
    settlements_created = 0
    
    for pump in pumps:
        # Check if settlement already exists for this period
        existing_settlement = db.query(Settlement).filter(
            Settlement.pump_id == pump.id,
            Settlement.from_date == date_from,
            Settlement.to_date == date_to
        ).first()
        
        if existing_settlement:
            continue
        
        # Calculate settlement
        settlement_data = calculate_pump_settlement(db, pump.id, date_from, date_to)
        
        if settlement_data["total_transactions"] > 0:
            # Create settlement record
            settlement = Settlement(
                settlement_id=f"SETT_{pump.id}_{int(date_from.timestamp())}",
                pump_id=pump.id,
                total_transactions=settlement_data["total_transactions"],
                total_amount=settlement_data["total_amount"],
                commission_amount=settlement_data["commission_amount"],
                net_amount=settlement_data["net_amount"],
                from_date=date_from,
                to_date=date_to
            )
            
            db.add(settlement)
            settlements_created += 1
    
    db.commit()
    
    return BaseResponse(
        success=True,
        message=f"Generated {settlements_created} settlements",
        data={"settlements_created": settlements_created}
    )

@router.get("/analytics/revenue")
async def get_revenue_analytics(
    days: int = Query(30, ge=1, le=365),
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get revenue analytics for the specified period."""
    
    user = get_current_user(db, credentials.credentials)
    verify_admin_access(user)
    
    from sqlalchemy import func, Date
    
    # Get daily revenue for the specified period
    end_date = datetime.utcnow()
    start_date = end_date - timedelta(days=days)
    
    daily_revenue = db.query(
        func.date(Transaction.completed_at).label('date'),
        func.sum(Transaction.amount).label('revenue'),
        func.count(Transaction.id).label('transactions')
    ).filter(
        Transaction.status == TransactionStatus.COMPLETED,
        Transaction.completed_at >= start_date,
        Transaction.completed_at <= end_date
    ).group_by(func.date(Transaction.completed_at)).all()
    
    # Format response
    analytics_data = [
        {
            "date": str(record.date),
            "revenue": float(record.revenue),
            "transactions": record.transactions
        }
        for record in daily_revenue
    ]
    
    # Calculate totals
    total_revenue = sum(record["revenue"] for record in analytics_data)
    total_transactions = sum(record["transactions"] for record in analytics_data)
    
    return {
        "period_days": days,
        "total_revenue": total_revenue,
        "total_transactions": total_transactions,
        "average_daily_revenue": total_revenue / days if days > 0 else 0,
        "daily_data": analytics_data
    } 