import logging
from typing import Any, Dict, Optional

from fastapi import HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

logger = logging.getLogger("zappay.exceptions")


class AppException(HTTPException):
    def __init__(
        self,
        status_code: int,
        message: str,
        error_code: Optional[str] = None,
        details: Optional[Dict[str, Any]] = None,
    ):
        self.error_code = error_code
        self.details = details
        super().__init__(status_code=status_code, detail=message)


class NotFoundException(AppException):
    def __init__(self, entity: str = "Resource"):
        super().__init__(
            status_code=404,
            message=f"{entity} not found",
            error_code="not_found",
        )


class UnauthorizedException(AppException):
    def __init__(self, message: str = "Not authenticated"):
        super().__init__(
            status_code=401,
            message=message,
            error_code="unauthorized",
        )


class ForbiddenException(AppException):
    def __init__(self, message: str = "Insufficient permissions"):
        super().__init__(
            status_code=403,
            message=message,
            error_code="forbidden",
        )


class BadRequestException(AppException):
    def __init__(self, message: str = "Bad request", details: Optional[Dict] = None):
        super().__init__(
            status_code=400,
            message=message,
            error_code="bad_request",
            details=details,
        )


class ConflictException(AppException):
    def __init__(self, message: str = "Resource already exists"):
        super().__init__(
            status_code=409,
            message=message,
            error_code="conflict",
        )


class PaymentException(AppException):
    def __init__(self, message: str = "Payment processing failed"):
        super().__init__(
            status_code=402,
            message=message,
            error_code="payment_error",
        )


class RateLimitException(AppException):
    def __init__(self, message: str = "Too many requests"):
        super().__init__(
            status_code=429,
            message=message,
            error_code="rate_limited",
        )


# --- Global Exception Handlers ---


def build_error_response(
    status_code: int,
    message: str,
    error_code: Optional[str] = None,
    details: Optional[Dict[str, Any]] = None,
) -> JSONResponse:
    return JSONResponse(
        status_code=status_code,
        content={
            "success": False,
            "error": {
                "code": error_code or "error",
                "message": message,
            },
            "details": details,
        },
    )


async def app_exception_handler(request: Request, exc: AppException) -> JSONResponse:
    if exc.status_code >= 500:
        logger.exception("Server error: %s", exc.detail)
    else:
        logger.warning("App exception: %s — %s", exc.status_code, exc.detail)
    return build_error_response(
        status_code=exc.status_code,
        message=str(exc.detail),
        error_code=exc.error_code,
        details=exc.details,
    )


async def http_exception_handler(request: Request, exc: StarletteHTTPException) -> JSONResponse:
    if exc.status_code >= 500:
        logger.exception("HTTP %d: %s", exc.status_code, exc.detail)
    else:
        logger.warning("HTTP %d: %s", exc.status_code, exc.detail)
    return build_error_response(
        status_code=exc.status_code,
        message=str(exc.detail),
        error_code="http_error",
    )


async def validation_exception_handler(request: Request, exc: RequestValidationError) -> JSONResponse:
    errors = []
    for err in exc.errors():
        errors.append({
            "field": " -> ".join(str(loc) for loc in err.get("loc", [])),
            "message": err.get("msg", "Invalid value"),
            "type": err.get("type", ""),
        })
    logger.warning("Validation error: %s", errors)
    return build_error_response(
        status_code=422,
        message="Validation failed",
        error_code="validation_error",
        details={"errors": errors},
    )


async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    logger.exception("Unhandled exception: %s", exc)
    return build_error_response(
        status_code=500,
        message="Internal server error",
        error_code="internal_error",
    )


def register_exception_handlers(app):
    app.add_exception_handler(AppException, app_exception_handler)
    app.add_exception_handler(StarletteHTTPException, http_exception_handler)
    app.add_exception_handler(RequestValidationError, validation_exception_handler)
    app.add_exception_handler(Exception, unhandled_exception_handler)
