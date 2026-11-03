"""Phase 4 tests for user endpoints: vehicles, notifications, support tickets."""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.database import Base
from app.models import (
    User,
    UserRole,
    UserVehicle,
    SupportTicket,
    TicketStatus,
    TicketPriority,
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
    db.add(customer)
    db.flush()

    wallet = Wallet(user_id=customer.id, balance=5000.0)
    db.add(wallet)
    db.flush()

    db.commit()
    return {"customer": customer, "wallet": wallet}


def _customer_token(user):
    return create_access_token({"sub": str(user.id), "phone_number": user.phone_number})


class TestUserVehicles:
    def test_get_vehicles_empty(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        resp = client.get("/api/v1/users/vehicles", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        assert resp.json() == []

    def test_add_vehicle(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        resp = client.post(
            "/api/v1/users/vehicles",
            json={"vehicle_number": "MH01AB1234", "vehicle_type": "car", "nickname": "My Car", "is_primary": True},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["success"] is True

    def test_get_vehicles_after_add(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        v = UserVehicle(user_id=data["customer"].id, vehicle_number="MH01AB1234", vehicle_type="car", is_primary=True)
        db_session.add(v)
        db_session.commit()
        resp = client.get("/api/v1/users/vehicles", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        vehicles = resp.json()
        assert len(vehicles) == 1
        assert vehicles[0]["vehicle_number"] == "MH01AB1234"

    def test_update_vehicle(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        v = UserVehicle(user_id=data["customer"].id, vehicle_number="MH01AB1234", vehicle_type="car", is_primary=True)
        db_session.add(v)
        db_session.commit()
        resp = client.put(
            f"/api/v1/users/vehicles/{v.id}",
            json={"nickname": "Updated Nickname", "is_primary": True},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True

    def test_remove_vehicle(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        v = UserVehicle(user_id=data["customer"].id, vehicle_number="MH01AB1234", vehicle_type="car", is_primary=True)
        db_session.add(v)
        db_session.commit()
        resp = client.delete(
            f"/api/v1/users/vehicles/{v.id}",
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        assert resp.json()["success"] is True
        still_there = db_session.query(UserVehicle).filter(UserVehicle.id == v.id).first()
        assert still_there is not None
        assert still_there.is_active is False


class TestUserNotifications:
    def test_get_notifications(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        resp = client.get("/api/v1/users/notifications", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "notifications" in body
        assert body["notifications"] == []


class TestUserSupportTickets:
    def test_create_support_ticket(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        resp = client.post(
            "/api/v1/users/support-tickets",
            json={"subject": "Test issue", "description": "Having a problem", "category": "other"},
            headers={"Authorization": f"Bearer {token}"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["success"] is True

    def test_get_support_tickets(self, client, db_session):
        data = _setup_test_data(db_session)
        token = _customer_token(data["customer"])
        ticket = SupportTicket(
            user_id=data["customer"].id,
            subject="Test issue",
            description="Having a problem",
            category="other",
            priority=TicketPriority.MEDIUM,
            status=TicketStatus.OPEN,
        )
        db_session.add(ticket)
        db_session.commit()
        resp = client.get("/api/v1/users/support-tickets", headers={"Authorization": f"Bearer {token}"})
        assert resp.status_code == 200
        body = resp.json()
        assert "tickets" in body
        assert len(body["tickets"]) >= 1
