"""Seed realistic forecourt demo data directly to Supabase production database."""
import json
import os
import sys
from datetime import datetime, timedelta

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Add backend directory to sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app.models import (
    User, UserRole, Wallet, PetrolPump, Vehicle, QRTag, PaymentMandate,
    PumpOperator, Transaction, TransactionType, TransactionStatus,
    VehicleType, QRTagStatus, MandateRail, FuelCategory, UserVehicle
)
from app.services.auth import get_password_hash

DATABASE_URL = os.environ.get(
    "DATABASE_URL",
    "postgresql+psycopg2://postgres.bxcvibvxbjqctmdazpfr:ZapPay2026SecureP99x!@aws-0-ap-south-1.pooler.supabase.com:6543/postgres"
)

def seed():
    engine = create_engine(DATABASE_URL)
    Session = sessionmaker(bind=engine)
    db = Session()

    print("Checking existing data...")
    existing_user = db.query(User).filter(User.phone_number == "+919876543210").first()
    if existing_user:
        print("Demo user +919876543210 already exists.")
    else:
        print("Creating demo customer user...")
        demo_user = User(
            phone_number="+919876543210",
            email="parth@zappay.in",
            full_name="Parth Dev",
            password_hash=get_password_hash("ZapPay@2026"),
            role=UserRole.CUSTOMER,
            is_active=True,
            is_verified=True,
            city="Pune",
            state="Maharashtra",
            pincode="411045",
            kyc_status="verified",
            vehicle_number="MH12AB1234",
            vehicle_type="4_wheeler",
            auto_recharge_enabled=True,
            auto_recharge_threshold=500.0,
            auto_recharge_amount=2000.0,
            auto_recharge_payment_method="upi",
        )
        db.add(demo_user)
        db.flush()

        wallet = Wallet(
            user_id=demo_user.id,
            balance=5000.0,
            total_recharged=10000.0,
            total_spent=5000.0,
            is_active=True
        )
        db.add(wallet)

        # Vehicles
        v1 = Vehicle(
            owner_user_id=demo_user.id,
            vrn_number="MH12AB1234",
            vahan_verified=True,
            make_model="Tata Nexon Fearless Petrol",
            vehicle_class=VehicleType.FOUR_WHEELER,
            fuel_type=FuelCategory.PETROL,
            tank_capacity_litres=44.0,
            insurance_expiry_date=datetime.utcnow() + timedelta(days=280),
            puc_expiry_date=datetime.utcnow() + timedelta(days=120),
            last_odometer_km=14250.0,
            avg_mileage_kmpl=16.8,
            parking_contact_enabled=True,
            emergency_contact_name="Family Member",
            emergency_contact_phone="+919876500000",
            blood_group="B+"
        )
        v2 = Vehicle(
            owner_user_id=demo_user.id,
            vrn_number="MH14CD5678",
            vahan_verified=True,
            make_model="Hyundai Creta SX Diesel",
            vehicle_class=VehicleType.FOUR_WHEELER,
            fuel_type=FuelCategory.DIESEL,
            tank_capacity_litres=50.0,
            insurance_expiry_date=datetime.utcnow() + timedelta(days=190),
            puc_expiry_date=datetime.utcnow() + timedelta(days=95),
            last_odometer_km=32100.0,
            avg_mileage_kmpl=19.4,
            parking_contact_enabled=True,
            emergency_contact_name="Family Member",
            emergency_contact_phone="+919876500000",
            blood_group="B+"
        )
        db.add(v1)
        db.add(v2)
        db.flush()

        # QR Tag on windshield / fuel flap
        qr_tag = QRTag(
            qr_public_token="ZP-TAG-MH12AB1234",
            hmac_secret_key="secret-salt-zp-9912",
            vehicle_id=v1.id,
            status=QRTagStatus.ACTIVE,
            batch_code="BATCH-2026-Q1",
            max_per_txn=3500.0,
            daily_limit=5000.0,
            monthly_limit=25000.0,
            require_geofence_match=True,
            allowed_fuel_type=FuelCategory.PETROL,
            activated_at=datetime.utcnow()
        )
        db.add(qr_tag)

        # Payment Mandate
        mandate = PaymentMandate(
            user_id=demo_user.id,
            vehicle_id=v1.id,
            rail_type=MandateRail.UPI_AUTOPAY,
            ummn_reference="UMMN-ZP-8837190",
            payer_vpa="parth@oksbi",
            max_limit=3000.0,
            status="active",
            delegated_to_name="Parth Dev",
            delegated_to_phone="+919876543210",
            valid_until=datetime.utcnow() + timedelta(days=365)
        )
        db.add(mandate)

    # Petrol Pumps
    existing_pumps = db.query(PetrolPump).count()
    if existing_pumps == 0:
        print("Seeding smart petrol pumps...")
        p1 = PetrolPump(
            pump_name="HPCL Smart Forecourt - Pune Expressway",
            owner_name="Hindustan Petroleum Corp",
            license_number="LIC-HPCL-PN-2024-991",
            address="Expressway Km 42, Urse Toll Plaza",
            city="Pune",
            state="Maharashtra",
            pincode="410506",
            latitude=18.7214,
            longitude=73.6821,
            phone_number="+919822001122",
            email="hpcl.urse@zappay.in",
            daily_fuel_capacity=45000.0,
            fuel_types="Petrol,Diesel,XP95,Power95",
            fuel_rates=json.dumps({"petrol": 104.21, "diesel": 90.75, "power95": 112.50}),
            is_open=True,
            commission_rate=1.2,
            verified_petrol_density=742.5,
            verified_diesel_density=831.2,
            trust_score=99.2,
            is_active=True,
            is_verified=True
        )
        p2 = PetrolPump(
            pump_name="BPCL Automated AutoCare - Hinjawadi Phase 1",
            owner_name="Bharat Petroleum Ltd",
            license_number="LIC-BPCL-HINJ-2023-45",
            address="Hinjawadi Phase 1 Main Road, Near Infosys Circle",
            city="Pune",
            state="Maharashtra",
            pincode="411057",
            latitude=18.5913,
            longitude=73.7389,
            phone_number="+919822334455",
            email="bpcl.hinj@zappay.in",
            daily_fuel_capacity=35000.0,
            fuel_types="Petrol,Diesel,CNG,Speed",
            fuel_rates=json.dumps({"petrol": 104.18, "diesel": 90.72, "cng": 86.00}),
            is_open=True,
            commission_rate=1.2,
            verified_petrol_density=743.1,
            verified_diesel_density=830.8,
            trust_score=98.7,
            is_active=True,
            is_verified=True
        )
        p3 = PetrolPump(
            pump_name="IndianOil Swachh Fuel Point - Baner",
            owner_name="Indian Oil Corporation",
            license_number="LIC-IOCL-BAN-2025-12",
            address="Baner-Pashan Link Road",
            city="Pune",
            state="Maharashtra",
            pincode="411045",
            latitude=18.5590,
            longitude=73.7868,
            phone_number="+919822556677",
            email="iocl.baner@zappay.in",
            daily_fuel_capacity=30000.0,
            fuel_types="Petrol,Diesel,XP100",
            fuel_rates=json.dumps({"petrol": 104.15, "diesel": 90.70}),
            is_open=True,
            commission_rate=1.2,
            verified_petrol_density=744.0,
            verified_diesel_density=832.0,
            trust_score=97.9,
            is_active=True,
            is_verified=True
        )
        db.add_all([p1, p2, p3])

    db.flush()
    demo_user = db.query(User).filter(User.phone_number == "+919876543210").first()
    if demo_user:
        demo_user.address = "Baner-Pashan Link Road, Baner"
        demo_user.city = "Pune"
        demo_user.state = "Maharashtra"
        demo_user.pincode = "411045"
        demo_user.vehicle_number = "MH12AB1234"
        demo_user.vehicle_type = "Car"

        if db.query(UserVehicle).filter(UserVehicle.user_id == demo_user.id).count() == 0:
            print("Seeding UserVehicle entries...")
            db.add_all([
                UserVehicle(
                    user_id=demo_user.id,
                    vehicle_number="MH12AB1234",
                    vehicle_type="Car",
                    nickname="Tata Nexon Fearless",
                    is_primary=True,
                    is_active=True,
                ),
                UserVehicle(
                    user_id=demo_user.id,
                    vehicle_number="MH14CD5678",
                    vehicle_type="Car",
                    nickname="Hyundai Creta SX",
                    is_primary=False,
                    is_active=True,
                ),
            ])

        wallet = db.query(Wallet).filter(Wallet.user_id == demo_user.id).first()
        first_pump = db.query(PetrolPump).first()
        if wallet and db.query(Transaction).filter(Transaction.user_id == demo_user.id).count() == 0:
            print("Seeding demo transactions...")
            now = datetime.utcnow()
            db.add_all([
                Transaction(
                    transaction_id="TXN-ZP-2026-1001",
                    user_id=demo_user.id,
                    wallet_id=wallet.id,
                    transaction_type=TransactionType.WALLET_RECHARGE,
                    amount=5000.0,
                    commission_amount=0.0,
                    status=TransactionStatus.COMPLETED,
                    created_at=now - timedelta(days=3),
                    completed_at=now - timedelta(days=3),
                ),
                Transaction(
                    transaction_id="TXN-ZP-2026-1002",
                    user_id=demo_user.id,
                    wallet_id=wallet.id,
                    pump_id=first_pump.id if first_pump else None,
                    transaction_type=TransactionType.FUEL_PURCHASE,
                    amount=1563.15,
                    commission_amount=18.75,
                    fuel_quantity=15.0,
                    fuel_type="Petrol",
                    fuel_rate=104.21,
                    ocr_verified=True,
                    status=TransactionStatus.COMPLETED,
                    created_at=now - timedelta(days=2),
                    completed_at=now - timedelta(days=2),
                ),
                Transaction(
                    transaction_id="TXN-ZP-2026-1003",
                    user_id=demo_user.id,
                    wallet_id=wallet.id,
                    pump_id=first_pump.id if first_pump else None,
                    transaction_type=TransactionType.FUEL_PURCHASE,
                    amount=907.50,
                    commission_amount=10.89,
                    fuel_quantity=10.0,
                    fuel_type="Diesel",
                    fuel_rate=90.75,
                    ocr_verified=True,
                    status=TransactionStatus.COMPLETED,
                    created_at=now - timedelta(hours=6),
                    completed_at=now - timedelta(hours=6),
                ),
            ])

    db.commit()
    print("Database seeding completed successfully!")
    db.close()

if __name__ == "__main__":
    seed()
