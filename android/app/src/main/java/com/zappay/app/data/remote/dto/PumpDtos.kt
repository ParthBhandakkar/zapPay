package com.zappay.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NearbyPumpDto(
    val id: Int,
    @Json(name = "pump_name") val pumpName: String,
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    @Json(name = "distance_km") val distanceKm: Double,
    @Json(name = "fuel_types") val fuelTypes: List<String>?,
    @Json(name = "is_open") val isOpen: Boolean?,
    @Json(name = "fuel_prices") val fuelPrices: List<FuelPriceDto>?,
)

@JsonClass(generateAdapter = true)
data class NearbyPumpsResponse(
    @Json(name = "nearby_pumps") val nearbyPumps: List<NearbyPumpDto>,
)

@JsonClass(generateAdapter = true)
data class FuelPriceDto(
    val id: Int,
    @Json(name = "pump_id") val pumpId: Int,
    @Json(name = "fuel_type") val fuelType: String,
    val price: Double,
    @Json(name = "effective_from") val effectiveFrom: String?,
    @Json(name = "effective_to") val effectiveTo: String?,
    @Json(name = "is_active") val isActive: Boolean,
)

@JsonClass(generateAdapter = true)
data class FuelPricesResponse(
    @Json(name = "fuel_prices") val fuelPrices: List<FuelPriceDto>,
)

@JsonClass(generateAdapter = true)
data class PumpInventoryDto(
    val id: Int,
    @Json(name = "pump_id") val pumpId: Int,
    @Json(name = "fuel_type") val fuelType: String,
    @Json(name = "current_stock") val currentStock: Double,
    @Json(name = "max_capacity") val maxCapacity: Double,
    @Json(name = "last_updated") val lastUpdated: String?,
)

@JsonClass(generateAdapter = true)
data class PumpInventoryResponse(
    val inventory: List<PumpInventoryDto>,
)

@JsonClass(generateAdapter = true)
data class ShiftDto(
    val id: Int,
    @Json(name = "pump_id") val pumpId: Int,
    @Json(name = "operator_id") val operatorId: Int,
    @Json(name = "shift_type") val shiftType: String?,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String?,
    val status: String?,
    val notes: String?,
    @Json(name = "operator_name") val operatorName: String?,
)

@JsonClass(generateAdapter = true)
data class ShiftsResponse(
    val shifts: List<ShiftDto>,
    @Json(name = "total_count") val totalCount: Int,
    val page: Int,
    @Json(name = "page_size") val pageSize: Int,
)

@JsonClass(generateAdapter = true)
data class VehicleDto(
    val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "vehicle_number") val vehicleNumber: String,
    @Json(name = "vehicle_type") val vehicleType: String?,
    val nickname: String?,
    @Json(name = "is_primary") val isPrimary: Boolean,
    @Json(name = "is_active") val isActive: Boolean,
)

@JsonClass(generateAdapter = true)
data class VehicleAddRequest(
    @Json(name = "vehicle_number") val vehicleNumber: String,
    @Json(name = "vehicle_type") val vehicleType: String? = null,
    val nickname: String? = null,
    @Json(name = "is_primary") val isPrimary: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class VehicleUpdateRequest(
    @Json(name = "vehicle_number") val vehicleNumber: String? = null,
    @Json(name = "vehicle_type") val vehicleType: String? = null,
    val nickname: String? = null,
    @Json(name = "is_primary") val isPrimary: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "notification_type") val notificationType: String,
    val title: String,
    val body: String?,
    @Json(name = "is_read") val isRead: Boolean,
    @Json(name = "is_sent") val isSent: Boolean,
    @Json(name = "created_at") val createdAt: String?,
)

@JsonClass(generateAdapter = true)
data class NotificationsResponse(
    val notifications: List<NotificationDto>,
)

@JsonClass(generateAdapter = true)
data class SupportTicketDto(
    val id: Int,
    @Json(name = "user_id") val userId: Int,
    val subject: String,
    val description: String?,
    val category: String?,
    val priority: String?,
    val status: String?,
    val resolution: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?,
)

@JsonClass(generateAdapter = true)
data class TicketsResponse(
    val tickets: List<SupportTicketDto>,
    @Json(name = "total_count") val totalCount: Int,
    val page: Int,
    @Json(name = "page_size") val pageSize: Int,
)

@JsonClass(generateAdapter = true)
data class TicketCreateRequest(
    val subject: String,
    val description: String? = null,
    val category: String = "other",
    val priority: String = "medium",
)

@JsonClass(generateAdapter = true)
data class DeviceRegisterRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "device_name") val deviceName: String,
)

@JsonClass(generateAdapter = true)
data class ShiftStartRequest(
    @Json(name = "shift_type") val shiftType: String = "morning",
    val notes: String? = null,
)
