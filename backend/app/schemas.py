from __future__ import annotations

from datetime import datetime
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, EmailStr, field_validator

from app.models import KYCStatus, TransactionStatus, TransactionType, UserRole


# ── Base ───────────────────────────────────────────────────────────────
class BaseResponse(BaseModel):
    success: bool
    message: str
    data: Optional[Dict[str, Any]] = None


class ErrorDetail(BaseModel):
    code: str
    message: str


class ErrorResponse(BaseModel):
    success: bool = False
    error: ErrorDetail
    details: Optional[Dict[str, Any]] = None


# ── Auth ───────────────────────────────────────────────────────────────
class UserLogin(BaseModel):
    username: str
    password: str
    phone_number: Optional[str] = None


class Token(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int


class TokenData(BaseModel):
    user_id: Optional[int] = None
    phone_number: Optional[str] = None


class OTPRequest(BaseModel):
    phone_number: str
    otp_type: str = "login"


class OTPVerify(BaseModel):
    phone_number: str
    otp_code: str
    otp_type: str = "login"


class RegisterOTPStart(BaseModel):
    phone_number: str
    full_name: str
    role: UserRole = UserRole.CUSTOMER
    email: Optional[EmailStr] = None


class RegisterOTPComplete(BaseModel):
    phone_number: str
    otp_code: str
    full_name: str
    role: UserRole = UserRole.CUSTOMER
    email: Optional[EmailStr] = None
    password: Optional[str] = None


# ── User ───────────────────────────────────────────────────────────────
class UserBase(BaseModel):
    phone_number: str
    email: Optional[EmailStr] = None
    full_name: str


class UserCreate(UserBase):
    password: str
    confirm_password: str
    role: UserRole = UserRole.CUSTOMER

    @field_validator("confirm_password")
    @classmethod
    def passwords_match(cls, v, info):
        if "password" in info.data and v != info.data["password"]:
            raise ValueError("Passwords do not match")
        return v


class UserProfile(UserBase):
    id: int
    role: UserRole
    is_active: bool
    is_verified: bool
    date_of_birth: Optional[datetime] = None
    address: Optional[str] = None
    city: Optional[str] = None
    state: Optional[str] = None
    pincode: Optional[str] = None
    kyc_status: KYCStatus
    vehicle_number: Optional[str] = None
    vehicle_type: Optional[str] = None
    created_at: datetime

    model_config = {"from_attributes": True}


class UserUpdate(BaseModel):
    full_name: Optional[str] = None
    email: Optional[EmailStr] = None
    date_of_birth: Optional[datetime] = None
    address: Optional[str] = None
    city: Optional[str] = None
    state: Optional[str] = None
    pincode: Optional[str] = None
    vehicle_number: Optional[str] = None
    vehicle_type: Optional[str] = None


class KYCSubmission(BaseModel):
    aadhaar_number: str
    pan_number: str
    driving_license: Optional[str] = None


class AutoRechargeSettings(BaseModel):
    enabled: bool
    threshold: float
    amount: float
    payment_method: str = "razorpay"


# ── Wallet ─────────────────────────────────────────────────────────────
class WalletResponse(BaseModel):
    id: int
    balance: float
    total_recharged: float
    total_spent: float
    is_active: bool
    created_at: datetime

    model_config = {"from_attributes": True}


class WalletSummaryResponse(BaseModel):
    balance: float
    total_recharged: float
    total_spent: float
    monthly_spending: float
    recent_transactions: list["TransactionResponse"]

    model_config = {"from_attributes": True}


class WalletRecharge(BaseModel):
    amount: float
    payment_method: str = "razorpay"

    @field_validator("amount")
    @classmethod
    def validate_amount(cls, v):
        if v <= 0:
            raise ValueError("Amount must be positive")
        if v > 10000:
            raise ValueError("Maximum recharge amount is 10,000")
        return v


# ── QR ─────────────────────────────────────────────────────────────────
class QRCodeResponse(BaseModel):
    id: int
    qr_code: str
    qr_image_path: Optional[str] = None
    qr_type: str
    is_active: bool
    created_at: datetime
    expires_at: Optional[datetime] = None

    model_config = {"from_attributes": True}


class QRCodeGenerate(BaseModel):
    qr_type: str = "mobile"
    validity_hours: Optional[int] = None


class QRCodeValidateRequest(BaseModel):
    qr_data: str


# ── Petrol Pump ────────────────────────────────────────────────────────
class PetrolPumpBase(BaseModel):
    pump_name: str
    owner_name: str
    license_number: str
    address: str
    city: str
    state: str
    pincode: str
    phone_number: str
    email: Optional[EmailStr] = None

    @field_validator("email")
    @classmethod
    def empty_string_to_none(cls, v):
        return None if v == "" else v


class PetrolPumpCreate(PetrolPumpBase):
    fuel_types: List[str] = ["petrol", "diesel"]
    daily_fuel_capacity: Optional[float] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None


class PetrolPumpResponse(PetrolPumpBase):
    id: int
    is_active: bool
    is_verified: bool
    commission_rate: float
    fuel_types: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    created_at: datetime
    email: Optional[str] = None

    model_config = {"from_attributes": True}


class PetrolPumpUpdate(BaseModel):
    pump_name: Optional[str] = None
    owner_name: Optional[str] = None
    address: Optional[str] = None
    city: Optional[str] = None
    state: Optional[str] = None
    pincode: Optional[str] = None
    phone_number: Optional[str] = None
    email: Optional[EmailStr] = None
    fuel_types: Optional[List[str]] = None
    daily_fuel_capacity: Optional[float] = None


# ── Transaction ────────────────────────────────────────────────────────
class TransactionBase(BaseModel):
    amount: float
    fuel_quantity: Optional[float] = None
    fuel_type: Optional[str] = None
    fuel_rate: Optional[float] = None


class FuelPurchaseRequest(BaseModel):
    qr_code: str
    pump_id: int
    fuel_type: str
    fuel_quantity: float
    fuel_rate: float
    idempotency_key: Optional[str] = None

    @field_validator("fuel_quantity")
    @classmethod
    def validate_fuel_quantity(cls, v):
        if v <= 0:
            raise ValueError("Fuel quantity must be positive")
        if v > 100:
            raise ValueError("Maximum fuel quantity is 100 liters")
        return v

    @field_validator("fuel_rate")
    @classmethod
    def validate_fuel_rate(cls, v):
        if v <= 0:
            raise ValueError("Fuel rate must be positive")
        return v


class TransactionResponse(TransactionBase):
    id: int
    transaction_id: str
    user_id: int
    pump_id: Optional[int] = None
    transaction_type: TransactionType
    status: TransactionStatus
    commission_amount: float
    fuel_quantity: Optional[float] = None
    fuel_type: Optional[str] = None
    fuel_rate: Optional[float] = None
    created_at: datetime
    completed_at: Optional[datetime] = None
    pump_name: Optional[str] = None
    user_name: Optional[str] = None
    user_phone: Optional[str] = None
    vehicle_number: Optional[str] = None

    model_config = {"from_attributes": True}


class ReceiptResponse(BaseModel):
    receipt_number: str
    transaction_id: str
    pump_name: Optional[str] = None
    pump_address: Optional[str] = None
    customer_name: Optional[str] = None
    customer_phone: Optional[str] = None
    vehicle_number: Optional[str] = None
    fuel_type: Optional[str] = None
    fuel_quantity: Optional[float] = None
    fuel_rate: Optional[float] = None
    amount: float
    commission_amount: float = 0.0
    wallet_balance_before: Optional[float] = None
    wallet_balance_after: Optional[float] = None
    status: str
    created_at: datetime
    completed_at: Optional[datetime] = None


class TransactionHistory(BaseModel):
    transactions: List[TransactionResponse]
    total_count: int
    page: int
    page_size: int
    total_pages: int


# ── Settlement ─────────────────────────────────────────────────────────
class SettlementResponse(BaseModel):
    id: int
    settlement_id: str
    pump_id: int
    total_transactions: int
    total_amount: float
    commission_amount: float
    net_amount: float
    settlement_date: datetime
    from_date: datetime
    to_date: datetime
    is_processed: bool

    model_config = {"from_attributes": True}


# ── Dashboard ──────────────────────────────────────────────────────────
class UserDashboard(BaseModel):
    wallet_balance: float
    total_transactions: int
    total_spent: float
    recent_transactions: List[TransactionResponse]
    qr_codes: List[QRCodeResponse]


class PumpDashboard(BaseModel):
    total_transactions: int
    total_revenue: float
    total_commission: float
    transactions_today: int
    revenue_today: float
    recent_transactions: List[TransactionResponse]


class AdminDashboard(BaseModel):
    total_users: int
    total_pumps: int
    total_transactions: int
    total_revenue: float
    active_users_today: int
    transactions_today: int
    revenue_today: float


# ── Webhook ────────────────────────────────────────────────────────────
class WebhookVerifyRequest(BaseModel):
    payment_id: str
    order_id: str
    signature: str
    payment_method: str


# ── Pump Fuel Prices ──
class PumpFuelPriceResponse(BaseModel):
    id: int
    pump_id: int
    fuel_type: str
    price: float
    effective_from: datetime
    effective_to: Optional[datetime] = None
    is_active: bool
    model_config = {"from_attributes": True}


class PumpFuelPriceCreate(BaseModel):
    fuel_type: str
    price: float


# ── Pump Devices ──
class PumpDeviceResponse(BaseModel):
    id: int
    pump_id: int
    device_id: str
    device_name: str
    is_active: bool
    last_seen: Optional[datetime] = None
    created_at: datetime
    model_config = {"from_attributes": True}


class PumpDeviceRegister(BaseModel):
    device_id: str
    device_name: str


# ── Operator Shifts ──
class OperatorShiftResponse(BaseModel):
    id: int
    pump_id: int
    operator_id: int
    shift_type: str
    start_time: datetime
    end_time: Optional[datetime] = None
    status: str
    notes: Optional[str] = None
    created_at: datetime
    operator_name: Optional[str] = None
    model_config = {"from_attributes": True}


class ShiftStartRequest(BaseModel):
    shift_type: str = "morning"
    notes: Optional[str] = None


# ── User Vehicles (Multi-Vehicle) ──
class UserVehicleResponse(BaseModel):
    id: int
    user_id: int
    vehicle_number: str
    vehicle_type: Optional[str] = None
    nickname: Optional[str] = None
    is_primary: bool
    is_active: bool
    model_config = {"from_attributes": True}


class UserVehicleCreate(BaseModel):
    vehicle_number: str
    vehicle_type: Optional[str] = None
    nickname: Optional[str] = None
    is_primary: bool = False


class UserVehicleUpdate(BaseModel):
    vehicle_number: Optional[str] = None
    vehicle_type: Optional[str] = None
    nickname: Optional[str] = None
    is_primary: bool = False


# ── Pump Inventory ──
class PumpInventoryResponse(BaseModel):
    id: int
    pump_id: int
    fuel_type: str
    current_stock: float
    max_capacity: float
    last_updated: datetime
    model_config = {"from_attributes": True}


class PumpInventoryUpdate(BaseModel):
    current_stock: float
    max_capacity: Optional[float] = None


# ── Disputes ──
class DisputeResponse(BaseModel):
    id: int
    transaction_id: str
    customer_id: int
    reason: str
    description: Optional[str] = None
    status: str
    resolution_notes: Optional[str] = None
    created_at: datetime
    resolved_at: Optional[datetime] = None
    model_config = {"from_attributes": True}


class DisputeCreate(BaseModel):
    transaction_id: str
    reason: str
    description: Optional[str] = None


# ── Notifications ──
class NotificationEventResponse(BaseModel):
    id: int
    user_id: int
    notification_type: str
    title: str
    body: str
    is_read: bool
    is_sent: bool
    sent_at: Optional[datetime] = None
    read_at: Optional[datetime] = None
    created_at: datetime
    model_config = {"from_attributes": True}


# ── Support Tickets ──
class SupportTicketResponse(BaseModel):
    id: int
    user_id: int
    subject: str
    description: Optional[str] = None
    category: Optional[str] = None
    priority: str
    status: str
    assigned_to: Optional[int] = None
    resolution: Optional[str] = None
    created_at: datetime
    updated_at: Optional[datetime] = None
    resolved_at: Optional[datetime] = None
    model_config = {"from_attributes": True}


class SupportTicketCreate(BaseModel):
    subject: str
    description: Optional[str] = None
    category: str = "other"
    priority: str = "medium"


# ── Fraud Rules ──
class FraudRuleResponse(BaseModel):
    id: int
    name: str
    rule_type: str
    rule_config: str
    is_active: bool
    created_at: datetime
    model_config = {"from_attributes": True}


class FraudRuleCreate(BaseModel):
    name: str
    rule_type: str
    rule_config: str


# ── Blacklist ──
class BlacklistEntryResponse(BaseModel):
    id: int
    user_id: Optional[int] = None
    pump_id: Optional[int] = None
    reason: str
    is_active: bool
    created_at: datetime
    model_config = {"from_attributes": True}


class BlacklistCreate(BaseModel):
    user_id: Optional[int] = None
    pump_id: Optional[int] = None
    reason: str


# ── Ledger ──
class LedgerAccountResponse(BaseModel):
    id: int
    account_type: str
    account_id: Optional[int] = None
    balance: float
    currency: str
    created_at: datetime
    model_config = {"from_attributes": True}


class LedgerEntryResponse(BaseModel):
    id: int
    ledger_account_id: int
    transaction_id: Optional[str] = None
    entry_type: str
    amount: float
    balance_before: Optional[float] = None
    balance_after: Optional[float] = None
    description: Optional[str] = None
    created_at: datetime
    model_config = {"from_attributes": True}


# ── Fleet ──
class FleetAccountResponse(BaseModel):
    id: int
    company_name: str
    admin_user_id: int
    monthly_budget: float
    is_active: bool
    created_at: datetime
    model_config = {"from_attributes": True}


class FleetAccountCreate(BaseModel):
    company_name: str
    monthly_budget: float = 0.0


class FleetVehicleResponse(BaseModel):
    id: int
    fleet_id: int
    vehicle_number: str
    fuel_type: Optional[str] = None
    monthly_fuel_limit: float
    is_active: bool
    model_config = {"from_attributes": True}


class FleetVehicleCreate(BaseModel):
    vehicle_number: str
    fuel_type: Optional[str] = None
    monthly_fuel_limit: float = 0.0


class FleetDriverResponse(BaseModel):
    id: int
    fleet_id: int
    user_id: int
    daily_limit: float
    is_active: bool
    model_config = {"from_attributes": True}


class FleetDriverAdd(BaseModel):
    user_id: int
    daily_limit: float = 0.0


# ── Nearby Pump Response ──
class NearbyPumpResponse(BaseModel):
    id: int
    pump_name: str
    address: str
    city: str
    latitude: float
    longitude: float
    distance_km: float
    fuel_types: List[str] = []
    is_open: bool
    fuel_prices: List[PumpFuelPriceResponse] = []


# ── Pump Detail Response ──
class PumpDetailResponse(BaseModel):
    id: int
    pump_name: str
    owner_name: str
    address: str
    city: str
    state: str
    pincode: str
    phone_number: str
    email: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    fuel_types: str
    is_open: bool
    is_active: bool
    is_verified: bool
    fuel_prices: List[PumpFuelPriceResponse] = []
    created_at: datetime


# ── Admin Dashboard Extended ──
class AdminDashboardExtended(AdminDashboard):
    pending_pump_approvals: int = 0
    open_disputes: int = 0
    pending_refund_requests: int = 0
    open_support_tickets: int = 0
    pending_settlements: int = 0
