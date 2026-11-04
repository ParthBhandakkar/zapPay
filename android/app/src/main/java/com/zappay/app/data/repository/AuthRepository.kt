package com.zappay.app.data.repository

import com.zappay.app.data.local.TokenManager
import com.zappay.app.data.remote.api.ZapPayApi
import com.zappay.app.data.remote.dto.*
import com.zappay.app.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ZapPayApi,
    private val tokenManager: TokenManager,
) {
    private fun parseErrorMessage(errorBody: String?, fallback: String): String {
        if (errorBody.isNullOrBlank()) return fallback
        return try {
            val json = org.json.JSONObject(errorBody)
            if (json.has("detail")) {
                val detail = json.get("detail")
                if (detail is String) return detail
            }
            if (json.has("error")) {
                val err = json.optJSONObject("error")
                if (err != null && err.has("message")) return err.getString("message")
            }
            if (json.has("message")) json.getString("message")
            else fallback
        } catch (_: Exception) {
            fallback
        }
    }

    suspend fun login(username: String, password: String): Resource<TokenResponse> {
        return try {
            val normalizedPhone = if (username.length == 10 && !username.startsWith("+")) "+91$username" else username.trim()
            val response = api.login(LoginRequest(normalizedPhone, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                Resource.Success(body)
            } else {
                Resource.Error(parseErrorMessage(response.errorBody()?.string(), "Login failed"))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun register(
        phone: String, name: String, password: String, role: String, email: String?,
    ): Resource<GenericResponse> {
        return try {
            val normalizedPhone = if (phone.length == 10 && !phone.startsWith("+")) "+91$phone" else phone.trim()
            val response = api.register(RegisterRequest(normalizedPhone, name, password, password, role, email))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error(parseErrorMessage(response.errorBody()?.string(), "Registration failed"))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun sendOTP(phone: String): Resource<GenericResponse> {
        return try {
            val normalizedPhone = if (phone.length == 10 && !phone.startsWith("+")) "+91$phone" else phone.trim()
            val response = api.sendOTP(OTPRequest(normalizedPhone))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error(parseErrorMessage(response.errorBody()?.string(), "Failed to send OTP"))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun loginWithOTP(phone: String, otp: String): Resource<TokenResponse> {
        return try {
            val normalizedPhone = if (phone.length == 10 && !phone.startsWith("+")) "+91$phone" else phone.trim()
            val response = api.loginWithOTP(OTPVerifyRequest(normalizedPhone, otp))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                Resource.Success(body)
            } else {
                Resource.Error(parseErrorMessage(response.errorBody()?.string(), "OTP verification failed"))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun refreshToken(): Boolean {
        return try {
            val rt = tokenManager.getRefreshToken() ?: return false
            val response = api.refreshToken(RefreshTokenRequest(rt))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun saveUserInfo(userId: Int, role: String, name: String, phone: String) {
        tokenManager.saveUserInfo(userId, role, name, phone)
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    suspend fun getUserRole(): String? = tokenManager.getUserRole()

    suspend fun logout() {
        tokenManager.clearTokens()
    }
}
