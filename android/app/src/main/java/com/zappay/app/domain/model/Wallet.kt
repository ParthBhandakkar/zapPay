package com.zappay.app.domain.model

data class Wallet(
    val id: Int,
    val balance: Double,
    val totalRecharged: Double,
    val totalSpent: Double,
    val isActive: Boolean,
)
