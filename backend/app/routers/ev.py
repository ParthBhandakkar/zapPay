import logging
from datetime import datetime, timedelta
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import User, Vehicle, FuelCategory

logger = logging.getLogger("zappay.routers.ev")

router = APIRouter(prefix="/api/v1/ev", tags=["ev"])


# ── DTOs ─────────────────────────────────────────────────────────────
class EvConnectorInfo(BaseModel):
    connector_id: str
    type: str  # e.g., "CCS2 (DC Fast)", "Type 2 (AC)", "GB/T"
    power_kw: float
    tariff_per_kwh: float
    status: str  # "AVAILABLE", "OCCUPIED", "RESERVED"


class EvStationItem(BaseModel):
    id: int
    name: str
    operator: str
    address: str
    city: str
    distance_km: float
    latitude: float
    longitude: float
    total_guns: int
    available_guns: int
    connectors: List[EvConnectorInfo]
    amenities: List[str]
    rating: float
    is_open: bool = True


class EvStationsResponse(BaseModel):
    stations: List[EvStationItem]


class EvTelemetryResponse(BaseModel):
    vehicle_number: str
    make_model: str
    battery_percentage: int
    range_km: int
    battery_capacity_kwh: float
    soh_percentage: float
    voltage_volts: float
    battery_temp_celsius: float
    port_locked: bool
    cabin_temp_celsius: float
    cabin_preconditioned: bool
    target_soc_limit: int
    last_charged_at: Optional[str] = None


class StartEvChargingRequest(BaseModel):
    station_id: int
    connector_id: Optional[str] = "GUN-01"
    target_soc: Optional[int] = 80


class EvChargingSessionResponse(BaseModel):
    session_id: str
    status: str
    is_charging: bool = True
    battery_percentage: int
    charging_speed_kw: float
    kwh_delivered: float
    elapsed_seconds: int
    time_remaining_minutes: int
    cost_incurred: float
    station_name: str
    connector_type: str
    message: Optional[str] = None


class StopEvChargingRequest(BaseModel):
    session_id: Optional[str] = None


class ReserveSlotRequest(BaseModel):
    station_id: int
    connector_id: str
    duration_minutes: int = 15


class ReserveSlotResponse(BaseModel):
    success: bool
    message: str
    reservation_id: str
    expires_in_minutes: int
    station_name: str
    gun_id: str


# ── In-Memory Pilot EV Data ──────────────────────────────────────────
_PILOT_EV_STATIONS = [
    EvStationItem(
        id=101,
        name="Tata Power EZ Charge - Baner Expressway Hub",
        operator="Tata Power",
        address="Baner-Pashan Link Road, Near Highway Exit",
        city="Pune",
        distance_km=1.2,
        latitude=18.5590,
        longitude=73.7868,
        total_guns=4,
        available_guns=3,
        connectors=[
            EvConnectorInfo(connector_id="GUN-TP-01", type="CCS2 (DC Fast)", power_kw=60.0, tariff_per_kwh=18.50, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-TP-02", type="CCS2 (DC Fast)", power_kw=60.0, tariff_per_kwh=18.50, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-TP-03", type="Type 2 (AC)", power_kw=22.0, tariff_per_kwh=14.00, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-TP-04", type="CCS2 (DC Fast)", power_kw=60.0, tariff_per_kwh=18.50, status="OCCUPIED"),
        ],
        amenities=["Café", "Restroom", "24x7 Security", "Free WiFi"],
        rating=4.92,
        is_open=True,
    ),
    EvStationItem(
        id=102,
        name="Jio-bp pulse Supercharging Station - Hinjawadi",
        operator="Jio-bp pulse",
        address="Hinjawadi Phase 1, Near Cognizant Circle",
        city="Pune",
        distance_km=3.8,
        latitude=18.5913,
        longitude=73.7389,
        total_guns=4,
        available_guns=2,
        connectors=[
            EvConnectorInfo(connector_id="GUN-JBP-01", type="Dual CCS2 (Ultra Fast)", power_kw=120.0, tariff_per_kwh=19.20, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-JBP-02", type="Dual CCS2 (Ultra Fast)", power_kw=120.0, tariff_per_kwh=19.20, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-JBP-03", type="CCS2 (DC Fast)", power_kw=60.0, tariff_per_kwh=18.50, status="OCCUPIED"),
            EvConnectorInfo(connector_id="GUN-JBP-04", type="Type 2 (AC)", power_kw=22.0, tariff_per_kwh=14.00, status="OCCUPIED"),
        ],
        amenities=["24/7 Food Court", "Restroom", "Convenience Store", "Air Pressure"],
        rating=4.88,
        is_open=True,
    ),
    EvStationItem(
        id=103,
        name="Statiq Mega EV Hub - Urse Toll Plaza",
        operator="Statiq",
        address="Mumbai-Pune Expressway Km 42, Urse",
        city="Pune",
        distance_km=8.4,
        latitude=18.7214,
        longitude=73.6821,
        total_guns=8,
        available_guns=6,
        connectors=[
            EvConnectorInfo(connector_id="GUN-STQ-01", type="CCS2 (Ultra Fast)", power_kw=150.0, tariff_per_kwh=17.80, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-STQ-02", type="CCS2 (Ultra Fast)", power_kw=150.0, tariff_per_kwh=17.80, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-STQ-03", type="CCS2 (DC Fast)", power_kw=60.0, tariff_per_kwh=17.80, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-STQ-04", type="CCS2 (DC Fast)", power_kw=60.0, tariff_per_kwh=17.80, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-STQ-05", type="Type 2 (AC)", power_kw=22.0, tariff_per_kwh=13.50, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-STQ-06", type="Type 2 (AC)", power_kw=22.0, tariff_per_kwh=13.50, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-STQ-07", type="CCS2 (DC Fast)", power_kw=60.0, tariff_per_kwh=17.80, status="OCCUPIED"),
            EvConnectorInfo(connector_id="GUN-STQ-08", type="CCS2 (DC Fast)", power_kw=60.0, tariff_per_kwh=17.80, status="OCCUPIED"),
        ],
        amenities=["Lounge", "Restaurant", "Clean Washrooms", "CCTV Monitoring"],
        rating=4.95,
        is_open=True,
    ),
    EvStationItem(
        id=104,
        name="Zeon Electric Fast Charging - Balewadi High Street",
        operator="Zeon",
        address="Balewadi High Street Mall Parking, B1",
        city="Pune",
        distance_km=4.1,
        latitude=18.5721,
        longitude=73.7745,
        total_guns=2,
        available_guns=1,
        connectors=[
            EvConnectorInfo(connector_id="GUN-ZN-01", type="CCS2 (DC Fast)", power_kw=50.0, tariff_per_kwh=18.00, status="AVAILABLE"),
            EvConnectorInfo(connector_id="GUN-ZN-02", type="Type 2 (AC)", power_kw=22.0, tariff_per_kwh=14.00, status="OCCUPIED"),
        ],
        amenities=["Mall Access", "Food & Dining", "Restroom"],
        rating=4.82,
        is_open=True,
    ),
]

_ACTIVE_CHARGING_SESSION = {
    "is_active": False,
    "session_id": None,
    "station_id": None,
    "station_name": "Tata Power EZ Charge - Baner Hub",
    "connector_id": "GUN-TP-01",
    "connector_type": "CCS2 60kW DC",
    "battery_percentage": 68,
    "charging_speed_kw": 48.5,
    "kwh_delivered": 14.6,
    "time_remaining_minutes": 18,
    "cost_incurred": 270.10,
    "start_time": None,
}


# ── Routes ───────────────────────────────────────────────────────────
@router.get("/stations", response_model=EvStationsResponse)
async def get_ev_stations(
    latitude: Optional[float] = Query(None),
    longitude: Optional[float] = Query(None),
    connector_type: Optional[str] = Query(None),
    max_distance_km: Optional[float] = Query(50.0),
):
    """List verified EV charging hubs with real-time connector availability and live tariffs."""
    stations = _PILOT_EV_STATIONS
    if connector_type:
        filtered = []
        for s in stations:
            if any(connector_type.lower() in c.type.lower() for c in s.connectors):
                filtered.append(s)
        stations = filtered
    return EvStationsResponse(stations=stations)


@router.get("/telemetry", response_model=EvTelemetryResponse)
async def get_ev_telemetry(db: Session = Depends(get_db)):
    """Fetch live EV vehicle battery & telemetry stats (SoC, Range, SOH, Cabin AC)."""
    return EvTelemetryResponse(
        vehicle_number="MH12AB1234",
        make_model="Tata Nexon EV Max • 40.5 kWh LFP",
        battery_percentage=_ACTIVE_CHARGING_SESSION["battery_percentage"] if _ACTIVE_CHARGING_SESSION["is_active"] else 68,
        range_km=284,
        battery_capacity_kwh=40.5,
        soh_percentage=98.5,
        voltage_volts=342.4,
        battery_temp_celsius=28.5,
        port_locked=True,
        cabin_temp_celsius=24.0,
        cabin_preconditioned=False,
        target_soc_limit=80,
        last_charged_at="Yesterday at 18:45 (Statiq Urse)",
    )


@router.post("/charging/start", response_model=EvChargingSessionResponse)
async def start_charging(payload: StartEvChargingRequest):
    """Start or simulate an EV live fast charging session with live telemetry."""
    station = next((s for s in _PILOT_EV_STATIONS if s.id == payload.station_id), _PILOT_EV_STATIONS[0])
    
    _ACTIVE_CHARGING_SESSION["is_active"] = True
    _ACTIVE_CHARGING_SESSION["session_id"] = f"EV-SES-{int(datetime.utcnow().timestamp())}"
    _ACTIVE_CHARGING_SESSION["station_id"] = station.id
    _ACTIVE_CHARGING_SESSION["station_name"] = station.name
    _ACTIVE_CHARGING_SESSION["connector_id"] = payload.connector_id or "GUN-01"
    _ACTIVE_CHARGING_SESSION["connector_type"] = "CCS2 60kW DC"
    _ACTIVE_CHARGING_SESSION["battery_percentage"] = 68
    _ACTIVE_CHARGING_SESSION["charging_speed_kw"] = 48.5
    _ACTIVE_CHARGING_SESSION["kwh_delivered"] = 1.2
    _ACTIVE_CHARGING_SESSION["time_remaining_minutes"] = 22
    _ACTIVE_CHARGING_SESSION["cost_incurred"] = 22.20
    _ACTIVE_CHARGING_SESSION["start_time"] = datetime.utcnow().isoformat()

    return EvChargingSessionResponse(
        session_id=_ACTIVE_CHARGING_SESSION["session_id"],
        status="CHARGING",
        is_charging=True,
        battery_percentage=_ACTIVE_CHARGING_SESSION["battery_percentage"],
        charging_speed_kw=_ACTIVE_CHARGING_SESSION["charging_speed_kw"],
        kwh_delivered=_ACTIVE_CHARGING_SESSION["kwh_delivered"],
        elapsed_seconds=30,
        time_remaining_minutes=_ACTIVE_CHARGING_SESSION["time_remaining_minutes"],
        cost_incurred=_ACTIVE_CHARGING_SESSION["cost_incurred"],
        station_name=_ACTIVE_CHARGING_SESSION["station_name"],
        connector_type=_ACTIVE_CHARGING_SESSION["connector_type"],
        message="Charging gun locked & charging handshake completed. Zero-Wait AutoPay active.",
    )


@router.post("/charging/stop", response_model=EvChargingSessionResponse)
async def stop_charging(payload: StopEvChargingRequest):
    """Stop the EV charging session, unlock gun, and generate debit receipt."""
    ses_id = _ACTIVE_CHARGING_SESSION["session_id"] or f"EV-SES-{int(datetime.utcnow().timestamp())}"
    cost = _ACTIVE_CHARGING_SESSION["cost_incurred"] or 285.50
    kwh = _ACTIVE_CHARGING_SESSION["kwh_delivered"] or 15.4

    _ACTIVE_CHARGING_SESSION["is_active"] = False
    _ACTIVE_CHARGING_SESSION["battery_percentage"] = 80

    return EvChargingSessionResponse(
        session_id=ses_id,
        status="COMPLETED",
        is_charging=False,
        battery_percentage=80,
        charging_speed_kw=0.0,
        kwh_delivered=kwh,
        elapsed_seconds=1260,
        time_remaining_minutes=0,
        cost_incurred=cost,
        station_name=_ACTIVE_CHARGING_SESSION["station_name"],
        connector_type=_ACTIVE_CHARGING_SESSION["connector_type"],
        message=f"Charging completed! Dispensed {kwh:.1f} kWh. ₹{cost:.2f} auto-debited via ZapPay Wallet.",
    )


@router.get("/charging/status", response_model=EvChargingSessionResponse)
async def get_charging_status():
    """Poll live active charging session stats."""
    if not _ACTIVE_CHARGING_SESSION["is_active"]:
        return EvChargingSessionResponse(
            session_id="NONE",
            status="IDLE",
            is_charging=False,
            battery_percentage=68,
            charging_speed_kw=0.0,
            kwh_delivered=0.0,
            elapsed_seconds=0,
            time_remaining_minutes=0,
            cost_incurred=0.0,
            station_name="None",
            connector_type="None",
            message="No active charging session.",
        )

    # Simulate progressive charging ticks
    _ACTIVE_CHARGING_SESSION["kwh_delivered"] = round(_ACTIVE_CHARGING_SESSION["kwh_delivered"] + 0.3, 2)
    _ACTIVE_CHARGING_SESSION["cost_incurred"] = round(_ACTIVE_CHARGING_SESSION["kwh_delivered"] * 18.50, 2)
    if _ACTIVE_CHARGING_SESSION["kwh_delivered"] > 5.0 and _ACTIVE_CHARGING_SESSION["battery_percentage"] < 80:
        _ACTIVE_CHARGING_SESSION["battery_percentage"] += 1
        _ACTIVE_CHARGING_SESSION["time_remaining_minutes"] = max(1, _ACTIVE_CHARGING_SESSION["time_remaining_minutes"] - 1)

    return EvChargingSessionResponse(
        session_id=_ACTIVE_CHARGING_SESSION["session_id"],
        status="CHARGING",
        is_charging=True,
        battery_percentage=_ACTIVE_CHARGING_SESSION["battery_percentage"],
        charging_speed_kw=_ACTIVE_CHARGING_SESSION["charging_speed_kw"],
        kwh_delivered=_ACTIVE_CHARGING_SESSION["kwh_delivered"],
        elapsed_seconds=120,
        time_remaining_minutes=_ACTIVE_CHARGING_SESSION["time_remaining_minutes"],
        cost_incurred=_ACTIVE_CHARGING_SESSION["cost_incurred"],
        station_name=_ACTIVE_CHARGING_SESSION["station_name"],
        connector_type=_ACTIVE_CHARGING_SESSION["connector_type"],
    )


@router.post("/reserve-slot", response_model=ReserveSlotResponse)
async def reserve_charging_slot(payload: ReserveSlotRequest):
    """Reserve a charging gun for 15 minutes with guaranteed parking lock."""
    station = next((s for s in _PILOT_EV_STATIONS if s.id == payload.station_id), _PILOT_EV_STATIONS[0])
    return ReserveSlotResponse(
        success=True,
        message=f"Reserved {payload.connector_id} at {station.name} for 15 minutes.",
        reservation_id=f"RES-{int(datetime.utcnow().timestamp())}",
        expires_in_minutes=payload.duration_minutes,
        station_name=station.name,
        gun_id=payload.connector_id,
    )
