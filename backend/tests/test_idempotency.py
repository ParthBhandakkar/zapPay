"""Tests for idempotency and duplicate payment prevention."""
import hashlib
import json
import uuid

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.database import Base
from app.models import (
    IdempotencyKey,
    PetrolPump,
    PumpOperator,
    QRCode,
    Transaction,
    TransactionStatus,
    TransactionType,
    User,
    UserRole,
    Wallet,
)
from app.services.payment import (
    check_idempotency,
    IDEMPOTENCY_TTL_HOURS,
    save_idempotency,
    _request_hash,
)


def _setup_test_data(db: Session):
    """Create test users, pump, wallet, and QR code."""
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
        license_number="LIC-TEST-001",
        address="123 Test St",
        city="Test City",
        state="Test State",
        pincode="123456",
        phone_number="9999999903",
        is_active=True,
        is_verified=True,
        fuel_types="Petrol,Diesel",
        fuel_rates="104.50,92.30",
        commission_rate=0.02,
    )
    db.add(pump)
    db.flush()

    operator_assoc = PumpOperator(
        user_id=operator.id,
        pump_id=pump.id,
        is_active=True,
    )
    db.add(operator_assoc)

    qr = QRCode(
        user_id=customer.id,
        qr_code=f"test_qr_{uuid.uuid4().hex}",
        qr_type="mobile",
        is_active=True,
    )
    db.add(qr)
    db.flush()

    db.commit()
    return {
        "customer": customer,
        "operator": operator,
        "wallet": wallet,
        "pump": pump,
        "qr": qr,
    }


class TestIdempotency:
    def test_request_hash_consistency(self):
        payload = {"qr_code": "test", "pump_id": 1, "fuel_type": "Petrol", "fuel_quantity": 5.0, "fuel_rate": 104.50}
        h1 = _request_hash(payload)
        h2 = _request_hash(payload)
        assert h1 == h2

    def test_request_hash_different(self):
        p1 = {"qr_code": "test", "pump_id": 1, "fuel_type": "Petrol", "fuel_quantity": 5.0, "fuel_rate": 104.50}
        p2 = {"qr_code": "test", "pump_id": 1, "fuel_type": "Diesel", "fuel_quantity": 5.0, "fuel_rate": 104.50}
        assert _request_hash(p1) != _request_hash(p2)

    def test_check_idempotency_no_record(self, db_session):
        result = check_idempotency(db_session, "nonexistent-key", "hash")
        assert result is None

    def test_check_idempotency_conflict(self, db_session):
        key = "conflict-key"
        save_idempotency(db_session, key, "hash1", {"success": False, "message": "failed"})
        result = check_idempotency(db_session, key, "hash2")
        assert result is not None
        assert result.get("conflict") is True
        assert "different request" in result.get("message", "")

    def test_check_idempotency_cached(self, db_session):
        key = "cached-key"
        save_idempotency(db_session, key, "hash1", {"success": True, "message": "done"})
        result = check_idempotency(db_session, key, "hash1")
        assert result is not None
        assert result.get("success") is True

    def test_failed_idempotency_allows_retry(self, db_session):
        key = "retry-key"
        save_idempotency(db_session, key, "hash1", {"success": False, "message": "failed"})
        result = check_idempotency(db_session, key, "hash1")
        # Should return None to allow retry
        assert result is None

    def test_save_and_retrieve(self, db_session):
        key = "save-retrieve-key"
        response = {"success": True, "message": "ok", "transaction_id": "TST123"}
        save_idempotency(db_session, key, "reqhash", response, transaction_id="TST123")
        record = db_session.query(IdempotencyKey).filter(
            IdempotencyKey.idempotency_key == key
        ).first()
        assert record is not None
        assert record.request_hash == "reqhash"
        assert record.transaction_id == "TST123"
        assert record.status == "completed"

    def test_duplicate_idempotency_key_rejected_by_api(self, client, db_session):
        data = _setup_test_data(db_session)
        pump = data["pump"]
        qr = data["qr"]
        operator = data["operator"]

        # Login as operator
        from app.services.auth import create_access_token
        token = create_access_token(operator.id, operator.phone_number)
        headers = {"Authorization": f"Bearer {token}"}

        idem_key = str(uuid.uuid4())
        payload = {
            "qr_code": qr.qr_code,
            "pump_id": pump.id,
            "fuel_type": "Petrol",
            "fuel_quantity": 5.0,
            "fuel_rate": 104.50,
            "idempotency_key": idem_key,
        }

        # First request should succeed
        resp1 = client.post("/api/v1/transactions/fuel-purchase", json=payload, headers=headers)
        assert resp1.status_code == 200, f"First request failed: {resp1.json()}"
        data1 = resp1.json()
        assert data1["success"] is True

        # Second request with same key should return same result
        resp2 = client.post("/api/v1/transactions/fuel-purchase", json=payload, headers=headers)
        assert resp2.status_code == 200
        data2 = resp2.json()
        assert data2["success"] is True
        assert data2["data"]["transaction_id"] == data1["data"]["transaction_id"]

        # Only one transaction should exist (idempotency prevented duplicate)
        txns = db_session.query(Transaction).filter(
            Transaction.transaction_id == data1["data"]["transaction_id"],
        ).all()
        assert len(txns) == 1

    def test_different_request_with_same_key_rejected(self, client, db_session):
        data = _setup_test_data(db_session)
        pump = data["pump"]
        qr = data["qr"]
        operator = data["operator"]

        from app.services.auth import create_access_token
        token = create_access_token(operator.id, operator.phone_number)
        headers = {"Authorization": f"Bearer {token}"}

        idem_key = str(uuid.uuid4())

        payload1 = {
            "qr_code": qr.qr_code,
            "pump_id": pump.id,
            "fuel_type": "Petrol",
            "fuel_quantity": 5.0,
            "fuel_rate": 104.50,
            "idempotency_key": idem_key,
        }
        resp1 = client.post("/api/v1/transactions/fuel-purchase", json=payload1, headers=headers)
        assert resp1.status_code == 200

        # Different payload with same key
        payload2 = {
            **payload1,
            "fuel_type": "Diesel",
            "fuel_quantity": 3.0,
        }
        # This should succeed for different fuel type since it goes thru, the idempotency check is on the hash
        resp2 = client.post("/api/v1/transactions/fuel-purchase", json=payload2, headers=headers)
        assert resp2.status_code == 400
        assert "different request" in resp2.json()["detail"].lower()

    def test_purchase_without_idempotency_still_works(self, client, db_session):
        data = _setup_test_data(db_session)
        pump = data["pump"]
        qr = data["qr"]
        operator = data["operator"]

        from app.services.auth import create_access_token
        token = create_access_token(operator.id, operator.phone_number)
        headers = {"Authorization": f"Bearer {token}"}

        payload = {
            "qr_code": qr.qr_code,
            "pump_id": pump.id,
            "fuel_type": "Diesel",
            "fuel_quantity": 10.0,
            "fuel_rate": 92.30,
        }

        resp = client.post("/api/v1/transactions/fuel-purchase", json=payload, headers=headers)
        assert resp.status_code == 200
        assert resp.json()["success"] is True
        assert "transaction_id" in resp.json()["data"]
