"""Phase 4 tests for pump operations endpoints."""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.database import Base
from app.models import (
    PetrolPump,
    PumpOperator,
    User,
    UserRole,
    Wallet,
)
from app.services.auth import create_access_token


def _setup_test_data(db: Session):
    customer = User(
        phone_number="9999999901",
        full_name="Test Customer",
        password_hash="hash",
        role=UserRole.CUSTOMER,
        is_active=True,
        vehicle_number="MH01AB1234",
    )
    operator = User(
        phone_number="9999999902",
        full_name="Test Operator",
        password_hash="hash",
        role=UserRole.PUMP_OPERATOR,
        is_active=True,
    )
    db.add_all([customer, operator])
    db.flush()

    wallet = Wallet(user_id=customer.id, balance=5000.0)
    db.add(wallet)

    pump = PetrolPump(
        pump_name="Test Pump",
        owner_name="Owner",
        license_number="LIC-POPS-TEST-001",
        address="123 Test St",
        city="Test City",
        state="Test State",
        pincode="123456",
        phone_number="9999999903",
        is_active=True,
        is_verified=True,
        fuel_types='["Petrol","Diesel"]',
        fuel_rates="104.50,92.30",
        commission_rate=0.02,
    )
    db.add(pump)
    db.flush()

    operator_assoc = PumpOperator(user_id=operator.id, pump_id=pump.id, is_active=True)
    db.add(operator_assoc)
    db.flush()

    db.commit()
    return {"customer": customer, "operator": operator, "wallet": wallet, "pump": pump}


def _operator_token(user):
    return create_access_token({"sub": str(user.id), "phone_number": user.phone_number})


class TestVehicleLookup:
    def test_vehicle_lookup_not_found(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        resp = client.post(
            "/api/v1/vehicle/lookup",
            json={"vehicle_number": "ZZ99XY9999"},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["found"] is False

    def test_vehicle_lookup_found(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        resp = client.post(
            "/api/v1/vehicle/lookup",
            json={"vehicle_number": "MH01AB1234"},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["found"] is True


class TestPumpSettings:
    def test_save_settings(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        resp = client.post(
            "/api/v1/settings/save",
            json={
                "pump_id": data["pump"].id,
                "fuel_types": "Petrol,Diesel,CNG",
                "fuel_rates": "106.00,94.00,80.00",
                "is_open": True,
            },
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["success"] is True
        assert body["data"]["fuel_types"] == "Petrol,Diesel,CNG"

    def test_get_settings(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        data["pump"].fuel_types = "Petrol,Diesel,CNG"
        data["pump"].fuel_rates = "106.00,94.00,80.00"
        db_session.commit()
        resp = client.get(
            f"/api/v1/settings/{data['pump'].id}",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["success"] is True
        assert "data" in body
