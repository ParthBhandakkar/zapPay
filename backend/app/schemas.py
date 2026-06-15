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
    phone_number: str
    password: str


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
    created_at: datetime
    completed_at: Optional[datetime] = None

    model_config = {"from_attributes": True}


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
