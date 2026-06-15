package com.zappay.app.domain.model

data class Transaction(
    val id: Int,
    val transactionId: String,
    val amount: Double,
    val type: TransactionType,
    val status: TransactionStatus,
    val fuelQuantity: Double?,
    val fuelType: String?,
    val createdAt: String,
    val description: String?,
)

enum class TransactionType { FUEL_PURCHASE, WALLET_RECHARGE, REFUND, COMMISSION }
enum class TransactionStatus { PENDING, COMPLETED, FAILED, REFUNDED }
