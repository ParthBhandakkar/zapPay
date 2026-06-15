package com.zappay.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "password") val password: String,
    @Json(name = "confirm_password") val confirmPassword: String,
    @Json(name = "role") val role: String,
    @Json(name = "email") val email: String? = null,
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int,
)

@JsonClass(generateAdapter = true)
data class OTPRequest(
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "otp_type") val otpType: String = "login",
)

@JsonClass(generateAdapter = true)
data class OTPVerifyRequest(
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "otp_code") val otpCode: String,
    @Json(name = "otp_type") val otpType: String = "login",
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refresh_token") val refreshToken: String,
)
