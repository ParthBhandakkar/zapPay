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


def _set_header(response: Response, key: str, value: str) -> None:
    if response is not None:
        try:
            response.headers[key] = value
        except Exception:
            pass


class RequestIDMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        try:
            request_id = request.headers.get("X-Request-ID") or str(uuid.uuid4())
            request.state.request_id = request_id
            response = await _safe_call_next(request, call_next)
            _set_header(response, "X-Request-ID", request_id)
            return response
        except Exception as exc:
            logger.warning("RequestIDMiddleware failed: %s", exc)
            return JSONResponse(status_code=500, content={"detail": "Internal server error"})


class RequestTimingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        try:
            start = time.perf_counter()
            response = await _safe_call_next(request, call_next)
            elapsed = time.perf_counter() - start
            _set_header(response, "X-Response-Time-Ms", str(int(elapsed * 1000)))
            if settings.debug or elapsed > 1.0:
                logger.info(
                    "TIMING  method=%s path=%s status=%d duration=%.3fs",
                    request.method,
                    request.url.path,
                    response.status_code,
                    elapsed,
                )
            return response
        except Exception as exc:
            logger.warning("RequestTimingMiddleware failed: %s", exc)
            return JSONResponse(status_code=500, content={"detail": "Internal server error"})


class RequestLoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        try:
            request_id = getattr(request.state, "request_id", "-")
            logger.info("REQ  [%s] %s %s", request_id, request.method, request.url.path)
            response = await _safe_call_next(request, call_next)
            logger.info(
                "RESP [%s] %s %s -> %d", request_id, request.method, request.url.path, response.status_code,
            )
            return response
        except Exception as exc:
            logger.warning("RequestLoggingMiddleware failed: %s", exc)
            return JSONResponse(status_code=500, content={"detail": "Internal server error"})


class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        try:
            response = await _safe_call_next(request, call_next)
            _set_header(response, "X-Content-Type-Options", "nosniff")
            _set_header(response, "X-Frame-Options", "DENY")
            _set_header(response, "X-XSS-Protection", "1; mode=block")
            _set_header(response, "Strict-Transport-Security", "max-age=31536000; includeSubDomains")
            _set_header(response, "Cache-Control", "no-store")
            _set_header(response, "Referrer-Policy", "strict-origin-when-cross-origin")
            return response
        except Exception as exc:
            logger.warning("SecurityHeadersMiddleware failed: %s", exc)
            return JSONResponse(status_code=500, content={"detail": "Internal server error"})


class RateLimitMiddleware(BaseHTTPMiddleware):
    def __init__(self, app: FastAPI):
        super().__init__(app)
        self._requests: dict = {}

    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        try:
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
        except Exception as exc:
            logger.warning("RateLimitMiddleware failed: %s", exc)
            return JSONResponse(status_code=500, content={"detail": "Internal server error"})


def register_middleware(app: FastAPI) -> None:
    app.add_middleware(SecurityHeadersMiddleware)
    app.add_middleware(RateLimitMiddleware)
    app.add_middleware(RequestTimingMiddleware)
    app.add_middleware(RequestLoggingMiddleware)
    app.add_middleware(RequestIDMiddleware)
