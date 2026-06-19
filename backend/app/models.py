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
    ADMIN = "admin"


class TransactionStatus(str, enum.Enum):
    PENDING = "pending"
    COMPLETED = "completed"
    FAILED = "failed"
    REFUNDED = "refunded"


class TransactionType(str, enum.Enum):
    FUEL_PURCHASE = "fuel_purchase"
    WALLET_RECHARGE = "wallet_recharge"
    REFUND = "refund"
    COMMISSION = "commission"


class KYCStatus(str, enum.Enum):
    PENDING = "pending"
    VERIFIED = "verified"
    REJECTED = "rejected"


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
    fuel_rates = Column(String(255))
    is_open = Column(Boolean, default=True)
    commission_rate = Column(Float, default=0.02)
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


class SettlementStatus(str, enum.Enum):
    PENDING = "pending"
    PROCESSING = "processing"
    PAID = "paid"
    FAILED = "failed"
    HELD = "held"


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
    status = Column(Enum(SettlementStatus), default=SettlementStatus.PENDING)
    is_processed = Column(Boolean, default=False)
    processed_at = Column(DateTime(timezone=True))
    bank_payout_ref = Column(String(255))
    notes = Column(Text)

    pump = relationship("PetrolPump", back_populates="settlements")

    def __repr__(self):
        return f"<Settlement id={self.id} sid={self.settlement_id} pump_id={self.pump_id} status={self.status}>"


class RefundStatus(str, enum.Enum):
    REQUESTED = "requested"
    APPROVED = "approved"
    PROCESSED = "processed"
    REJECTED = "rejected"


class RefundRequest(Base):
    __tablename__ = "refund_requests"

    id = Column(Integer, primary_key=True, index=True)
    transaction_id = Column(String(100), ForeignKey("transactions.transaction_id"), nullable=False)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    requested_by = Column(Integer, ForeignKey("users.id"), nullable=False)
    reason = Column(Text, nullable=False)
    status = Column(Enum(RefundStatus), default=RefundStatus.REQUESTED)
    reviewed_by = Column(Integer, ForeignKey("users.id"))
    review_notes = Column(Text)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    reviewed_at = Column(DateTime(timezone=True))

    transaction = relationship("Transaction", foreign_keys=[transaction_id])
    user = relationship("User", foreign_keys=[user_id])
    reviewer = relationship("User", foreign_keys=[reviewed_by])

    def __repr__(self):
        return f"<RefundRequest id={self.id} txn={self.transaction_id} status={self.status}>"


class AuditEvent(Base):
    __tablename__ = "audit_events"

    id = Column(Integer, primary_key=True, index=True)
    actor_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    actor_role = Column(String(50))
    action = Column(String(100), nullable=False)
    resource_type = Column(String(100), nullable=False)
    resource_id = Column(String(100))
    details = Column(Text)
    ip_address = Column(String(45))
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    actor = relationship("User", foreign_keys=[actor_id])

    def __repr__(self):
        return f"<AuditEvent id={self.id} action={self.action} resource={self.resource_type}>"


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


class IdempotencyKey(Base):
    __tablename__ = "idempotency_keys"

    id = Column(Integer, primary_key=True, index=True)
    idempotency_key = Column(String(255), unique=True, index=True, nullable=False)
    request_hash = Column(String(64), nullable=False)
    status = Column(String(50), nullable=False)
    response_body = Column(Text)
    transaction_id = Column(String(100))
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    expires_at = Column(DateTime(timezone=True), nullable=False)


class Country(Base):
    __tablename__ = "countries"

    id = Column(Integer, primary_key=True, index=True)
    code = Column(String(2), unique=True, nullable=False, index=True)
    name = Column(String(100), nullable=False)
    phone_code = Column(String(5))
    currency_code = Column(String(3), nullable=False)
    default_language = Column(String(10), default="en")
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())


class Currency(Base):
    __tablename__ = "currencies"

    id = Column(Integer, primary_key=True, index=True)
    code = Column(String(3), unique=True, nullable=False, index=True)
    name = Column(String(100), nullable=False)
    symbol = Column(String(10), nullable=False)
    decimal_places = Column(Integer, default=2)
    is_active = Column(Boolean, default=True)


class FuelProduct(Base):
    __tablename__ = "fuel_products"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    country_code = Column(String(2), ForeignKey("countries.code"))
    unit = Column(String(20), default="litre")
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())


class FeatureFlag(Base):
    __tablename__ = "feature_flags"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), unique=True, nullable=False, index=True)
    enabled = Column(Boolean, default=False)
    description = Column(Text)
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


# ── Composite Indexes ──────────────────────────────────────────────────
Index("idx_user_phone", User.phone_number)
Index("idx_user_email", User.email)
Index("idx_transaction_user_date", Transaction.user_id, Transaction.created_at)
Index("idx_transaction_pump_date", Transaction.pump_id, Transaction.created_at)
Index("idx_qr_code_user", QRCode.user_id, QRCode.is_active)
