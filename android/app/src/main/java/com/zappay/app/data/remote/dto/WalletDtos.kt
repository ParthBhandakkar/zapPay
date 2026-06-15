package com.zappay.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalletResponse(
    val id: Int,
    val balance: Double,
    @Json(name = "total_recharged") val totalRecharged: Double,
    @Json(name = "total_spent") val totalSpent: Double,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class WalletSummaryResponse(
    val balance: Double,
    @Json(name = "total_recharged") val totalRecharged: Double,
    @Json(name = "total_spent") val totalSpent: Double,
    @Json(name = "monthly_spending") val monthlySpending: Double,
    @Json(name = "recent_transactions") val recentTransactions: List<TransactionDto>,
)

@JsonClass(generateAdapter = true)
data class RechargeOrderRequest(
    val amount: Double,
    @Json(name = "payment_method") val paymentMethod: String = "razorpay",
)

@JsonClass(generateAdapter = true)
data class RechargeOrderResponse(
    val success: Boolean,
    val message: String,
    val data: RechargeOrderData?,
)

@JsonClass(generateAdapter = true)
data class RechargeOrderData(
    @Json(name = "order_id") val orderId: String?,
    val amount: Double?,
    val currency: String?,
    @Json(name = "client_secret") val clientSecret: String?,
    @Json(name = "payment_method") val paymentMethod: String?,
)
