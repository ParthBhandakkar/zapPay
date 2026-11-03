"""Phase 4 tests for config endpoints.

The config router uses SessionLocal() directly (not get_db dependency override).
We seed test data at the session level using the same SQLite engine so that
SessionLocal() picks up the records.
"""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

from app.database import Base
from app.models import Country, Currency, FuelProduct, FeatureFlag


def _seed_config_data(db: Session):
    country = Country(code="IN", name="India", phone_code="+91", currency_code="INR", is_active=True)
    db.add(country)
    db.flush()

    currency = Currency(code="INR", name="Indian Rupee", symbol="₹", decimal_places=2, is_active=True)
    db.add(currency)

    fuel = FuelProduct(name="Petrol", country_code="IN", unit="litre", is_active=True)
    db.add(fuel)

    flag = FeatureFlag(name="enable_wallet", enabled=True, description="Wallet feature")
    db.add(flag)
    db.commit()


@pytest.fixture(scope="module")
def config_test_data(db_engine):
    """Seed config data once per module so SessionLocal() can see it."""
    Session = sessionmaker(bind=db_engine)
    session = Session()
    _seed_config_data(session)
    session.close()


class TestConfigCountries:
    def test_get_countries(self, client, config_test_data):
        resp = client.get("/api/v1/config/countries")
        assert resp.status_code == 200
        body = resp.json()
        assert body["success"] is True
        assert len(body["data"]) >= 1
        codes = [c["code"] for c in body["data"]]
        assert "IN" in codes


class TestConfigCurrencies:
    def test_get_currencies(self, client, config_test_data):
        resp = client.get("/api/v1/config/currencies")
        assert resp.status_code == 200
        body = resp.json()
        assert body["success"] is True
        assert len(body["data"]) >= 1
        codes = [c["code"] for c in body["data"]]
        assert "INR" in codes


class TestConfigFuelProducts:
    def test_get_fuel_products(self, client, config_test_data):
        resp = client.get("/api/v1/config/fuel-products")
        assert resp.status_code == 200
        body = resp.json()
        assert body["success"] is True
        assert len(body["data"]) >= 1
        names = [p["name"] for p in body["data"]]
        assert "Petrol" in names


class TestConfigFeatureFlags:
    def test_get_feature_flags(self, client, config_test_data):
        resp = client.get("/api/v1/config/feature-flags")
        assert resp.status_code == 200
        body = resp.json()
        assert body["success"] is True
        assert "enable_wallet" in body["data"]
        assert body["data"]["enable_wallet"] is True
