package com.zappay.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    val id: Int,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    val email: String?,
    val role: String,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "is_verified") val isVerified: Boolean,
    val address: String?,
    val city: String?,
    val state: String?,
    val pincode: String?,
    @Json(name = "vehicle_number") val vehicleNumber: String?,
    @Json(name = "vehicle_type") val vehicleType: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "wallet_balance") val walletBalance: Double?,
)
