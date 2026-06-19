import logging

from fastapi import APIRouter, Depends, HTTPException, Query, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session
from typing import Optional

from app.database import get_db
from app.models import Country, Currency, FuelProduct, FeatureFlag
from app.services.auth import get_current_user

logger = logging.getLogger("zappay.routers.config")

router = APIRouter(prefix="/api/v1/config", tags=["config"])
security = HTTPBearer()


def _require_admin(db, credentials):
    user = get_current_user(db, credentials.credentials)
    if user.role.value != "admin":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Admin access required")
    return user


@router.get("/countries")
async def list_countries(is_active: Optional[bool] = None):
    from app.database import SessionLocal
    db = SessionLocal()
    try:
        query = db.query(Country)
        if is_active is not None:
            query = query.filter(Country.is_active == is_active)
        countries = query.order_by(Country.name).all()
        return {"success": True, "data": countries}
    finally:
        db.close()


@router.get("/currencies")
async def list_currencies(is_active: Optional[bool] = None):
    from app.database import SessionLocal
    db = SessionLocal()
    try:
        query = db.query(Currency)
        if is_active is not None:
            query = query.filter(Currency.is_active == is_active)
        currencies = query.order_by(Currency.code).all()
        return {"success": True, "data": currencies}
    finally:
        db.close()


@router.get("/fuel-products")
async def list_fuel_products(country_code: Optional[str] = None, is_active: Optional[bool] = None):
    from app.database import SessionLocal
    db = SessionLocal()
    try:
        query = db.query(FuelProduct)
        if country_code:
            query = query.filter(FuelProduct.country_code == country_code)
        if is_active is not None:
            query = query.filter(FuelProduct.is_active == is_active)
        products = query.order_by(FuelProduct.name).all()
        return {"success": True, "data": products}
    finally:
        db.close()


@router.get("/feature-flags")
async def list_feature_flags():
    from app.database import SessionLocal
    db = SessionLocal()
    try:
        flags = db.query(FeatureFlag).all()
        return {"success": True, "data": {f.name: f.enabled for f in flags}}
    finally:
        db.close()


@router.get("/feature-flags/{name}")
async def get_feature_flag(name: str):
    from app.database import SessionLocal
    db = SessionLocal()
    try:
        flag = db.query(FeatureFlag).filter(FeatureFlag.name == name).first()
        if not flag:
            return {"success": True, "data": {"name": name, "enabled": False}}
        return {"success": True, "data": {"name": flag.name, "enabled": flag.enabled, "description": flag.description}}
    finally:
        db.close()


@router.post("/feature-flags")
async def toggle_feature_flag(
    name: str,
    enabled: bool,
    description: Optional[str] = None,
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
):
    _require_admin(db, credentials)
    flag = db.query(FeatureFlag).filter(FeatureFlag.name == name).first()
    if flag:
        flag.enabled = enabled
        if description:
            flag.description = description
    else:
        flag = FeatureFlag(name=name, enabled=enabled, description=description)
        db.add(flag)
    db.commit()
    return {"success": True, "message": f"Feature '{name}' {'enabled' if enabled else 'disabled'}"}
