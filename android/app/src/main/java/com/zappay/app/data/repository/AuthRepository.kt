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
    suspend fun login(username: String, password: String): Resource<TokenResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                Resource.Success(body)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Login failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun register(
        phone: String, name: String, password: String, role: String, email: String?,
    ): Resource<GenericResponse> {
        return try {
            val response = api.register(RegisterRequest(phone, name, password, password, role, email))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error(response.errorBody()?.string() ?: "Registration failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun sendOTP(phone: String): Resource<GenericResponse> {
        return try {
            val response = api.sendOTP(OTPRequest(phone))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error(response.errorBody()?.string() ?: "Failed to send OTP")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun loginWithOTP(phone: String, otp: String): Resource<TokenResponse> {
        return try {
            val response = api.loginWithOTP(OTPVerifyRequest(phone, otp))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                Resource.Success(body)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "OTP verification failed")
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
