package com.zappay.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QRCodeResponse(
    val id: Int,
    @Json(name = "qr_code") val qrCode: String,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "vehicle_id") val vehicleId: Int?,
    @Json(name = "expires_at") val expiresAt: String?,
)

@JsonClass(generateAdapter = true)
data class QRCodeGenerateRequest(
    @Json(name = "qr_type") val qrType: String = "mobile",
    @Json(name = "vehicle_id") val vehicleId: Int? = null,
    @Json(name = "validity_hours") val validityHours: Int? = null,
)

@JsonClass(generateAdapter = true)
data class QRValidateRequest(
    @Json(name = "qr_data") val qrData: String,
)
