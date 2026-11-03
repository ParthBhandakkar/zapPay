import logging
from typing import Generator, Optional

import redis
from sqlalchemy import create_engine, text, event
from sqlalchemy.engine import Engine
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker

from app.config import settings

logger = logging.getLogger("zappay.database")


class Base(DeclarativeBase):
    pass


@event.listens_for(Engine, "connect")
def set_sqlite_pragma(dbapi_connection, connection_record):
    if settings.database_url.startswith("sqlite"):
        cursor = dbapi_connection.cursor()
        cursor.execute("PRAGMA journal_mode=WAL")
        cursor.execute("PRAGMA foreign_keys=ON")
        cursor.execute("PRAGMA synchronous=NORMAL")
        cursor.close()


def _create_db_engine():
    db_url = settings.database_url_with_ssl
    if db_url.startswith("postgresql://"):
        db_url = db_url.replace("postgresql://", "postgresql+psycopg2://", 1)
    is_sqlite = db_url.startswith("sqlite")
    
    if not is_sqlite:
        try:
            # Test direct connection capability
            _connect_args = {"sslmode": "require", "connect_timeout": 5} if settings.is_supabase else {"connect_timeout": 5}
            eng = create_engine(
                db_url,
                pool_pre_ping=settings.database_pool_pre_ping,
                pool_recycle=settings.database_pool_recycle,
                pool_size=settings.database_pool_size,
                max_overflow=settings.database_max_overflow,
                echo=settings.database_echo,
                connect_args=_connect_args,
            )
            with eng.connect() as test_conn:
                test_conn.execute(text("SELECT 1"))
            logger.info("Connected to primary PostgreSQL/Supabase database")
            return eng
        except Exception as e:
            logger.warning("Primary database connection failed (%s). Falling back to local resilient SQLite storage.", e)
            db_url = "sqlite:///./zappay.db"
            is_sqlite = True

    # SQLite connection configuration
    return create_engine(
        db_url,
        echo=settings.database_echo,
        connect_args={"check_same_thread": False},
    )

def _migrate_schema(eng):
    try:
        with eng.connect() as conn:
            # Check transactions table columns
            if eng.dialect.name == "sqlite":
                res = conn.execute(text("PRAGMA table_info(transactions);")).fetchall()
                cols = {r[1] for r in res}
                missing_tx = {
                    "idempotency_key": "VARCHAR(100)",
                    "fuel_dispensed": "VARCHAR(50)",
                    "ocr_verified": "BOOLEAN DEFAULT 0",
                    "ocr_confidence": "FLOAT DEFAULT 1.0",
                    "soundbox_payload": "VARCHAR(255)",
                    "platform_fee": "FLOAT DEFAULT 0.0",
                    "net_dealer_amount": "FLOAT DEFAULT 0.0",
                }
                for col_name, col_type in missing_tx.items():
                    if col_name not in cols:
                        conn.execute(text(f"ALTER TABLE transactions ADD COLUMN {col_name} {col_type};"))
                
                # Check petrol_pumps table columns
                res_pumps = conn.execute(text("PRAGMA table_info(petrol_pumps);")).fetchall()
                pump_cols = {r[1] for r in res_pumps}
                missing_pumps = {
                    "mdr_fee_bps": "INTEGER DEFAULT 0",
                    "verified_petrol_density": "FLOAT DEFAULT 742.0",
                    "verified_diesel_density": "FLOAT DEFAULT 835.0",
                    "trust_score": "FLOAT DEFAULT 5.0",
                }
                for col_name, col_type in missing_pumps.items():
                    if col_name not in pump_cols:
                        conn.execute(text(f"ALTER TABLE petrol_pumps ADD COLUMN {col_name} {col_type};"))
                conn.commit()
    except Exception as e:
        logger.warning("Schema auto-migration check note: %s", e)

engine = _create_db_engine()
_migrate_schema(engine)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


# --- Redis ---
_redis_client: Optional[redis.Redis] = None


def _init_redis() -> Optional[redis.Redis]:
    global _redis_client
    try:
        pool = redis.ConnectionPool.from_url(
            settings.redis_url,
            max_connections=settings.redis_max_connections,
            socket_timeout=settings.redis_socket_timeout,
            socket_connect_timeout=settings.redis_socket_timeout,
            decode_responses=True,
            health_check_interval=30,
        )
        client = redis.Redis(connection_pool=pool)
        client.ping()
        logger.info("Redis connection established")
        return client
    except Exception as e:
        logger.warning("Redis unavailable — features degrade gracefully: %s", e)
        return None


def get_redis_client() -> Optional[redis.Redis]:
    global _redis_client
    if _redis_client is None:
        _redis_client = _init_redis()
    return _redis_client


# --- DB Dependencies ---


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def get_redis():
    client = get_redis_client()
    if client is None:
        raise RuntimeError("Redis is not available")
    return client


# --- Health Checks ---


def check_db_connection() -> bool:
    try:
        with engine.connect() as conn:
            conn.execute(text("SELECT 1"))
        return True
    except Exception as e:
        logger.error("Database health check failed: %s", e)
        return False


def check_redis_connection() -> bool:
    client = get_redis_client()
    if client is None:
        return False
    try:
        client.ping()
        return True
    except Exception as e:
        logger.warning("Redis health check failed: %s", e)
        return False
