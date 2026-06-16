import json
import logging

from fastapi import APIRouter, Depends, HTTPException, Query, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session
from typing import List, Optional

from app.database import get_db
from app.schemas import (
    BaseResponse,
    PetrolPumpCreate,
    PetrolPumpResponse,
    PetrolPumpUpdate,
    PumpDashboard,
)
from app.services.auth import get_current_user
from app.models import PetrolPump, PumpOperator, Settlement, Transaction, TransactionStatus, User, UserRole

logger = logging.getLogger("zappay.routers.pumps")

router = APIRouter()
security = HTTPBearer()

@router.post("/register", response_model=BaseResponse)
async def register_pump(
    pump_data: PetrolPumpCreate,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Register a new petrol pump."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Check if user has permission to register pumps (pump_owner or admin)
    if user.role not in [UserRole.PUMP_OWNER, UserRole.ADMIN]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only pump owners or admins can register pumps"
        )
    
    # Check if license number already exists
    existing_pump = db.query(PetrolPump).filter(
        PetrolPump.license_number == pump_data.license_number
    ).first()
    
    if existing_pump:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Pump with this license number already exists"
        )
    
    # Create new pump
    pump = PetrolPump(
        pump_name=pump_data.pump_name,
        owner_name=pump_data.owner_name,
        license_number=pump_data.license_number,
        address=pump_data.address,
        city=pump_data.city,
        state=pump_data.state,
        pincode=pump_data.pincode,
        phone_number=pump_data.phone_number,
        email=pump_data.email,
        fuel_types=json.dumps(pump_data.fuel_types),
        daily_fuel_capacity=pump_data.daily_fuel_capacity,
        latitude=pump_data.latitude,
        longitude=pump_data.longitude
    )
    
    db.add(pump)
    db.commit()
    db.refresh(pump)
    
    # Automatically add the creator as a Pump Operator (Owner)
    pump_operator = PumpOperator(
        user_id=user.id,
        pump_id=pump.id,
        employee_id="OWNER",
        is_active=True
    )
    db.add(pump_operator)
    db.commit()
    
    return BaseResponse(
        success=True,
        message="Petrol pump registered successfully",
        data={"pump_id": pump.id}
    )

@router.get("", response_model=List[PetrolPumpResponse])
@router.get("/", response_model=List[PetrolPumpResponse])
async def get_pumps(
    city: Optional[str] = None,
    state: Optional[str] = None,
    verified_only: bool = True,
    page: int = Query(1, ge=1),
    page_size: int = Query(10, ge=1, le=50),
    db: Session = Depends(get_db)
):
    """Get list of petrol pumps with optional filtering."""
    
    query = db.query(PetrolPump).filter(PetrolPump.is_active == True)
    
    if verified_only:
        query = query.filter(PetrolPump.is_verified == True)
    
    if city:
        query = query.filter(PetrolPump.city.ilike(f"%{city}%"))
    
    if state:
        query = query.filter(PetrolPump.state.ilike(f"%{state}%"))
    
    # Pagination
    offset = (page - 1) * page_size
    pumps = query.offset(offset).limit(page_size).all()
    
    return pumps

@router.get("/my-pump", response_model=PetrolPumpResponse)
async def get_my_pump(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get the pump associated with the current user (owner/operator)."""
    user = get_current_user(db, credentials.credentials)
    
    # Find pump association
    association = db.query(PumpOperator).filter(
        PumpOperator.user_id == user.id,
        PumpOperator.is_active == True
    ).first()
    
    if not association:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No pump associated with this user"
        )
        
    pump = db.query(PetrolPump).filter(PetrolPump.id == association.pump_id).first()
    if not pump:
        raise HTTPException(status_code=404, detail="Pump not found")
        
    return pump

@router.get("/nearby")
async def get_nearby_pumps(
    latitude: float,
    longitude: float,
    radius_km: float = 10.0,
    db: Session = Depends(get_db)
):
    """Get nearby pumps based on coordinates."""
    
    # Simple distance calculation (for production, use PostGIS or similar)
    # This is a basic implementation
    
    pumps = db.query(PetrolPump).filter(
        PetrolPump.is_active == True,
        PetrolPump.is_verified == True,
        PetrolPump.latitude.isnot(None),
        PetrolPump.longitude.isnot(None)
    ).all()
    
    nearby_pumps = []
    
    for pump in pumps:
        # Simple distance calculation (approximate)
        lat_diff = abs(pump.latitude - latitude)
        lon_diff = abs(pump.longitude - longitude)
        
        # Rough distance calculation (1 degree ≈ 111 km)
        distance = ((lat_diff ** 2 + lon_diff ** 2) ** 0.5) * 111
        
        if distance <= radius_km:
            pump_data = {
                "id": pump.id,
                "pump_name": pump.pump_name,
                "address": pump.address,
                "city": pump.city,
                "latitude": pump.latitude,
                "longitude": pump.longitude,
                "distance_km": round(distance, 2),
                "fuel_types": json.loads(pump.fuel_types) if pump.fuel_types else []
            }
            nearby_pumps.append(pump_data)
    
    # Sort by distance
    nearby_pumps.sort(key=lambda x: x["distance_km"])
    
    return {"nearby_pumps": nearby_pumps[:20]}  # Return top 20 nearest pumps 

@router.get("/{pump_id}", response_model=PetrolPumpResponse)
async def get_pump_details(
    pump_id: int,
    db: Session = Depends(get_db)
):
    """Get details of a specific pump."""
    
    pump = db.query(PetrolPump).filter(
        PetrolPump.id == pump_id,
        PetrolPump.is_active == True
    ).first()
    
    if not pump:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Pump not found"
        )
    
    return pump

@router.put("/{pump_id}", response_model=PetrolPumpResponse)
async def update_pump(
    pump_id: int,
    pump_data: PetrolPumpUpdate,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Update pump details."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Check authorization (pump owner, operator, or admin)
    if user.role not in [UserRole.PUMP_OWNER, UserRole.ADMIN]:
        # Check if user is associated with this pump as operator
        pump_association = db.query(PumpOperator).filter(
            PumpOperator.user_id == user.id,
            PumpOperator.pump_id == pump_id,
            PumpOperator.is_active == True
        ).first()
        
        if not pump_association:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to update this pump"
            )
    
    pump = db.query(PetrolPump).filter(PetrolPump.id == pump_id).first()
    
    if not pump:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Pump not found"
        )
    
    # Update only provided fields
    update_data = pump_data.dict(exclude_unset=True)
    
    # Handle fuel_types conversion
    if "fuel_types" in update_data:
        update_data["fuel_types"] = json.dumps(update_data["fuel_types"])
    
    for field, value in update_data.items():
        setattr(pump, field, value)
    
    db.commit()
    db.refresh(pump)
    
    return pump

@router.post("/save", response_model=BaseResponse)
async def save_pump_settings(
    payload: dict,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Save pump settings (price, name, etc)."""
    user = get_current_user(db, credentials.credentials)
    pump_id = payload.get("pump_id")
    
    # Check authorization
    if user.role not in [UserRole.PUMP_OWNER, UserRole.ADMIN]:
        pump_association = db.query(PumpOperator).filter(
            PumpOperator.user_id == user.id,
            PumpOperator.pump_id == pump_id,
            PumpOperator.is_active == True
        ).first()
        if not pump_association:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not authorized")

    pump = db.query(PetrolPump).filter(PetrolPump.id == pump_id).first()
    if not pump:
        raise HTTPException(status_code=404, detail="Pump not found")

    if "pump_name" in payload:
        pump.pump_name = payload["pump_name"]
    
    # Handle price updates (simplified for now, assuming petrol price update)
    # In a real app, you'd parse fuel_types JSON and update specific prices
    
    db.commit()
    return BaseResponse(success=True, message="Settings saved")

@router.get("/{pump_id}/dashboard", response_model=PumpDashboard)
async def get_pump_dashboard(
    pump_id: int,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Get pump dashboard with statistics."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Check authorization
    if user.role not in [UserRole.PUMP_OWNER, UserRole.ADMIN]:
        pump_association = db.query(PumpOperator).filter(
            PumpOperator.user_id == user.id,
            PumpOperator.pump_id == pump_id,
            PumpOperator.is_active == True
        ).first()
        
        if not pump_association:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to view this pump's dashboard"
            )
    
    # Get statistics
    from sqlalchemy import func
    from datetime import datetime, timedelta
    
    # Total transactions
    total_transactions = db.query(Transaction).filter(
        Transaction.pump_id == pump_id,
        Transaction.status == TransactionStatus.COMPLETED
    ).count()
    
    # Total revenue
    total_revenue = db.query(func.sum(Transaction.amount)).filter(
        Transaction.pump_id == pump_id,
        Transaction.status == TransactionStatus.COMPLETED
    ).scalar() or 0.0
    
    # Total commission
    total_commission = db.query(func.sum(Transaction.commission_amount)).filter(
        Transaction.pump_id == pump_id,
        Transaction.status == TransactionStatus.COMPLETED
    ).scalar() or 0.0
    
    # Today's statistics
    today_start = datetime.utcnow().replace(hour=0, minute=0, second=0, microsecond=0)
    
    transactions_today = db.query(Transaction).filter(
        Transaction.pump_id == pump_id,
        Transaction.status == TransactionStatus.COMPLETED,
        Transaction.completed_at >= today_start
    ).count()
    
    revenue_today = db.query(func.sum(Transaction.amount)).filter(
        Transaction.pump_id == pump_id,
        Transaction.status == TransactionStatus.COMPLETED,
        Transaction.completed_at >= today_start
    ).scalar() or 0.0
    
    # Recent transactions
    recent_transactions = db.query(Transaction).filter(
        Transaction.pump_id == pump_id,
        Transaction.status == TransactionStatus.COMPLETED
    ).order_by(Transaction.completed_at.desc()).limit(5).all()
    
    return PumpDashboard(
        total_transactions=total_transactions,
        total_revenue=total_revenue,
        total_commission=total_commission,
        transactions_today=transactions_today,
        revenue_today=revenue_today,
        recent_transactions=recent_transactions
    )

@router.post("/{pump_id}/operators", response_model=BaseResponse)
async def add_pump_operator(
    pump_id: int,
    operator_phone: str,
    employee_id: str = None,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
):
    """Add a pump operator."""
    
    user = get_current_user(db, credentials.credentials)
    
    # Check authorization (pump owner or admin)
    if user.role not in [UserRole.PUMP_OWNER, UserRole.ADMIN]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only pump owners or admins can add operators"
        )
    
    # Find operator user
    operator_user = db.query(User).filter(User.phone_number == operator_phone).first()
    
    if not operator_user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found with provided phone number"
        )
    
    # Check if already an operator
    existing_association = db.query(PumpOperator).filter(
        PumpOperator.user_id == operator_user.id,
        PumpOperator.pump_id == pump_id,
        PumpOperator.is_active == True
    ).first()
    
    if existing_association:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="User is already an operator for this pump"
        )
    
    # Create pump operator association
    pump_operator = PumpOperator(
        user_id=operator_user.id,
        pump_id=pump_id,
        employee_id=employee_id
    )
    
    # Update user role if needed
    if operator_user.role == UserRole.CUSTOMER:
        operator_user.role = UserRole.PUMP_OPERATOR
    
    db.add(pump_operator)
    db.commit()
    
    return BaseResponse(
        success=True,
        message="Pump operator added successfully"
    )

@router.get("/{pump_id}/settlements")
async def get_pump_settlements(
	pump_id: int,
	page: int = Query(1, ge=1),
	page_size: int = Query(10, ge=1, le=50),
	credentials: HTTPAuthorizationCredentials = Depends(security),
	db: Session = Depends(get_db)
):
	user = get_current_user(db, credentials.credentials)
	# Only owner/operator/admin
	is_authorized = False
	if user.role in [UserRole.ADMIN]:
		is_authorized = True
	elif user.role in [UserRole.PUMP_OWNER, UserRole.PUMP_OPERATOR]:
		assoc = db.query(PumpOperator).filter(PumpOperator.user_id == user.id, PumpOperator.pump_id == pump_id, PumpOperator.is_active == True).first()
		if assoc or user.role == UserRole.PUMP_OWNER:
			is_authorized = True
	if not is_authorized:
		raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not authorized")
	query = db.query(Settlement).filter(Settlement.pump_id == pump_id)
	total = query.count()
	settlements = query.order_by(Settlement.settlement_date.desc()).offset((page-1)*page_size).limit(page_size).all()
	return {"settlements": settlements, "total_count": total, "page": page, "page_size": page_size, "total_pages": (total + page_size - 1)//page_size} 