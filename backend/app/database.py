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


_connect_args = {"sslmode": "require", "connect_timeout": 10} if settings.is_supabase else {"connect_timeout": 10}

engine = create_engine(
    settings.database_url_with_ssl,
    pool_pre_ping=settings.database_pool_pre_ping,
    pool_recycle=settings.database_pool_recycle,
    pool_size=settings.database_pool_size,
    max_overflow=settings.database_max_overflow,
    echo=settings.database_echo,
    connect_args=_connect_args,
)

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
