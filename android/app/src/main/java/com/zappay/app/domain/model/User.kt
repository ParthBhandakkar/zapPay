package com.zappay.app.domain.model

enum class UserRole { CUSTOMER, PUMP_OWNER, PUMP_OPERATOR, ADMIN }

data class User(
    val id: Int,
    val phoneNumber: String,
    val fullName: String,
    val email: String?,
    val role: UserRole,
    val isActive: Boolean,
    val isVerified: Boolean,
    val kycStatus: String,
    val vehicleNumber: String?,
    val vehicleType: String?,
)
