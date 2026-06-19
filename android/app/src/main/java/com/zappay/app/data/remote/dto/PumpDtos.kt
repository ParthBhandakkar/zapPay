package com.zappay.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PumpRegisterRequest(
    @Json(name = "pump_name") val pumpName: String,
    @Json(name = "owner_name") val ownerName: String,
    @Json(name = "license_number") val licenseNumber: String,
    val address: String,
    val city: String,
    val state: String,
    val pincode: String,
    @Json(name = "phone_number") val phoneNumber: String,
    val email: String? = null,
)

@JsonClass(generateAdapter = true)
data class PumpResponse(
    val id: Int,
    @Json(name = "pump_name") val pumpName: String,
    @Json(name = "owner_name") val ownerName: String,
    val address: String,
    val city: String,
    val state: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "is_verified") val isVerified: Boolean,
)

@JsonClass(generateAdapter = true)
data class PumpDashboardResponse(
    @Json(name = "total_transactions") val totalTransactions: Int,
    @Json(name = "total_revenue") val totalRevenue: Double,
    @Json(name = "transactions_today") val transactionsToday: Int,
    @Json(name = "revenue_today") val revenueToday: Double,
    @Json(name = "recent_transactions") val recentTransactions: List<TransactionDto>,
)

@JsonClass(generateAdapter = true)
data class ValidatedCustomerResponse(
    val valid: Boolean,
    @Json(name = "user_id") val userId: Int?,
    @Json(name = "user_name") val userName: String?,
    @Json(name = "user_phone") val userPhone: String?,
    @Json(name = "wallet_balance") val walletBalance: Double?,
    @Json(name = "vehicle_number") val vehicleNumber: String?,
    @Json(name = "vehicle_type") val vehicleType: String?,
)

@JsonClass(generateAdapter = true)
data class VehicleLookupResponse(
    val found: Boolean,
    @Json(name = "user_id") val userId: Int?,
    @Json(name = "user_name") val userName: String?,
    @Json(name = "user_phone") val userPhone: String?,
    @Json(name = "wallet_balance") val walletBalance: Double?,
    @Json(name = "vehicle_number") val vehicleNumber: String?,
    val message: String?,
)

@JsonClass(generateAdapter = true)
data class OCRResponse(
    val success: Boolean,
    val plates: List<String>,
    @Json(name = "raw_text") val rawText: String,
    val message: String,
)

@JsonClass(generateAdapter = true)
data class GenericResponse(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>? = null,
)

@JsonClass(generateAdapter = true)
data class PumpSettingsData(
    @Json(name = "pump_id") val pumpId: Int,
    @Json(name = "pump_name") val pumpName: String? = null,
    @Json(name = "fuel_types") val fuelTypes: String = "Petrol,Diesel,CNG",
    @Json(name = "fuel_rates") val fuelRates: String = "104.50,92.30,78.00",
    @Json(name = "is_open") val isOpen: Boolean = true,
    @Json(name = "petrol_price") val petrolPrice: String? = null,
    @Json(name = "diesel_price") val dieselPrice: String? = null,
    @Json(name = "cng_price") val cngPrice: String? = null,
)

@JsonClass(generateAdapter = true)
data class PumpSettingsResponse(
    val success: Boolean,
    val message: String,
    val data: PumpSettingsData,
)

@JsonClass(generateAdapter = true)
data class SaveSettingsRequest(
    @Json(name = "pump_id") val pumpId: Int,
    @Json(name = "fuel_types") val fuelTypes: String,
    @Json(name = "fuel_rates") val fuelRates: String,
    @Json(name = "is_open") val isOpen: Boolean,
)
