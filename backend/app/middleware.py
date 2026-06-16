import logging
import time
import uuid
from typing import Callable

from fastapi import FastAPI, Request, Response
from starlette.middleware.base import BaseHTTPMiddleware

from app.config import settings
from app.exceptions import RateLimitException

logger = logging.getLogger("zappay.middleware")


from starlette.responses import JSONResponse


async def _safe_call_next(request: Request, call_next: Callable) -> Response:
    """Call the next middleware/route and return a fallback response on failure."""
    try:
        return await call_next(request)
    except Exception as exc:
        logger.warning("Unhandled exception in %s %s: %s", request.method, request.url.path, exc)
        return JSONResponse(
            status_code=500,
            content={"detail": "Internal server error"},
            headers={"X-Request-ID": getattr(request.state, "request_id", "-")},
        )


class RequestIDMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        request_id = request.headers.get("X-Request-ID") or str(uuid.uuid4())
        request.state.request_id = request_id
        response = await _safe_call_next(request, call_next)
        response.headers["X-Request-ID"] = request_id
        return response


class RequestTimingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        start = time.perf_counter()
        response = await _safe_call_next(request, call_next)
        elapsed = time.perf_counter() - start
        response.headers["X-Response-Time-Ms"] = str(int(elapsed * 1000))
        if settings.debug or elapsed > 1.0:
            logger.info(
                "TIMING  method=%s path=%s status=%d duration=%.3fs",
                request.method,
                request.url.path,
                response.status_code,
                elapsed,
            )
        return response


class RequestLoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        request_id = getattr(request.state, "request_id", "-")
        logger.info("REQ  [%s] %s %s", request_id, request.method, request.url.path)
        response = await _safe_call_next(request, call_next)
        logger.info(
            "RESP [%s] %s %s -> %d", request_id, request.method, request.url.path, response.status_code,
        )
        return response


class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        response = await _safe_call_next(request, call_next)
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["X-XSS-Protection"] = "1; mode=block"
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
        response.headers["Cache-Control"] = "no-store"
        response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
        return response


class RateLimitMiddleware(BaseHTTPMiddleware):
    def __init__(self, app: FastAPI):
        super().__init__(app)
        self._requests: dict = {}

    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        if not settings.rate_limit_enabled:
            return await _safe_call_next(request, call_next)

        client_ip = request.client.host if request.client else "unknown"
        now = time.time()
        window = settings.rate_limit_window_seconds
        limit = settings.rate_limit_requests

        if client_ip not in self._requests:
            self._requests[client_ip] = []

        self._requests[client_ip] = [t for t in self._requests[client_ip] if t > now - window]
        self._requests[client_ip].append(now)

        if len(self._requests[client_ip]) > limit:
            logger.warning("Rate limit exceeded for %s", client_ip)
            raise RateLimitException()

        return await _safe_call_next(request, call_next)


def register_middleware(app: FastAPI) -> None:
    app.add_middleware(SecurityHeadersMiddleware)
    app.add_middleware(RateLimitMiddleware)
    app.add_middleware(RequestTimingMiddleware)
    app.add_middleware(RequestLoggingMiddleware)
    app.add_middleware(RequestIDMiddleware)
