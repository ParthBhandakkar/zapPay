from pydantic_settings import BaseSettings
from typing import Optional, List, ClassVar
import os
import warnings

class Settings(BaseSettings):
    # --- Application ---
    app_name: str = "ZapPay API"
    app_version: str = "1.0.0"
    debug: bool = False
    environment: str = "production"

    # --- Database (Supabase PostgreSQL) ---
    database_url: str = "postgresql://postgres:password@localhost:5432/postgres"
    database_pool_size: int = 5
    database_max_overflow: int = 5
    database_pool_recycle: int = 180
    database_pool_pre_ping: bool = True
    database_echo: bool = False

    # --- Redis ---
    redis_url: str = "redis://localhost:6379/0"
    redis_max_connections: int = 10
    redis_socket_timeout: int = 5

    # --- Security ---
    secret_key: str = ""
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 30
    refresh_token_expire_days: int = 7
    jwt_issuer: str = "zappay-api"
    jwt_audience: Optional[str] = None

    # --- Payment Gateways ---
    razorpay_key_id: Optional[str] = None
    razorpay_key_secret: Optional[str] = None
    stripe_secret_key: Optional[str] = None
    stripe_webhook_secret: Optional[str] = None

    # --- QR Encryption (Fernet) ---
    qr_encryption_key: Optional[str] = None

    # --- File Upload ---
    upload_dir: str = "uploads"
    max_file_size: int = 10 * 1024 * 1024

    # --- SMS ---
    sms_api_key: Optional[str] = None
    sms_sender_id: str = "ZAPPAY"
    sms_provider: str = "fast2sms"
    twilio_account_sid: Optional[str] = None
    twilio_auth_token: Optional[str] = None
    twilio_phone_number: Optional[str] = None

    # --- Email (SMTP) ---
    email_from: str = "noreply@zappay.com"
    smtp_host: str = "smtp.gmail.com"
    smtp_port: int = 587
    smtp_username: Optional[str] = None
    smtp_password: Optional[str] = None
    smtp_tls: bool = True

    # --- Business ---
    commission_rate: float = 0.02
    minimum_balance: float = 100.0
    maximum_transaction: float = 10000.0

    # --- CORS ---
    cors_origins: str = "http://localhost:3000,http://localhost:5173"

    # --- Rate Limiting ---
    rate_limit_enabled: bool = True
    rate_limit_requests: int = 100
    rate_limit_window_seconds: int = 60

    # --- Monitoring ---
    sentry_dsn: Optional[str] = None
    otel_service_name: str = "zappay-api"

    @property
    def cors_origins_list(self) -> List[str]:
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()]

    @property
    def is_supabase(self) -> bool:
        return "supabase.co" in self.database_url.lower() or "pooler.supabase.com" in self.database_url.lower()

    @property
    def database_url_with_ssl(self) -> str:
        url = self.database_url
        if self.is_supabase and "sslmode" not in url:
            separator = "&" if "?" in url else "?"
            url = f"{url}{separator}sslmode=require"
        return url

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False
        extra = "ignore"

    def validate_production(self):
        if self.debug:
            warnings.warn("DEBUG mode enabled — disable in production")
        if not self.secret_key or len(self.secret_key) < 32:
            warnings.warn("SECRET_KEY is too short or missing — set a strong key (≥32 chars)")
        if self.environment == "production" and "sqlite" in self.database_url.lower():
            warnings.warn("Using SQLite in production! Set DATABASE_URL to PostgreSQL")


settings = Settings()
settings.validate_production()

os.makedirs(settings.upload_dir, exist_ok=True)
os.makedirs(os.path.join(settings.upload_dir, "qr_codes"), exist_ok=True)
