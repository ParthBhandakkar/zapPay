import logging
import time
import traceback
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
        logger.warning(
            "Unhandled exception in %s %s: %s\n%s",
            request.method, request.url.path, exc, traceback.format_exc(),
        )
        return JSONResponse(
            status_code=500,
            content={"detail": "Internal server error"},
            headers={"X-Request-ID": getattr(request.state, "request_id", "-")},
        )


def _set_header_safe(response: Response | None, key: str, value: str) -> None:
    """Set a response header, safely handling None or already-sent responses."""
    if response is not None:
        try:
            response.headers[key] = value
        except Exception as exc:
            logger.warning("Failed to set header %s: %s", key, exc)


async def _dispatch_safe(
    request: Request,
    call_next: Callable,
    handler: Callable,
) -> Response:
    """Wrap a dispatch handler to ensure it always returns a Response.

    HTTPException subclasses (e.g. RateLimitException) are re-raised so they
    can be handled by FastAPI's exception handlers. All other exceptions are
    caught and turned into a 500 fallback.
    """
    from starlette.exceptions import HTTPException as StarletteHTTPException
    try:
        return await handler(request, call_next)
    except StarletteHTTPException:
        raise
    except Exception as exc:
        logger.warning(
            "Dispatch handler failed for %s %s: %s\n%s",
            request.method, request.url.path, exc, traceback.format_exc(),
        )
        return JSONResponse(
            status_code=500,
            content={"detail": "Internal server error"},
            headers={"X-Request-ID": getattr(request.state, "request_id", "-")},
        )


class RequestIDMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        async def _handler(req: Request, cn: Callable) -> Response:
            request_id = req.headers.get("X-Request-ID") or str(uuid.uuid4())
            req.state.request_id = request_id
            response = await _safe_call_next(req, cn)
            response.headers["X-Request-ID"] = request_id
            return response
        return await _dispatch_safe(request, call_next, _handler)


class RequestTimingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        async def _handler(req: Request, cn: Callable) -> Response:
            start = time.perf_counter()
            response = await _safe_call_next(req, cn)
            elapsed = time.perf_counter() - start
            _set_header_safe(response, "X-Response-Time-Ms", str(int(elapsed * 1000)))
            if settings.debug or elapsed > 1.0:
                logger.info(
                    "TIMING  method=%s path=%s status=%d duration=%.3fs",
                    req.method, req.url.path, response.status_code, elapsed,
                )
            return response
        return await _dispatch_safe(request, call_next, _handler)


class RequestLoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        async def _handler(req: Request, cn: Callable) -> Response:
            request_id = getattr(req.state, "request_id", "-")
            logger.info("REQ  [%s] %s %s", request_id, req.method, req.url.path)
            response = await _safe_call_next(req, cn)
            logger.info(
                "RESP [%s] %s %s -> %d", request_id, req.method, req.url.path, response.status_code,
            )
            return response
        return await _dispatch_safe(request, call_next, _handler)


class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        async def _handler(req: Request, cn: Callable) -> Response:
            response = await _safe_call_next(req, cn)
            _set_header_safe(response, "X-Content-Type-Options", "nosniff")
            _set_header_safe(response, "X-Frame-Options", "DENY")
            _set_header_safe(response, "X-XSS-Protection", "1; mode=block")
            _set_header_safe(response, "Strict-Transport-Security", "max-age=31536000; includeSubDomains")
            _set_header_safe(response, "Cache-Control", "no-store")
            _set_header_safe(response, "Referrer-Policy", "strict-origin-when-cross-origin")
            return response
        return await _dispatch_safe(request, call_next, _handler)


class RateLimitMiddleware(BaseHTTPMiddleware):
    def __init__(self, app: FastAPI):
        super().__init__(app)
        self._requests: dict = {}

    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        async def _handler(req: Request, cn: Callable) -> Response:
            if not settings.rate_limit_enabled:
                return await _safe_call_next(req, cn)

            client_ip = req.client.host if req.client else "unknown"
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

            return await _safe_call_next(req, cn)
        return await _dispatch_safe(request, call_next, _handler)


def register_middleware(app: FastAPI) -> None:
    app.add_middleware(SecurityHeadersMiddleware)
    app.add_middleware(RateLimitMiddleware)
    app.add_middleware(RequestTimingMiddleware)
    app.add_middleware(RequestLoggingMiddleware)
    app.add_middleware(RequestIDMiddleware)
