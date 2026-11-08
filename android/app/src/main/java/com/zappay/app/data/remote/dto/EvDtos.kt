package com.zappay.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EvConnectorDto(
    @Json(name = "connector_id") val connectorId: String,
    val type: String,
    @Json(name = "power_kw") val powerKw: Double,
    @Json(name = "tariff_per_kwh") val tariffPerKwh: Double,
    val status: String,
)

@JsonClass(generateAdapter = true)
data class EvStationDto(
    val id: Int,
    val name: String,
    val operator: String,
    val address: String,
    val city: String,
    @Json(name = "distance_km") val distanceKm: Double,
    val latitude: Double,
    val longitude: Double,
    @Json(name = "total_guns") val totalGuns: Int,
    @Json(name = "available_guns") val availableGuns: Int,
    val connectors: List<EvConnectorDto>,
    val amenities: List<String>,
    val rating: Double,
    @Json(name = "is_open") val isOpen: Boolean = true,
)

@JsonClass(generateAdapter = true)
data class EvStationsResponse(
    val stations: List<EvStationDto>,
)

@JsonClass(generateAdapter = true)
data class EvTelemetryDto(
    @Json(name = "vehicle_number") val vehicleNumber: String,
    @Json(name = "make_model") val makeModel: String,
    @Json(name = "battery_percentage") val batteryPercentage: Int,
    @Json(name = "range_km") val rangeKm: Int,
    @Json(name = "battery_capacity_kwh") val batteryCapacityKwh: Double,
    @Json(name = "soh_percentage") val sohPercentage: Double,
    @Json(name = "voltage_volts") val voltageVolts: Double,
    @Json(name = "battery_temp_celsius") val batteryTempCelsius: Double,
    @Json(name = "port_locked") val portLocked: Boolean,
    @Json(name = "cabin_temp_celsius") val cabinTempCelsius: Double,
    @Json(name = "cabin_preconditioned") val cabinPreconditioned: Boolean,
    @Json(name = "target_soc_limit") val targetSocLimit: Int,
    @Json(name = "last_charged_at") val lastChargedAt: String?,
)

@JsonClass(generateAdapter = true)
data class StartEvChargingRequest(
    @Json(name = "station_id") val stationId: Int,
    @Json(name = "connector_id") val connectorId: String? = "GUN-01",
    @Json(name = "target_soc") val targetSoc: Int? = 80,
)

@JsonClass(generateAdapter = true)
data class EvChargingSessionResponse(
    @Json(name = "session_id") val sessionId: String,
    val status: String,
    @Json(name = "is_charging") val isCharging: Boolean = true,
    @Json(name = "battery_percentage") val batteryPercentage: Int,
    @Json(name = "charging_speed_kw") val chargingSpeedKw: Double,
    @Json(name = "kwh_delivered") val kwhDelivered: Double,
    @Json(name = "elapsed_seconds") val elapsedSeconds: Int,
    @Json(name = "time_remaining_minutes") val timeRemainingMinutes: Int,
    @Json(name = "cost_incurred") val costIncurred: Double,
    @Json(name = "station_name") val stationName: String,
    @Json(name = "connector_type") val connectorType: String,
    val message: String? = null,
)

@JsonClass(generateAdapter = true)
data class StopEvChargingRequest(
    @Json(name = "session_id") val sessionId: String? = null,
)

@JsonClass(generateAdapter = true)
data class ReserveSlotRequest(
    @Json(name = "station_id") val stationId: Int,
    @Json(name = "connector_id") val connectorId: String,
    @Json(name = "duration_minutes") val durationMinutes: Int = 15,
)

@JsonClass(generateAdapter = true)
data class ReserveSlotResponse(
    val success: Boolean,
    val message: String,
    @Json(name = "reservation_id") val reservationId: String,
    @Json(name = "expires_in_minutes") val expiresInMinutes: Int,
    @Json(name = "station_name") val stationName: String,
    @Json(name = "gun_id") val gunId: String,
)
