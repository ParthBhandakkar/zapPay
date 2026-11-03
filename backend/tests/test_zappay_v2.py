import pytest
from app.database import Base, engine

@pytest.fixture(autouse=True)
def setup_db(db_session):
    # Ensure tables are created
    pass

def test_health(client):
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert "status" in data


def test_forecourt_shift_login(client):
    res = client.post("/api/v1/forecourt/shift-login", json={
        "phone_number": "9827112233",
        "pin": "1234",
        "station_id": 1,
    })
    assert res.status_code == 200
    data = res.json()
    assert data["success"] is True
    assert "token" in data
    assert data["role"] == "PUMP_OPERATOR"


def test_forecourt_scan_vehicle(client):
    res = client.post("/api/v1/forecourt/scan-vehicle", json={
        "qr_public_token": "zp_cg_4582",
        "station_id": 1,
    })
    assert res.status_code == 200
    data = res.json()
    assert data["verified"] is True
    assert data["vrn_number"] == "CG-10-AB-4582"
    assert data["permitted_fuel_type"] == "PETROL"
    assert "PETROL ONLY" in data["fuel_lock_message"]


def test_forecourt_verify_meter_ocr(client):
    # Math match: 10 Litres * ₹100.50 = ₹1005.00
    res_match = client.post("/api/v1/forecourt/verify-meter-ocr", json={
        "volume_litres": 10.0,
        "amount": 1005.0,
        "fuel_rate": 100.50,
        "density": 742.5,
    })
    assert res_match.status_code == 200
    data_match = res_match.json()
    assert data_match["math_verified"] is True
    assert data_match["status"] == "MATCHED"

    # Math mismatch
    res_mismatch = client.post("/api/v1/forecourt/verify-meter-ocr", json={
        "volume_litres": 10.0,
        "amount": 1200.0,
        "fuel_rate": 100.50,
    })
    assert res_mismatch.status_code == 200
    data_mismatch = res_mismatch.json()
    assert data_mismatch["math_verified"] is False
    assert data_mismatch["status"] == "DISCREPANCY_FLAGGED"


def test_forecourt_execute_debit_and_soundbox(client):
    # First ensure scan-vehicle is called to seed tag & vehicle
    client.post("/api/v1/forecourt/scan-vehicle", json={
        "qr_public_token": "zp_cg_4582",
        "station_id": 1,
    })

    # Attempting to dispense DIESEL to a PETROL car must be BLOCKED!
    res_blocked = client.post("/api/v1/forecourt/execute-debit", json={
        "qr_public_token": "zp_cg_4582",
        "fuel_dispensed": "DIESEL",
        "volume_litres": 5.0,
        "fuel_rate": 90.0,
        "amount": 450.0,
    })
    assert res_blocked.status_code == 400
    assert "FUEL MISMATCH INTERCEPTED" in res_blocked.text

    # Dispensing correct fuel (PETROL)
    res_success = client.post("/api/v1/forecourt/execute-debit", json={
        "qr_public_token": "zp_cg_4582",
        "fuel_dispensed": "PETROL",
        "volume_litres": 5.0,
        "fuel_rate": 100.0,
        "amount": 500.0,
        "density": 742.5,
    })
    assert res_success.status_code == 200
    data = res_success.json()
    assert data["status"] == "SETTLED"
    assert "4582" in data["soundbox_payload"]
    assert "ZapPay" in data["soundbox_payload"]
    assert data["gross_amount"] == 500.0


def test_consumer_vault_and_mandates(client):
    # Vehicles Vault
    res_v = client.get("/api/v1/consumer/vehicles")
    assert res_v.status_code == 200
    vehicles = res_v.json()["vehicles"]
    assert len(vehicles) >= 1
    assert vehicles[0]["vahan_verified"] is True
    assert vehicles[0]["insurance_status"] == "VALID"

    # UPI Autopay & UPI Circle Mandates
    res_m = client.get("/api/v1/consumer/mandates")
    assert res_m.status_code == 200
    mandates = res_m.json()["mandates"]
    assert len(mandates) >= 2


def test_mileage_and_stations(client):
    # Ensure vehicle exists
    res_v = client.get("/api/v1/consumer/vehicles")
    assert res_v.status_code == 200
    v_id = res_v.json()["vehicles"][0]["id"]

    res_s = client.get("/api/v1/consumer/stations/nearby")
    assert res_s.status_code == 200
    assert len(res_s.json()["stations"]) >= 2
    s_id = res_s.json()["stations"][0]["id"]

    res_log = client.post("/api/v1/consumer/mileage-log", json={
        "vehicle_id": v_id,
        "station_id": s_id,
        "odometer_km": 14380,
        "litres_filled": 10.0,
    })
    assert res_log.status_code == 200
    assert res_log.json()["computed_kmpl"] > 0


def test_fleet_rules(client):
    res_org = client.get("/api/v1/fleet/organization")
    assert res_org.status_code == 200
    assert res_org.json()["master_wallet_balance"] > 0

    res_f = client.get("/api/v1/fleet/vehicles")
    assert res_f.status_code == 200
    assert len(res_f.json()["fleet_vehicles"]) >= 2


def test_public_scan_and_sos(client):
    # Seed vehicle first
    client.post("/api/v1/forecourt/scan-vehicle", json={
        "qr_public_token": "zp_cg_4582",
        "station_id": 1,
    })

    res_view = client.get("/api/v1/public/vehicle/zp_cg_4582")
    assert res_view.status_code == 200
    data = res_view.json()
    assert "masked_vrn" in data
    assert "**" in data["masked_vrn"]  # Privacy protected

    # Test parking alert
    res_alert = client.post("/api/v1/public/vehicle/zp_cg_4582/parking-alert", json={
        "alert_type": "WRONG_PARKING",
    })
    assert res_alert.status_code == 200
    assert res_alert.json()["success"] is True

    # Test emergency SOS
    res_sos = client.post("/api/v1/public/vehicle/zp_cg_4582/emergency-sos", json={
        "caller_location_lat": 22.0797,
        "caller_location_lng": 82.1409,
    })
    assert res_sos.status_code == 200
    assert res_sos.json()["sos_triggered"] is True

    # Test HTML Web View
    res_html = client.get("/v/zp_cg_4582")
    assert res_html.status_code == 200
    assert "ZapPay Verified Tag" in res_html.text
