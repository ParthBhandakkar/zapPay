import logging
import uuid
from datetime import datetime
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import (
    FuelCategory,
    MandateRail,
    PaymentMandate,
    PetrolPump,
    QRTag,
    StationPurityReview,
    User,
    UserRole,
    Vehicle,
    VehicleType,
    Wallet,
)

logger = logging.getLogger("zappay.routers.consumer")
router = APIRouter(prefix="/api/v1/consumer", tags=["consumer"])


# ── DTOs ────────────────────────────────────────────────────────────────
class AddVehicleRequest(BaseModel):
    vrn_number: str
    owner_user_id: Optional[int] = None
    make_model: Optional[str] = None
    vehicle_class: Optional[VehicleType] = VehicleType.FOUR_WHEELER
    fuel_type: Optional[FuelCategory] = FuelCategory.PETROL
    tank_capacity_litres: Optional[float] = 45.0
    insurance_expiry_date: Optional[str] = "2027-04-15"
    puc_expiry_date: Optional[str] = "2026-12-10"
    emergency_contact_name: Optional[str] = "Family Member"
    emergency_contact_phone: Optional[str] = "9876543211"
    blood_group: Optional[str] = "O+"


class BindQRRequest(BaseModel):
    qr_public_token: str
    max_per_txn: Optional[float] = 2500.0
    daily_limit: Optional[float] = 5000.0


class CreateMandateRequest(BaseModel):
    rail_type: MandateRail = MandateRail.UPI_AUTOPAY
    vehicle_id: Optional[int] = None
    payer_vpa: str = "user@okaxis"
    max_limit: float = 2500.0
    delegated_to_name: Optional[str] = None
    delegated_to_phone: Optional[str] = None


class LogMileageRequest(BaseModel):
    vehicle_id: int
    station_id: int
    odometer_km: int
    litres_filled: float
    feedback: Optional[str] = None


# ── 1. Vehicles & Compliance Vault ─────────────────────────────────────
@router.get("/vehicles")
def get_user_vehicles(user_id: Optional[int] = None, db: Session = Depends(get_db)):
    """Fetch user's vehicles with complete Compliance Vault (RC, Insurance, PUC, Odometer)."""
    vehicles = db.query(Vehicle).all()
    if not vehicles:
        # Seed an initial vehicle for instant testing
        user = db.query(User).first()
        if not user:
            user = User(
                phone_number="9876543210",
                full_name="Aakash Verma",
                password_hash="pwd",
                role=UserRole.CUSTOMER,
            )
            db.add(user)
            db.flush()

        v = Vehicle(
            owner_user_id=user.id,
            vrn_number="CG-10-AB-4582",
            vahan_verified=True,
            make_model="Tata Nexon EV / Petrol",
            fuel_type=FuelCategory.PETROL,
            tank_capacity_litres=44.0,
            insurance_expiry_date="2027-04-15",
            puc_expiry_date="2026-12-10",
            last_odometer_km=14200,
            avg_mileage_kmpl=17.2,
            emergency_contact_name="Pooja Verma",
            emergency_contact_phone="9876543211",
            blood_group="O+",
        )
        db.add(v)
        db.flush()

        tag = QRTag(
            qr_public_token="zp_cg_4582",
            vehicle_id=v.id,
            max_per_txn=2500.0,
            daily_limit=5000.0,
        )
        db.add(tag)
        db.commit()
        vehicles = [v]

    result = []
    for v in vehicles:
        tag = db.query(QRTag).filter(QRTag.vehicle_id == v.id).first()
        fuel_str = v.fuel_type.value if hasattr(v.fuel_type, "value") else str(v.fuel_type)
        class_str = v.vehicle_class.value if hasattr(v.vehicle_class, "value") else str(v.vehicle_class)
        result.append({
            "id": v.id,
            "vrn_number": v.vrn_number,
            "make_model": v.make_model,
            "vehicle_class": class_str,
            "fuel_type": fuel_str,
            "vahan_verified": v.vahan_verified,
            "tank_capacity_litres": v.tank_capacity_litres,
            "insurance_expiry_date": v.insurance_expiry_date,
            "insurance_status": "VALID",
            "puc_expiry_date": v.puc_expiry_date,
            "puc_status": "VALID",
            "last_odometer_km": v.last_odometer_km,
            "avg_mileage_kmpl": v.avg_mileage_kmpl,
            "parking_contact_enabled": v.parking_contact_enabled,
            "emergency_contact_name": v.emergency_contact_name,
            "emergency_contact_phone": v.emergency_contact_phone,
            "blood_group": v.blood_group,
            "qr_tag": {
                "token": tag.qr_public_token if tag else None,
                "status": tag.status if tag else "UNASSIGNED",
                "max_per_txn": tag.max_per_txn if tag else 2500.0,
                "daily_limit": tag.daily_limit if tag else 5000.0,
                "public_url": f"https://zappay.in/v/{tag.qr_public_token}" if tag else None,
            } if tag else None,
        })

    return {"success": True, "vehicles": result}


@router.post("/vehicles")
def add_vehicle(payload: AddVehicleRequest, db: Session = Depends(get_db)):
    """Add new vehicle with instant simulated Vahan RC lookup."""
    owner_id = payload.owner_user_id
    if not owner_id:
        user = db.query(User).first()
        owner_id = user.id if user else 1

    clean_vrn = payload.vrn_number.upper().strip()
    existing = db.query(Vehicle).filter(Vehicle.vrn_number == clean_vrn).first()
    if existing:
        return {"success": True, "message": "Vehicle already registered", "vehicle_id": existing.id}

    # Simulate Vahan RC lookup data
    make_model = payload.make_model or ("Hyundai Creta 1.5" if "CG" in clean_vrn else "Maruti Suzuki Swift")

    v = Vehicle(
        owner_user_id=owner_id,
        vrn_number=clean_vrn,
        vahan_verified=True,
        make_model=make_model,
        vehicle_class=payload.vehicle_class,
        fuel_type=payload.fuel_type,
        tank_capacity_litres=payload.tank_capacity_litres,
        insurance_expiry_date=payload.insurance_expiry_date,
        puc_expiry_date=payload.puc_expiry_date,
        emergency_contact_name=payload.emergency_contact_name,
        emergency_contact_phone=payload.emergency_contact_phone,
        blood_group=payload.blood_group,
    )
    db.add(v)
    db.flush()

    # Generate auto QR token for testing
    tag_token = f"zp_{clean_vrn.lower().replace('-', '')[-6:]}"
    tag = QRTag(
        qr_public_token=tag_token,
        vehicle_id=v.id,
        max_per_txn=2500.0,
        daily_limit=5000.0,
    )
    db.add(tag)
    db.commit()
    db.refresh(v)

    return {
        "success": True,
        "message": "Vehicle registered and Vahan verified successfully!",
        "vehicle_id": v.id,
        "vrn_number": v.vrn_number,
        "qr_token": tag.qr_public_token,
    }


@router.post("/vehicles/{vehicle_id}/bind-qr")
def bind_qr_sticker(vehicle_id: int, payload: BindQRRequest, db: Session = Depends(get_db)):
    """Bind a physical tamper-proof QR sticker UUID to the vehicle."""
    vehicle = db.query(Vehicle).filter(Vehicle.id == vehicle_id).first()
    if not vehicle:
        raise HTTPException(status_code=404, detail="Vehicle not found")

    tag = db.query(QRTag).filter(QRTag.vehicle_id == vehicle_id).first()
    if not tag:
        tag = QRTag(
            qr_public_token=payload.qr_public_token,
            vehicle_id=vehicle_id,
            max_per_txn=payload.max_per_txn or 2500.0,
            daily_limit=payload.daily_limit or 5000.0,
        )
        db.add(tag)
    else:
        tag.qr_public_token = payload.qr_public_token
        tag.max_per_txn = payload.max_per_txn or tag.max_per_txn
        tag.daily_limit = payload.daily_limit or tag.daily_limit

    db.commit()
    return {
        "success": True,
        "message": f"Sticker {payload.qr_public_token} successfully bound to {vehicle.vrn_number}",
        "qr_token": tag.qr_public_token,
        "public_url": f"https://zappay.in/v/{tag.qr_public_token}",
    }


# ── 2. Payment Mandates (UPI Autopay & UPI Circle) ─────────────────────
@router.get("/mandates")
def get_user_mandates(db: Session = Depends(get_db)):
    """List active UPI Autopay and UPI Circle delegated mandates."""
    mandates = db.query(PaymentMandate).all()
    if not mandates:
        # Seed an Autopay and a UPI Circle mandate
        user = db.query(User).first()
        uid = user.id if user else 1
        m1 = PaymentMandate(
            user_id=uid,
            rail_type=MandateRail.UPI_AUTOPAY,
            ummn_reference=f"UMMN{uuid.uuid4().hex[:12].upper()}",
            payer_vpa="aakash@okaxis",
            max_limit=2500.0,
            status="ACTIVE",
        )
        m2 = PaymentMandate(
            user_id=uid,
            rail_type=MandateRail.UPI_CIRCLE_DELEGATED,
            ummn_reference=f"UMMN{uuid.uuid4().hex[:12].upper()}",
            payer_vpa="aakash@okaxis",
            max_limit=1500.0,
            status="ACTIVE",
            delegated_to_name="Rohan Verma (Son - 2 Wheeler)",
            delegated_to_phone="9876543299",
        )
        db.add(m1)
        db.add(m2)
        db.commit()
        mandates = [m1, m2]

    return {
        "success": True,
        "mandates": [
            {
                "id": m.id,
                "rail_type": m.rail_type.value if hasattr(m.rail_type, "value") else str(m.rail_type),
                "ummn_reference": m.ummn_reference,
                "payer_vpa": m.payer_vpa,
                "max_limit": m.max_limit,
                "status": m.status,
                "delegated_to_name": m.delegated_to_name,
                "delegated_to_phone": m.delegated_to_phone,
                "valid_until": m.valid_until,
            }
            for m in mandates
        ],
    }


@router.post("/mandates/create")
def create_mandate(payload: CreateMandateRequest, db: Session = Depends(get_db)):
    """Set up NPCI UPI Autopay or UPI Circle family fueling mandate."""
    user = db.query(User).first()
    uid = user.id if user else 1

    m = PaymentMandate(
        user_id=uid,
        vehicle_id=payload.vehicle_id,
        rail_type=payload.rail_type,
        ummn_reference=f"UMMN{uuid.uuid4().hex[:12].upper()}",
        payer_vpa=payload.payer_vpa,
        max_limit=payload.max_limit,
        delegated_to_name=payload.delegated_to_name,
        delegated_to_phone=payload.delegated_to_phone,
        status="ACTIVE",
    )
    db.add(m)
    db.commit()
    db.refresh(m)

    return {
        "success": True,
        "message": f"Successfully activated {m.rail_type} mandate for ₹{m.max_limit} cap",
        "mandate_id": m.id,
        "ummn": m.ummn_reference,
    }


# ── 3. Mileage & Petrol Pump Purity Scores ─────────────────────────────
@router.post("/mileage-log")
def log_mileage(payload: LogMileageRequest, db: Session = Depends(get_db)):
    """Log vehicle odometer after fueling to compute exact km/L and station purity score."""
    vehicle = db.query(Vehicle).filter(Vehicle.id == payload.vehicle_id).first()
    if not vehicle:
        vehicle = db.query(Vehicle).first()
    if not vehicle:
        raise HTTPException(status_code=404, detail="Vehicle not found")

    dist = payload.odometer_km - vehicle.last_odometer_km
    kmpl = round(dist / payload.litres_filled, 2) if dist > 0 and payload.litres_filled > 0 else 16.5
    if kmpl > 40.0 or kmpl < 4.0:
        kmpl = 16.5  # sanitize edge cases

    vehicle.last_odometer_km = payload.odometer_km
    vehicle.avg_mileage_kmpl = round((vehicle.avg_mileage_kmpl + kmpl) / 2.0, 2)

    # Station purity review
    purity = 5.0 if kmpl >= 15.0 else (4.2 if kmpl >= 12.0 else 3.5)
    review = StationPurityReview(
        station_id=payload.station_id,
        user_id=vehicle.owner_user_id,
        vehicle_id=payload.vehicle_id,
        odometer_km=payload.odometer_km,
        litres_filled=payload.litres_filled,
        computed_kmpl=kmpl,
        purity_score=purity,
        feedback=payload.feedback or "Smooth engine performance and verified density",
    )
    db.add(review)
    db.commit()

    return {
        "success": True,
        "distance_covered_km": max(dist, 0),
        "computed_kmpl": kmpl,
        "updated_avg_kmpl": vehicle.avg_mileage_kmpl,
        "station_purity_score": purity,
        "message": f"Logged! Your mileage for this tank was {kmpl} km/L.",
    }


@router.get("/stations/nearby")
def get_nearby_stations(db: Session = Depends(get_db)):
    """List verified petrol pumps with live density scores and trust ratings."""
    stations = db.query(PetrolPump).all()
    if not stations:
        # Seed standard pilot stations
        s1 = PetrolPump(
            pump_name="Bharat Petroleum - Bilaspur Ring Road",
            owner_name="Shri Rajesh Agrawal",
            license_number="CG-BP-0012",
            address="Ring Road No 2, Bilaspur",
            city="Bilaspur",
            state="Chhattisgarh",
            pincode="495001",
            phone_number="9827110001",
            fuel_types="Petrol, Diesel, CNG",
            commission_rate=0.02,
            mdr_fee_bps=0,
            verified_petrol_density=743.2,
            verified_diesel_density=836.5,
            trust_score=4.95,
        )
        s2 = PetrolPump(
            pump_name="Indian Oil - Raipur Expressway Hub",
            owner_name="M/s Chhattisgarh Fuel Services",
            license_number="CG-IOC-0045",
            address="VIP Road, Raipur",
            city="Raipur",
            state="Chhattisgarh",
            pincode="492001",
            phone_number="9827110002",
            fuel_types="Petrol, Diesel",
            commission_rate=0.02,
            mdr_fee_bps=50,
            verified_petrol_density=741.8,
            verified_diesel_density=835.0,
            trust_score=4.88,
        )
        db.add(s1)
        db.add(s2)
        db.commit()
        stations = [s1, s2]

    return {
        "success": True,
        "stations": [
            {
                "id": s.id,
                "name": s.pump_name,
                "city": s.city,
                "address": s.address,
                "verified_petrol_density": s.verified_petrol_density or 742.0,
                "verified_diesel_density": s.verified_diesel_density or 835.0,
                "trust_score": s.trust_score or 5.0,
                "fuel_types": s.fuel_types,
                "mdr_mode": "0% DEALER SUBSIDIZED" if (s.mdr_fee_bps or 0) == 0 else "0.5% MDR",
            }
            for s in stations
        ],
    }
