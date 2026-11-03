"""Phase 4 tests for admin endpoints."""
from datetime import datetime, timedelta
from uuid import uuid4

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.database import Base
from app.models import (
    AuditEvent,
    BlacklistEntry,
    Dispute,
    DisputeStatus,
    FleetAccount,
    FleetDriver,
    FleetVehicle,
    FraudRule,
    PetrolPump,
    PumpOperator,
    RefundRequest,
    RefundStatus,
    Settlement,
    SettlementStatus,
    SupportTicket,
    TicketStatus,
    TicketPriority,
    Transaction,
    TransactionStatus,
    TransactionType,
    User,
    UserRole,
    Wallet,
)
from app.services.auth import create_access_token


def _setup_admin_test_data(db: Session):
    admin = User(
        phone_number="9999999900",
        full_name="Admin User",
        password_hash="hash",
        role=UserRole.ADMIN,
        is_active=True,
    )
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
    db.add_all([admin, customer, operator])
    db.flush()

    wallet = Wallet(user_id=customer.id, balance=5000.0)
    db.add(wallet)
    db.flush()

    pump = PetrolPump(
        pump_name="Test Pump",
        owner_name="Owner",
        license_number="LIC-ADMIN-TEST-001",
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

    txn = Transaction(
        transaction_id=f"TXN-ADMIN-{uuid4().hex[:8].upper()}",
        user_id=customer.id,
        wallet_id=wallet.id,
        pump_id=pump.id,
        transaction_type=TransactionType.FUEL_PURCHASE,
        amount=500.0,
        fuel_quantity=5.0,
        fuel_type="Petrol",
        fuel_rate=100.0,
        commission_amount=10.0,
        status=TransactionStatus.COMPLETED,
    )
    db.add(txn)
    db.flush()

    settlement = Settlement(
        settlement_id=f"SETT-ADMIN-{uuid4().hex[:8].upper()}",
        pump_id=pump.id,
        total_transactions=10,
        total_amount=5000.0,
        commission_amount=100.0,
        net_amount=4900.0,
        from_date=datetime.utcnow() - timedelta(days=7),
        to_date=datetime.utcnow(),
        status=SettlementStatus.PENDING,
    )
    db.add(settlement)

    dispute = Dispute(
        transaction_id=txn.transaction_id,
        customer_id=customer.id,
        reason="Incorrect amount charged",
        status=DisputeStatus.OPEN,
    )
    db.add(dispute)

    fraud_rule = FraudRule(
        name="High-value transaction alert",
        rule_type="amount_threshold",
        rule_config='{"min_amount": 10000}',
        created_by=admin.id,
    )
    db.add(fraud_rule)

    ticket = SupportTicket(
        user_id=customer.id,
        subject="Need help",
        description="Please assist",
        category="other",
        priority=TicketPriority.MEDIUM,
        status=TicketStatus.OPEN,
    )
    db.add(ticket)

    refund_req = RefundRequest(
        transaction_id=txn.transaction_id,
        user_id=customer.id,
        requested_by=customer.id,
        reason="Customer requested refund",
        status=RefundStatus.REQUESTED,
    )
    db.add(refund_req)

    fleet = FleetAccount(
        company_name="Test Fleet Corp",
        admin_user_id=admin.id,
        monthly_budget=50000.0,
    )
    db.add(fleet)
    db.flush()

    db.commit()
    return {
        "admin": admin,
        "customer": customer,
        "operator": operator,
        "wallet": wallet,
        "pump": pump,
        "transaction": txn,
        "settlement": settlement,
        "dispute": dispute,
        "fraud_rule": fraud_rule,
        "ticket": ticket,
        "refund_request": refund_req,
        "fleet": fleet,
    }


def _admin_token(user):
    return create_access_token({"sub": str(user.id), "phone_number": user.phone_number})


class TestAdminDashboard:
    def test_get_extended_dashboard(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get("/api/v1/admin/dashboard/extended", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "total_users" in body
        assert "total_pumps" in body
        assert "total_transactions" in body
        assert body["open_disputes"] >= 1
        assert body["pending_refund_requests"] >= 1
        assert body["open_support_tickets"] >= 1
        assert body["pending_settlements"] >= 1


class TestAdminDisputes:
    def test_get_disputes(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get("/api/v1/admin/disputes", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "disputes" in body
        assert len(body["disputes"]) >= 1


class TestAdminFraudRules:
    def test_get_fraud_rules(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get("/api/v1/admin/fraud-rules", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "fraud_rules" in body
        assert len(body["fraud_rules"]) >= 1

    def test_create_fraud_rule(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.post(
            "/api/v1/admin/fraud-rules",
            json={"name": "New Rule", "rule_type": "frequency", "rule_config": '{"max_per_hour": 5}'},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True


class TestAdminBlacklist:
    def test_get_blacklist_empty(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get("/api/v1/admin/blacklist", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "blacklist" in body
        assert body["blacklist"] == []

    def test_add_blacklist_entry(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.post(
            "/api/v1/admin/blacklist",
            json={"user_id": data["customer"].id, "reason": "Suspicious activity"},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True


class TestAdminSupportTickets:
    def test_get_support_tickets_admin(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get("/api/v1/admin/support-tickets", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "tickets" in body

    def test_assign_support_ticket(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.post(
            f"/api/v1/admin/support-tickets/{data['ticket'].id}/assign?assignee_id={data['admin'].id}",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True

    def test_resolve_support_ticket(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.post(
            f"/api/v1/admin/support-tickets/{data['ticket'].id}/resolve?resolution=Fixed",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True


class TestAdminRefunds:
    def test_get_refund_requests(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get("/api/v1/admin/refund-requests", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "refund_requests" in body
        assert len(body["refund_requests"]) >= 1


class TestAdminSettlements:
    def test_get_settlements(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get("/api/v1/admin/settlements", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "settlements" in body
        assert len(body["settlements"]) >= 1

    def test_update_settlement_status(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.post(
            f"/api/v1/admin/settlements/{data['settlement'].id}/update-status?new_status=paid",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True


class TestAdminAuditLogs:
    def test_get_audit_logs(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get("/api/v1/admin/audit-logs", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "events" in body


class TestAdminFleet:
    def test_get_fleet_accounts(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get("/api/v1/admin/fleet-accounts", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "fleet_accounts" in body
        assert len(body["fleet_accounts"]) >= 1

    def test_create_fleet_account(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.post(
            "/api/v1/admin/fleet-accounts",
            json={"company_name": "New Fleet Co", "monthly_budget": 25000.0},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True

    def test_get_fleet_detail(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.get(
            f"/api/v1/admin/fleet-accounts/{data['fleet'].id}",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert "account" in body
        assert "vehicles" in body
        assert "drivers" in body

    def test_add_fleet_vehicle(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.post(
            f"/api/v1/admin/fleet-accounts/{data['fleet'].id}/vehicles",
            json={"vehicle_number": "MH02CD5678", "fuel_type": "Petrol", "monthly_fuel_limit": 5000.0},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True

    def test_add_fleet_driver(self, client, db_session):
        data = _setup_admin_test_data(db_session)
        token = _admin_token(data["admin"])
        resp = client.post(
            f"/api/v1/admin/fleet-accounts/{data['fleet'].id}/drivers",
            json={"user_id": data["customer"].id, "daily_limit": 2000.0},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True
