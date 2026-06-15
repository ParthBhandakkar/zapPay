package com.zappay.app.domain.model

data class PetrolPump(
    val id: Int,
    val pumpName: String,
    val address: String,
    val city: String,
    val state: String,
    val phoneNumber: String,
    val isActive: Boolean,
    val isVerified: Boolean,
    val commissionRate: Double,
)

data class PumpSettings(
    val pumpId: Int,
    val pumpName: String,
    val petrolPrice: String,
    val dieselPrice: String,
)

data class PumpDashboardData(
    val totalTransactions: Int,
    val totalRevenue: Double,
    val transactionsToday: Int,
    val revenueToday: Double,
    val recentTransactions: List<Transaction>,
)

data class ValidatedCustomer(
    val userId: Int,
    val userName: String,
    val userPhone: String,
    val walletBalance: Double,
    val vehicleNumber: String?,
    val vehicleType: String?,
)

data class VehicleLookupResult(
    val found: Boolean,
    val userId: Int?,
    val userName: String?,
    val userPhone: String?,
    val walletBalance: Double?,
    val vehicleNumber: String?,
    val message: String?,
)
