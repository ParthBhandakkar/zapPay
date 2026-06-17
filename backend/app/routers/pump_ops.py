import logging
import base64
import io
import json
import os
import re
import tempfile
import threading
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Request, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from PIL import Image, ImageEnhance
from sqlalchemy.orm import Session

from app.config import settings
from app.database import get_db, get_redis
from app.exceptions import NotFoundException, UnauthorizedException
from app.models import User, UserRole, PetrolPump
from app.schemas import BaseResponse, FuelPurchaseRequest
from app.services.auth import verify_token
from app.services.payment import process_fuel_purchase, process_fuel_purchase_by_user
from app.services.qr_service import validate_qr_code

logger = logging.getLogger("zappay.routers.pump_ops")

router = APIRouter(prefix="/api/v1", tags=["pump-ops"])
security = HTTPBearer(auto_error=False)


def require_pump_operator(db: Session, credentials: Optional[HTTPAuthorizationCredentials]) -> User:
    if not credentials:
        raise UnauthorizedException("Authentication required")
    token_data = verify_token(
        credentials.credentials,
        HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Could not validate credentials"),
    )
    user = db.query(User).filter(User.id == token_data.user_id).first()
    if not user:
        raise UnauthorizedException("User not found")
    role = user.role.value if hasattr(user.role, "value") else str(user.role)
    allowed = {UserRole.PUMP_OPERATOR.value, UserRole.PUMP_OWNER.value, UserRole.ADMIN.value}
    if role not in allowed:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Pump operator access required")
    return user


# --- EasyOCR (lazy-loaded, thread-safe) ---
_ocr_lock = threading.Lock()
_ocr_reader = None


def _get_ocr_reader():
    global _ocr_reader
    if _ocr_reader is None:
        with _ocr_lock:
            if _ocr_reader is None:
                import easyocr
                logger.info("Initializing EasyOCR reader (first load)...")
                _ocr_reader = easyocr.Reader(["en"], gpu=False, verbose=False)
                logger.info("EasyOCR reader ready")
    return _ocr_reader


def _preload_ocr():
    try:
        _get_ocr_reader()
    except Exception as e:
        logger.warning("Failed to preload OCR: %s", e)


# --- Endpoints ---


@router.post("/vehicle/lookup")
async def lookup_vehicle(
    request: Request,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    require_pump_operator(db, credentials)
    body = await request.json()
    vehicle_number = body.get("vehicle_number")
    if not vehicle_number:
        raise HTTPException(status_code=400, detail="vehicle_number required")

    normalized = vehicle_number.upper().replace(" ", "").replace("-", "")
    logger.info("Looking up vehicle: %s -> %s", vehicle_number, normalized)

    users = db.query(User).filter(
        User.vehicle_number.isnot(None),
        User.is_active == True,
    ).all()

    matched = None
    for u in users:
        if u.vehicle_number:
            un = u.vehicle_number.upper().replace(" ", "").replace("-", "")
            if un == normalized:
                matched = u
                break

    if not matched:
        return {"found": False, "message": "No user found with this vehicle number"}

    return {
        "found": True,
        "user_id": matched.id,
        "user_name": matched.full_name,
        "user_phone": matched.phone_number,
        "wallet_balance": matched.wallet.balance if matched.wallet else 0.0,
        "vehicle_number": matched.vehicle_number,
        "vehicle_type": matched.vehicle_type,
    }


@router.post("/ocr/number-plate")
async def recognize_number_plate(
    request: Request,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    require_pump_operator(db, credentials)
    body = await request.json()
    image_data = body.get("image")
    if not image_data:
        raise HTTPException(status_code=400, detail="image (base64) required")

    if "," in image_data:
        image_data = image_data.split(",")[1]

    try:
        image_bytes = base64.b64decode(image_data)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Invalid base64: {e}")

    img = Image.open(io.BytesIO(image_bytes))
    if img.width > 800:
        ratio = 800.0 / img.width
        img = img.resize((800, int(img.height * ratio)), Image.LANCZOS)
    img = img.convert("L")
    img = ImageEnhance.Contrast(img).enhance(2.0)

    tmp_path = None
    try:
        fd, tmp_path = tempfile.mkstemp(suffix=".jpg")
        os.close(fd)
        img.save(tmp_path, format="JPEG", quality=85)

        reader = _get_ocr_reader()
        results = reader.readtext(tmp_path, detail=1, paragraph=False)
        all_text = " ".join([t for _, t, _ in results])
        logger.info("OCR raw text: %s", all_text)

        plates = []
        patterns = [
            r"[A-Z]{2}\s*\d{1,2}\s*[A-Z]{1,3}\s*\d{1,4}",
            r"[A-Z]{2}\d{1,2}[A-Z]{1,3}\d{1,4}",
        ]
        for pat in patterns:
            for m in re.findall(pat, all_text.upper()):
                n = m.upper().replace(" ", "").replace("-", "")
                if 8 <= len(n) <= 12 and n not in plates:
                    plates.append(n)

        for _, t, _ in results:
            n = t.upper().replace(" ", "").replace("-", "").replace(".", "")
            if 8 <= len(n) <= 12 and n.isalnum() and n not in plates:
                if any(c.isalpha() for c in n) and any(c.isdigit() for c in n):
                    plates.append(n)

        return {
            "success": True,
            "plates": plates,
            "raw_text": all_text,
            "message": f"Found {len(plates)} plate(s)" if plates else "No plates detected",
        }
    finally:
        if tmp_path:
            try:
                os.unlink(tmp_path)
            except Exception:
                pass


@router.post("/qr/validate")
async def validate_qr(
    request: Request,
    db: Session = Depends(get_db),
):
    body = await request.json()
    qr_data = body.get("qr_data")
    if not qr_data:
        raise HTTPException(status_code=400, detail="qr_data required")

    result = validate_qr_code(db, qr_data)
    if not result:
        raise HTTPException(status_code=400, detail="Invalid or expired QR code")

    user, qr_record = result
    return {
        "valid": True,
        "user_id": user.id,
        "user_name": user.full_name,
        "user_phone": user.phone_number,
        "qr_code_id": qr_record.id,
        "qr_type": qr_record.qr_type,
        "wallet_balance": user.wallet.balance if user.wallet else 0.0,
        "vehicle_number": user.vehicle_number,
        "vehicle_type": user.vehicle_type,
    }



@router.post("/transactions/fuel-purchase-by-vehicle")
async def purchase_fuel_by_vehicle(
    request: Request,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    require_pump_operator(db, credentials)
    body = await request.json()
    user_id = body.get("user_id")
    pump_id = body.get("pump_id")
    fuel_type = body.get("fuel_type")
    fuel_quantity = body.get("fuel_quantity")
    fuel_rate = body.get("fuel_rate")

    if not all([user_id, pump_id, fuel_type, fuel_quantity, fuel_rate]):
        raise HTTPException(status_code=400, detail="Missing required fields")

    result = process_fuel_purchase_by_user(
        db=db,
        user_id=int(user_id),
        pump_id=int(pump_id),
        fuel_type=fuel_type,
        fuel_quantity=float(fuel_quantity),
        fuel_rate=float(fuel_rate),
    )
    if not result["success"]:
        raise HTTPException(status_code=400, detail=result["message"])
    return BaseResponse(success=True, message=result["message"], data=result)


@router.get("/operators/profile")
async def get_operator_profile(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    return require_pump_operator(db, credentials)


@router.post("/settings/save")
async def save_pump_settings(
    payload: dict,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    require_pump_operator(db, credentials)
    pump_id = payload.get("pump_id")
    if not pump_id:
        raise HTTPException(status_code=400, detail="pump_id required")

    pump_name = str(payload.get("pump_name") or "")
    petrol_price = str(payload.get("petrol_price") or "0")
    diesel_price = str(payload.get("diesel_price") or "0")

    try:
        r = get_redis()
        key = f"pump_settings:{pump_id}"
        r.hset(key, "pump_name", pump_name)
        r.hset(key, "petrol_price", petrol_price)
        r.hset(key, "diesel_price", diesel_price)
    except Exception as e:
        logger.warning("Redis unavailable for pump settings: %s", e)

    return {
        "success": True,
        "message": "Settings saved",
        "data": {"pump_id": pump_id, "pump_name": pump_name, "petrol_price": petrol_price, "diesel_price": diesel_price},
    }


@router.get("/settings/{pump_id}")
async def get_pump_settings(
    pump_id: int,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    require_pump_operator(db, credentials)
    try:
        r = get_redis()
        key = f"pump_settings:{pump_id}"
        data = r.hgetall(key) or {}
        return {
            "success": True,
            "message": "Settings loaded",
            "data": {
                "pump_id": pump_id,
                "pump_name": data.get("pump_name", ""),
                "petrol_price": data.get("petrol_price", "0"),
                "diesel_price": data.get("diesel_price", "0"),
            },
        }
    except Exception as e:
        logger.warning("Redis unavailable for pump settings: %s", e)
        return {
            "success": True,
            "message": "Settings loaded (from defaults)",
            "data": {"pump_id": pump_id, "pump_name": "", "petrol_price": "0", "diesel_price": "0"},
        }
