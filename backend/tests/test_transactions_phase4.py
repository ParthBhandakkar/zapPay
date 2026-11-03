"""Phase 4 tests for transaction endpoints: receipt, detail, history, refund."""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.database import Base
from app.models import (
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
    db.flush()

    pump = PetrolPump(
        pump_name="Test Pump",
        owner_name="Owner",
        license_number="LIC-TXN-TEST-001",
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

    qr = QRCode(user_id=customer.id, qr_code="test_qr_txn", qr_type="mobile", is_active=True)
    db.add(qr)
    db.flush()

    db.commit()
    return {"customer": customer, "operator": operator, "wallet": wallet, "pump": pump, "qr": qr}


def _customer_token(user):
    return create_access_token({"sub": str(user.id), "phone_number": user.phone_number})


class TestTransactionReceipt:
    def test_get_receipt_not_found(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        resp = client.get(
            "/api/v1/transactions/NONEXISTENT/receipt",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 404


class TestTransactionDetail:
    def test_get_transaction_detail_not_found(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        resp = client.get(
            "/api/v1/transactions/NONEXISTENT",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 404


class TestTransactionHistory:
    def test_get_history_empty(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        resp = client.get(
            "/api/v1/transactions/history",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["total_count"] == 0
        assert body["transactions"] == []


class TestTransactionRefund:
    def test_request_refund_not_found(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        resp = client.post(
            "/api/v1/transactions/NONEXISTENT/refund?reason=Testing",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 404
