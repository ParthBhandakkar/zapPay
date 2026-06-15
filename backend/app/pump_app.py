"""
ZapPay Pump API — DEPRECATED.

All pump operations have been consolidated into the unified API server (`app.main`).
The unified server runs on port 8000 and serves both customer and pump endpoints.

To start the server:
    uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload

Pump-specific endpoints are available under the `/api/v1/pump-ops` tag.
"""
import logging
import warnings

warnings.warn(
    "pump_app.py is deprecated. Use app.main:app (unified server on port 8000) instead.",
    DeprecationWarning,
    stacklevel=2,
)

logger = logging.getLogger("zappay.pump_app")
logger.warning("pump_app.py imported — this module is deprecated. Use app.main instead.")
