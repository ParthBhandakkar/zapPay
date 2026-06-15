package com.zappay.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionDto(
    val id: Int,
    @Json(name = "transaction_id") val transactionId: String,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "amount") val amount: Double,
    @Json(name = "commission_amount") val commissionAmount: Double,
    @Json(name = "fuel_quantity") val fuelQuantity: Double?,
    @Json(name = "fuel_type") val fuelType: String?,
    @Json(name = "transaction_type") val transactionType: String,
    val status: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "completed_at") val completedAt: String?,
)

@JsonClass(generateAdapter = true)
data class TransactionHistoryResponse(
    val transactions: List<TransactionDto>,
    @Json(name = "total_count") val totalCount: Int,
    val page: Int,
    @Json(name = "page_size") val pageSize: Int,
    @Json(name = "total_pages") val totalPages: Int,
)

@JsonClass(generateAdapter = true)
data class FuelPurchaseRequest(
    @Json(name = "qr_code") val qrCode: String,
    @Json(name = "pump_id") val pumpId: Int,
    @Json(name = "fuel_type") val fuelType: String,
    @Json(name = "fuel_quantity") val fuelQuantity: Double,
    @Json(name = "fuel_rate") val fuelRate: Double,
)

@JsonClass(generateAdapter = true)
data class PurchaseByVehicleRequest(
    @Json(name = "user_id") val userId: Int,
    @Json(name = "pump_id") val pumpId: Int,
    @Json(name = "fuel_type") val fuelType: String,
    @Json(name = "fuel_quantity") val fuelQuantity: Double,
    @Json(name = "fuel_rate") val fuelRate: Double,
)
