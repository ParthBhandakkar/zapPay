import enum

from sqlalchemy import (
    Boolean,
    Column,
    DateTime,
    Enum,
    Float,
    ForeignKey,
    Index,
    Integer,
    String,
    Text,
)
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from app.database import Base


# ── Enums ──────────────────────────────────────────────────────────────
class UserRole(str, enum.Enum):
    CUSTOMER = "customer"
    PUMP_OPERATOR = "pump_operator"
    PUMP_OWNER = "pump_owner"
    FLEET_ADMIN = "fleet_admin"
    ADMIN = "admin"


class TransactionStatus(str, enum.Enum):
    PENDING = "pending"
    INITIATED = "initiated"
    OCR_VERIFIED = "ocr_verified"
    AUTHORIZED = "authorized"
    COMPLETED = "completed"
    SETTLED = "settled"
    FAILED = "failed"
    REFUNDED = "refunded"
    DISPUTED = "disputed"


class TransactionType(str, enum.Enum):
    FUEL_PURCHASE = "fuel_purchase"
    WALLET_RECHARGE = "wallet_recharge"
    REFUND = "refund"
    COMMISSION = "commission"
    FLEET_POOL_DEBIT = "fleet_pool_debit"


class KYCStatus(str, enum.Enum):
    PENDING = "pending"
    VERIFIED = "verified"
    REJECTED = "rejected"


class FuelCategory(str, enum.Enum):
    PETROL = "PETROL"
    DIESEL = "DIESEL"
    CNG = "CNG"
    EV = "EV"
    PETROL_CNG = "PETROL_CNG"


class VehicleType(str, enum.Enum):
    TWO_WHEELER = "TWO_WHEELER"
    THREE_WHEELER = "THREE_WHEELER"
    FOUR_WHEELER = "FOUR_WHEELER"
    COMMERCIAL_LIGHT = "COMMERCIAL_LIGHT"
    COMMERCIAL_HEAVY = "COMMERCIAL_HEAVY"


class MandateRail(str, enum.Enum):
    UPI_AUTOPAY = "UPI_AUTOPAY"
    UPI_CIRCLE_DELEGATED = "UPI_CIRCLE_DELEGATED"
    UPI_LITE_X = "UPI_LITE_X"
    FLEET_MASTER_WALLET = "FLEET_MASTER_WALLET"
    RUPAY_CREDIT_UPI = "RUPAY_CREDIT_UPI"


class QRTagStatus(str, enum.Enum):
    PRINTED_UNASSIGNED = "PRINTED_UNASSIGNED"
    ACTIVE = "ACTIVE"
    TAMPERED_VOID = "TAMPERED_VOID"
    LOST_BLOCKED = "LOST_BLOCKED"


class AccountType(str, enum.Enum):
    CUSTOMER_MANDATE = "CUSTOMER_MANDATE"
    FLEET_WALLET = "FLEET_WALLET"
    DEALER_PAYABLE = "DEALER_PAYABLE"
    ZAPPAY_REVENUE = "ZAPPAY_REVENUE"


# ── Models ─────────────────────────────────────────────────────────────
class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    phone_number = Column(String(15), unique=True, index=True, nullable=False)
    email = Column(String(255), unique=True, index=True)
    full_name = Column(String(255), nullable=False)
    password_hash = Column(String(255), nullable=False)
    role = Column(Enum(UserRole), default=UserRole.CUSTOMER)
    is_active = Column(Boolean, default=True)
    is_verified = Column(Boolean, default=False)

    date_of_birth = Column(DateTime)
    address = Column(Text)
    city = Column(String(100))
    state = Column(String(100))
    pincode = Column(String(10))

    kyc_status = Column(Enum(KYCStatus), default=KYCStatus.PENDING)
    aadhaar_number = Column(String(12))
    pan_number = Column(String(10))
    driving_license = Column(String(20))

    vehicle_number = Column(String(20))
    vehicle_type = Column(String(50))

    auto_recharge_enabled = Column(Boolean, default=False)
    auto_recharge_threshold = Column(Float, default=200.0)
    auto_recharge_amount = Column(Float, default=1000.0)
    auto_recharge_payment_method = Column(String(50))

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    last_login = Column(DateTime(timezone=True))

    wallet = relationship("Wallet", back_populates="user", uselist=False)
    transactions = relationship("Transaction", back_populates="user")
    qr_codes = relationship("QRCode", back_populates="user")
    pump_associations = relationship("PumpOperator", back_populates="user")

    def __repr__(self):
        return f"<User id={self.id} phone={self.phone_number} role={self.role}>"


class Wallet(Base):
    __tablename__ = "wallets"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), unique=True, nullable=False)
    balance = Column(Float, default=0.0, nullable=False)
    total_recharged = Column(Float, default=0.0)
    total_spent = Column(Float, default=0.0)
    is_active = Column(Boolean, default=True)

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    user = relationship("User", back_populates="wallet")
    transactions = relationship("Transaction", back_populates="wallet")

    def __repr__(self):
        return f"<Wallet id={self.id} user_id={self.user_id} balance={self.balance}>"


class QRCode(Base):
    __tablename__ = "qr_codes"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    qr_code = Column(String(255), unique=True, index=True, nullable=False)
    qr_image_path = Column(String(500))
    is_active = Column(Boolean, default=True)
    qr_type = Column(String(50), default="mobile")
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    expires_at = Column(DateTime(timezone=True))

    user = relationship("User", back_populates="qr_codes")
    transactions = relationship("Transaction", back_populates="qr_code")

    def __repr__(self):
        return f"<QRCode id={self.id} user_id={self.user_id} active={self.is_active}>"


class PetrolPump(Base):
    __tablename__ = "petrol_pumps"

    id = Column(Integer, primary_key=True, index=True)
    pump_name = Column(String(255), nullable=False)
    owner_name = Column(String(255), nullable=False)
    license_number = Column(String(100), unique=True, nullable=False)
    address = Column(Text, nullable=False)
    city = Column(String(100), nullable=False)
    state = Column(String(100), nullable=False)
    pincode = Column(String(10), nullable=False)
    latitude = Column(Float)
    longitude = Column(Float)
    phone_number = Column(String(15), nullable=False)
    email = Column(String(255))
    daily_fuel_capacity = Column(Float)
    fuel_types = Column(String(255))
    commission_rate = Column(Float, default=0.02)
    mdr_fee_bps = Column(Integer, default=0) # 0 bps (0%) or 50 bps (0.5%)
    verified_petrol_density = Column(Float, default=742.0)
    verified_diesel_density = Column(Float, default=835.0)
    trust_score = Column(Float, default=5.0)
    is_active = Column(Boolean, default=True)
    is_verified = Column(Boolean, default=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    operators = relationship("PumpOperator", back_populates="pump")
    transactions = relationship("Transaction", back_populates="pump")
    settlements = relationship("Settlement", back_populates="pump")

    def __repr__(self):
        return f"<PetrolPump id={self.id} name={self.pump_name} city={self.city}>"


class PumpOperator(Base):
    __tablename__ = "pump_operators"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    pump_id = Column(Integer, ForeignKey("petrol_pumps.id"), nullable=False)
    employee_id = Column(String(50))
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="pump_associations")
    pump = relationship("PetrolPump", back_populates="operators")

    def __repr__(self):
        return f"<PumpOperator id={self.id} user_id={self.user_id} pump_id={self.pump_id}>"


class Transaction(Base):
    __tablename__ = "transactions"

    id = Column(Integer, primary_key=True, index=True)
    transaction_id = Column(String(100), unique=True, index=True, nullable=False)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    wallet_id = Column(Integer, ForeignKey("wallets.id"), nullable=False)
    qr_code_id = Column(Integer, ForeignKey("qr_codes.id"))
    pump_id = Column(Integer, ForeignKey("petrol_pumps.id"))
    operator_id = Column(Integer, ForeignKey("pump_operators.id"))
    transaction_type = Column(Enum(TransactionType), nullable=False)
    amount = Column(Float, nullable=False)
    commission_amount = Column(Float, default=0.0)
    fuel_quantity = Column(Float)
    fuel_type = Column(String(50))
    fuel_rate = Column(Float)
    idempotency_key = Column(String(100), unique=True, index=True, nullable=True)
    fuel_dispensed = Column(String(50))
    ocr_verified = Column(Boolean, default=False)
    ocr_confidence = Column(Float, default=1.0)
    soundbox_payload = Column(String(255), nullable=True)
    platform_fee = Column(Float, default=0.0)
    net_dealer_amount = Column(Float, default=0.0)
    status = Column(Enum(TransactionStatus), default=TransactionStatus.PENDING)
    payment_gateway_response = Column(Text)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    completed_at = Column(DateTime(timezone=True))

    user = relationship("User", back_populates="transactions")
    wallet = relationship("Wallet", back_populates="transactions")
    qr_code = relationship("QRCode", back_populates="transactions")
    pump = relationship("PetrolPump", back_populates="transactions")

    def __repr__(self):
        return f"<Transaction id={self.id} tid={self.transaction_id} amount={self.amount} status={self.status}>"


class Settlement(Base):
    __tablename__ = "settlements"

    id = Column(Integer, primary_key=True, index=True)
    settlement_id = Column(String(100), unique=True, index=True, nullable=False)
    pump_id = Column(Integer, ForeignKey("petrol_pumps.id"), nullable=False)
    total_transactions = Column(Integer, default=0)
    total_amount = Column(Float, default=0.0)
    commission_amount = Column(Float, default=0.0)
    net_amount = Column(Float, default=0.0)
    settlement_date = Column(DateTime(timezone=True), server_default=func.now())
    from_date = Column(DateTime(timezone=True), nullable=False)
    to_date = Column(DateTime(timezone=True), nullable=False)
    is_processed = Column(Boolean, default=False)
    processed_at = Column(DateTime(timezone=True))

    pump = relationship("PetrolPump", back_populates="settlements")

    def __repr__(self):
        return f"<Settlement id={self.id} sid={self.settlement_id} pump_id={self.pump_id}>"


class OTP(Base):
    __tablename__ = "otps"

    id = Column(Integer, primary_key=True, index=True)
    phone_number = Column(String(15), nullable=False)
    otp_code = Column(String(6), nullable=False)
    otp_type = Column(String(50), nullable=False)
    is_used = Column(Boolean, default=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    expires_at = Column(DateTime(timezone=True), nullable=False)
    used_at = Column(DateTime(timezone=True))


# ── ZapPay 2.0 Ecosystem Models ───────────────────────────────────────
class FleetOrganization(Base):
    __tablename__ = "fleet_organizations"

    id = Column(Integer, primary_key=True, index=True)
    owner_user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    company_name = Column(String(200), nullable=False)
    gstin = Column(String(15), unique=True)
    pan_number = Column(String(10))
    subscription_plan = Column(String(50), default="PRO_PER_VEHICLE")
    master_wallet_balance = Column(Float, default=0.0)
    credit_limit = Column(Float, default=0.0)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    owner = relationship("User")
    vehicles = relationship("Vehicle", back_populates="fleet_org")


class Vehicle(Base):
    __tablename__ = "vehicles"

    id = Column(Integer, primary_key=True, index=True)
    owner_user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    fleet_org_id = Column(Integer, ForeignKey("fleet_organizations.id"), nullable=True)
    vrn_number = Column(String(20), unique=True, index=True, nullable=False)
    vahan_verified = Column(Boolean, default=True)
    make_model = Column(String(120), default="Tata Nexon / Maruti Swift")
    vehicle_class = Column(Enum(VehicleType), default=VehicleType.FOUR_WHEELER)
    fuel_type = Column(Enum(FuelCategory), default=FuelCategory.PETROL)
    tank_capacity_litres = Column(Float, default=45.0)
    insurance_expiry_date = Column(String(30), default="2027-04-15")
    puc_expiry_date = Column(String(30), default="2026-12-10")
    last_odometer_km = Column(Integer, default=12450)
    avg_mileage_kmpl = Column(Float, default=16.8)
    parking_contact_enabled = Column(Boolean, default=True)
    emergency_contact_phone = Column(String(15), nullable=True)
    emergency_contact_name = Column(String(100), nullable=True)
    blood_group = Column(String(10), default="O+")
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    owner = relationship("User")
    fleet_org = relationship("FleetOrganization", back_populates="vehicles")
    qr_tag = relationship("QRTag", back_populates="vehicle", uselist=False)
    mandates = relationship("PaymentMandate", back_populates="vehicle")


class QRTag(Base):
    __tablename__ = "qr_tags"

    id = Column(Integer, primary_key=True, index=True)
    qr_public_token = Column(String(64), unique=True, index=True, nullable=False)
    hmac_secret_key = Column(String(128), default="zp_hmac_secret_key_prod_v2")
    vehicle_id = Column(Integer, ForeignKey("vehicles.id"), unique=True, nullable=True)
    status = Column(Enum(QRTagStatus), default=QRTagStatus.ACTIVE)
    batch_code = Column(String(50), default="BATCH_CG_01")
    max_per_txn = Column(Float, default=2500.0)
    daily_limit = Column(Float, default=5000.0)
    monthly_limit = Column(Float, default=25000.0)
    require_geofence_match = Column(Boolean, default=False)
    allowed_fuel_type = Column(Enum(FuelCategory), nullable=True)
    activated_at = Column(DateTime(timezone=True), server_default=func.now())
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    vehicle = relationship("Vehicle", back_populates="qr_tag")


class PaymentMandate(Base):
    __tablename__ = "payment_mandates"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    vehicle_id = Column(Integer, ForeignKey("vehicles.id"), nullable=True)
    rail_type = Column(Enum(MandateRail), default=MandateRail.UPI_AUTOPAY)
    ummn_reference = Column(String(100), unique=True, nullable=True)
    payer_vpa = Column(String(120), default="user@okaxis")
    max_limit = Column(Float, default=2500.0)
    status = Column(String(30), default="ACTIVE")
    delegated_to_name = Column(String(100), nullable=True)
    delegated_to_phone = Column(String(15), nullable=True)
    valid_until = Column(String(30), default="2030-12-31")
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User")
    vehicle = relationship("Vehicle", back_populates="mandates")


class DispenserMeterAudit(Base):
    __tablename__ = "dispenser_meter_audits"

    id = Column(Integer, primary_key=True, index=True)
    transaction_id = Column(String(100), index=True, nullable=True)
    station_id = Column(Integer, ForeignKey("petrol_pumps.id"), nullable=True)
    attendant_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    volume_litres = Column(Float, nullable=False)
    amount = Column(Float, nullable=False)
    density = Column(Float, default=742.0)
    fuel_rate = Column(Float, nullable=False)
    math_verified = Column(Boolean, default=True)
    confidence_score = Column(Float, default=0.99)
    manual_override = Column(Boolean, default=False)
    meter_image_path = Column(String(255), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())


class LedgerEntry(Base):
    __tablename__ = "ledger_entries"

    id = Column(Integer, primary_key=True, index=True)
    transaction_id = Column(String(100), index=True, nullable=True)
    account_type = Column(Enum(AccountType), nullable=False)
    account_owner_id = Column(Integer, nullable=False)
    direction = Column(String(10), nullable=False) # DEBIT or CREDIT
    amount = Column(Float, nullable=False)
    running_balance = Column(Float, default=0.0)
    description = Column(String(255))
    created_at = Column(DateTime(timezone=True), server_default=func.now())


class StationPurityReview(Base):
    __tablename__ = "station_purity_reviews"

    id = Column(Integer, primary_key=True, index=True)
    station_id = Column(Integer, ForeignKey("petrol_pumps.id"), nullable=False)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    vehicle_id = Column(Integer, ForeignKey("vehicles.id"), nullable=True)
    odometer_km = Column(Integer, nullable=False)
    litres_filled = Column(Float, nullable=False)
    computed_kmpl = Column(Float, default=15.0)
    purity_score = Column(Float, default=4.8)
    feedback = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())


# ── Composite Indexes ──────────────────────────────────────────────────
Index("idx_user_phone", User.phone_number)
Index("idx_user_email", User.email)
Index("idx_transaction_user_date", Transaction.user_id, Transaction.created_at)
Index("idx_transaction_pump_date", Transaction.pump_id, Transaction.created_at)
Index("idx_qr_code_user", QRCode.user_id, QRCode.is_active)
Index("idx_vehicles_vrn", Vehicle.vrn_number)
Index("idx_qr_tags_token", QRTag.qr_public_token)
