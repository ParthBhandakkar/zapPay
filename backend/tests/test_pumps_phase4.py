"""Phase 4 tests for pump endpoints."""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.database import Base
from app.models import (
    OperatorShift,
    PetrolPump,
    PumpDevice,
    PumpFuelPrice,
    PumpInventory,
    PumpOperator,
    ShiftStatus,
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
        license_number="LIC-PUMP-TEST-001",
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
        latitude=19.0760,
        longitude=72.8777,
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


class TestPumpDetails:
    def test_get_pump_details(self, client, db_session):
        data = _setup_test_data(db_session)
        resp = client.get(f"/api/v1/pumps/{data['pump'].id}")
        assert resp.status_code == 200
        body = resp.json()
        assert body["pump_name"] == "Test Pump"

    def test_get_nearby_pumps(self, client, db_session):
        data = _setup_test_data(db_session)
        resp = client.get("/api/v1/pumps/nearby?latitude=19.0760&longitude=72.8777&radius_km=50")
        assert resp.status_code == 200
        body = resp.json()
        assert "nearby_pumps" in body
        assert len(body["nearby_pumps"]) >= 1


class TestPumpFuelPrices:
    def test_get_fuel_prices_empty(self, client, db_session):
        data = _setup_test_data(db_session)
        resp = client.get(f"/api/v1/pumps/{data['pump'].id}/fuel-prices")
        assert resp.status_code == 200
        body = resp.json()
        assert body["fuel_prices"] == []

    def test_add_fuel_price(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        resp = client.post(
            f"/api/v1/pumps/{data['pump'].id}/fuel-prices",
            json={"fuel_type": "Petrol", "price": 106.50},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True


class TestPumpDevices:
    def test_register_device(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        resp = client.post(
            f"/api/v1/pumps/{data['pump'].id}/devices/register",
            json={"device_id": "DEV-001", "device_name": "Pump Terminal 1"},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True

    def test_get_devices(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        device = PumpDevice(pump_id=data["pump"].id, device_id="DEV-001", device_name="Pump Terminal 1")
        db_session.add(device)
        db_session.commit()
        resp = client.get(
            f"/api/v1/pumps/{data['pump'].id}/devices",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert "devices" in body
        assert len(body["devices"]) >= 1


class TestPumpShifts:
    def test_start_shift(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        resp = client.post(
            f"/api/v1/pumps/{data['pump'].id}/shifts/start",
            json={"shift_type": "morning"},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["success"] is True

    def test_end_shift(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        from datetime import datetime
        shift = OperatorShift(
            pump_id=data["pump"].id,
            operator_id=data["operator"].id,
            shift_type="morning",
            start_time=datetime.utcnow(),
            status=ShiftStatus.ACTIVE,
        )
        db_session.add(shift)
        db_session.commit()
        resp = client.post(
            f"/api/v1/pumps/{data['pump'].id}/shifts/end",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True

    def test_get_shifts(self, client, db_session):
        data = _setup_test_data(db_session)
        from datetime import datetime
        shift = OperatorShift(
            pump_id=data["pump"].id,
            operator_id=data["operator"].id,
            shift_type="morning",
            start_time=datetime.utcnow(),
            status=ShiftStatus.COMPLETED,
        )
        db_session.add(shift)
        db_session.commit()
        resp = client.get(f"/api/v1/pumps/{data['pump'].id}/shifts")
        assert resp.status_code == 200
        body = resp.json()
        assert "shifts" in body
        assert len(body["shifts"]) >= 1


class TestPumpInventory:
    def test_get_inventory(self, client, db_session):
        data = _setup_test_data(db_session)
        inv = PumpInventory(pump_id=data["pump"].id, fuel_type="Petrol", current_stock=5000.0, max_capacity=10000.0)
        db_session.add(inv)
        db_session.commit()
        resp = client.get(f"/api/v1/pumps/{data['pump'].id}/inventory")
        assert resp.status_code == 200
        body = resp.json()
        assert "inventory" in body
        assert len(body["inventory"]) >= 1

    def test_update_inventory(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _operator_token(data["operator"])
        resp = client.post(
            f"/api/v1/pumps/{data['pump'].id}/inventory?fuel_type=Petrol&current_stock=8000.0&max_capacity=10000.0",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True
