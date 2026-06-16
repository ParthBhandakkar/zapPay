import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.config import settings
from app.database import check_db_connection, check_redis_connection
from app.exceptions import register_exception_handlers
from app.logging_config import setup_logging
from app.middleware import register_middleware
from app.realtime import connection_manager

# ── Logging (early) ────────────────────────────────────────────────────
setup_logging()
logger = logging.getLogger("zappay.main")


# ── Lifecycle ──────────────────────────────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting ZapPay API — environment=%s version=%s", settings.environment, settings.app_version)
    logger.info("Database: %s (supabase=%s)", "sqlite" if "sqlite" in settings.database_url else "postgresql", settings.is_supabase)

    yield

    logger.info("Shutting down ZapPay API")


# ── App ────────────────────────────────────────────────────────────────
app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    docs_url="/docs" if settings.debug else None,
    redoc_url="/redoc" if settings.debug else None,
    lifespan=lifespan,
    terms_of_service="https://zappay.com/terms",
    contact={
        "name": "ZapPay Support",
        "email": "support@zappay.com",
    },
    license_info={
        "name": "Proprietary",
    },
)


# ── CORS ───────────────────────────────────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins_list if not settings.debug else ["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Middleware ──────────────────────────────────────────────────────────
register_middleware(app)

# ── Exception Handlers ─────────────────────────────────────────────────
register_exception_handlers(app)


# ── Static demo frontend ───────────────────────────────────────────────
_frontend_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "frontend"))
if os.path.isdir(_frontend_dir):
    app.mount("/demo", StaticFiles(directory=_frontend_dir, html=True), name="demo")


# ── Routers ────────────────────────────────────────────────────────────
from app.routers import auth
from app.routers import wallet
from app.routers import qr
from app.routers import users
from app.routers import admin
from app.routers import webhooks
from app.routers import transactions
from app.routers import pumps
from app.routers import pump_ops

app.include_router(auth.router, prefix="/api/v1/auth", tags=["auth"])
app.include_router(wallet.router, prefix="/api/v1/wallet", tags=["wallet"])
app.include_router(qr.router, prefix="/api/v1/qr", tags=["qr"])
app.include_router(users.router, prefix="/api/v1/users", tags=["users"])
app.include_router(admin.router, prefix="/api/v1/admin", tags=["admin"])
app.include_router(webhooks.router, prefix="/api/v1/webhooks", tags=["webhooks"])
app.include_router(transactions.router, prefix="/api/v1/transactions", tags=["transactions"])
app.include_router(pumps.router, prefix="/api/v1/pumps", tags=["pumps"])
app.include_router(pump_ops.router)


# ── Health ─────────────────────────────────────────────────────────────
@app.get("/health", tags=["system"])
async def health_check():
    import asyncio
    async def check_db():
        try:
            return await asyncio.wait_for(
                asyncio.get_event_loop().run_in_executor(None, check_db_connection),
                timeout=5.0,
            )
        except asyncio.TimeoutError:
            return False

    async def check_redis():
        try:
            return await asyncio.wait_for(
                asyncio.get_event_loop().run_in_executor(None, check_redis_connection),
                timeout=3.0,
            )
        except asyncio.TimeoutError:
            return False

    db_ok, redis_ok = await asyncio.gather(check_db(), check_redis())
    status = "healthy" if db_ok else "unhealthy"
    return {
        "status": status,
        "version": settings.app_version,
        "environment": settings.environment,
        "checks": {
            "database": "pass" if db_ok else "fail",
            "redis": "pass" if redis_ok else "fail",
        },
        "uptime_seconds": None,
    }


# ── WebSocket ──────────────────────────────────────────────────────────
@app.websocket("/ws/{user_id}")
async def websocket_endpoint(websocket: WebSocket, user_id: int):
    await connection_manager.connect(user_id, websocket)
    try:
        while True:
            await websocket.receive_text()
    except WebSocketDisconnect:
        connection_manager.disconnect(user_id, websocket)
    except Exception as e:
        logger.warning("WebSocket error for user %d: %s", user_id, e)
        connection_manager.disconnect(user_id, websocket)
