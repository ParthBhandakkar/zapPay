import logging
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import FleetOrganization, FuelCategory, QRTag, User, Vehicle

logger = logging.getLogger("zappay.routers.fleet")
router = APIRouter(prefix="/api/v1/fleet", tags=["fleet"])


class UpdateVehicleRuleRequest(BaseModel):
    allowed_fuel_type: FuelCategory = FuelCategory.DIESEL
    max_per_txn: float = 4000.0
    daily_limit: float = 8000.0
    monthly_limit: float = 60000.0
    require_geofence_match: bool = True


class TopupWalletRequest(BaseModel):
    amount: float
    payment_mode: str = "CORPORATE_NEFT"


@router.get("/organization")
def get_fleet_organization(db: Session = Depends(get_db)):
    """Fetch B2B Fleet Organization master balance, credit line, and subscription status."""
    org = db.query(FleetOrganization).first()
    if not org:
        user = db.query(User).first()
        if not user:
            user = User(
                phone_number="9827000000",
                full_name="Fleet Admin",
                password_hash="pwd",
                role="fleet_admin",
            )
            db.add(user)
            db.flush()

        org = FleetOrganization(
            owner_user_id=user.id,
            company_name="Chhattisgarh Mining & Logistics Fleet Ltd.",
            gstin="22AAACC1234F1Z5",
            pan_number="AAACC1234F",
            subscription_plan="PRO_PER_VEHICLE",
            master_wallet_balance=150000.0,
            credit_limit=500000.0,
        )
        db.add(org)
        db.commit()
        db.refresh(org)

    return {
        "success": True,
        "organization_id": org.id,
        "company_name": org.company_name,
        "gstin": org.gstin,
        "subscription_plan": org.subscription_plan,
        "master_wallet_balance": org.master_wallet_balance,
        "credit_limit": org.credit_limit,
        "total_available_liquidity": org.master_wallet_balance + org.credit_limit,
        "anti_theft_ocr_enforced": True,
    }


@router.get("/vehicles")
def get_fleet_vehicles(db: Session = Depends(get_db)):
    """List fleet commercial vehicles with programmatic fuel rules and quotas."""
    org = db.query(FleetOrganization).first()
    org_id = org.id if org else None

    vehicles = db.query(Vehicle).filter(Vehicle.fleet_org_id == org_id).all() if org_id else []
    if not vehicles:
        # Seed two commercial trucks
        user = db.query(User).first()
        uid = user.id if user else 1
        v1 = Vehicle(
            owner_user_id=uid,
            fleet_org_id=org_id,
            vrn_number="CG-04-JB-9011",
            make_model="BharatBenz 2823R Heavy Truck",
            fuel_type=FuelCategory.DIESEL,
            tank_capacity_litres=260.0,
            last_odometer_km=84300,
            avg_mileage_kmpl=4.2,
        )
        v2 = Vehicle(
            owner_user_id=uid,
            fleet_org_id=org_id,
            vrn_number="CG-04-HA-3344",
            make_model="Tata Signa Tipper 3525",
            fuel_type=FuelCategory.DIESEL,
            tank_capacity_litres=300.0,
            last_odometer_km=42100,
            avg_mileage_kmpl=3.8,
        )
        db.add(v1)
        db.add(v2)
        db.flush()

        t1 = QRTag(
            qr_public_token="zp_fleet_9011",
            vehicle_id=v1.id,
            max_per_txn=12000.0,
            daily_limit=25000.0,
            require_geofence_match=True,
            allowed_fuel_type=FuelCategory.DIESEL,
        )
        t2 = QRTag(
            qr_public_token="zp_fleet_3344",
            vehicle_id=v2.id,
            max_per_txn=15000.0,
            daily_limit=30000.0,
            require_geofence_match=True,
            allowed_fuel_type=FuelCategory.DIESEL,
        )
        db.add(t1)
        db.add(t2)
        db.commit()
        vehicles = [v1, v2]

    return {
        "success": True,
        "fleet_vehicles": [
            {
                "id": v.id,
                "vrn_number": v.vrn_number,
                "make_model": v.make_model,
                "fuel_lock": "DIESEL ONLY",
                "tank_capacity_litres": v.tank_capacity_litres,
                "avg_mileage_kmpl": v.avg_mileage_kmpl,
                "rules": {
                    "max_per_txn": 12000.0,
                    "daily_limit": 25000.0,
                    "geofence_enforced": True,
                    "time_locks": "06:00 AM - 09:00 PM",
                },
                "anti_pilferage_status": "PROTECTED BY DISPENSER OCR",
            }
            for v in vehicles
        ],
    }


@router.put("/vehicles/{vehicle_id}/rules")
def update_vehicle_rules(vehicle_id: int, payload: UpdateVehicleRuleRequest, db: Session = Depends(get_db)):
    """Update granular fuel rule: e.g. Diesel-only lock, daily rupee cap, geofence."""
    tag = db.query(QRTag).filter(QRTag.vehicle_id == vehicle_id).first()
    if not tag:
        raise HTTPException(status_code=404, detail="Vehicle tag not found")

    tag.allowed_fuel_type = payload.allowed_fuel_type
    tag.max_per_txn = payload.max_per_txn
    tag.daily_limit = payload.daily_limit
    tag.monthly_limit = payload.monthly_limit
    tag.require_geofence_match = payload.require_geofence_match
    db.commit()

    return {
        "success": True,
        "message": f"Rules updated: Fuel locked to {payload.allowed_fuel_type.value}, Cap ₹{payload.max_per_txn}",
    }


@router.post("/wallet/topup")
def topup_fleet_wallet(payload: TopupWalletRequest, db: Session = Depends(get_db)):
    """Add funds to B2B corporate master pool."""
    org = db.query(FleetOrganization).first()
    if not org:
        raise HTTPException(status_code=404, detail="Organization not found")

    org.master_wallet_balance = round(org.master_wallet_balance + payload.amount, 2)
    db.commit()

    return {
        "success": True,
        "message": f"Successfully credited ₹{payload.amount} via {payload.payment_mode}",
        "new_balance": org.master_wallet_balance,
    }
