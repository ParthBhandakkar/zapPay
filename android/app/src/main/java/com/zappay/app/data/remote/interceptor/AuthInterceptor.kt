package com.zappay.app.data.remote.interceptor

import com.zappay.app.data.local.TokenManager
import com.zappay.app.util.Constants
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val lock = Any()
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { tokenManager.getAccessToken() }

        val authenticatedRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        if (response.code == 401 && !isRefreshRequest(originalRequest)) {
            response.close()
            val refreshed = synchronized(lock) {
                if (!isRefreshing) {
                    isRefreshing = true
                    try {
                        refreshToken()
                    } finally {
                        isRefreshing = false
                    }
                } else {
                    false
                }
            }
            if (refreshed) {
                val newToken = runBlocking { tokenManager.getAccessToken() } ?: return response
                val retryRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retryRequest)
            }
        }

        return response
    }

    private fun isRefreshRequest(request: Request): Boolean {
        return request.url.encodedPath.contains("auth/refresh")
    }

    private fun refreshToken(): Boolean {
        return try {
            val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return false
            val jsonBody = JSONObject().apply {
                put("refresh_token", refreshToken)
            }.toString().toRequestBody("application/json".toMediaType())

            val refreshRequest = Request.Builder()
                .url("${Constants.BASE_URL}auth/refresh")
                .post(jsonBody)
                .build()

            val response = refreshClient.newCall(refreshRequest).execute()
            val bodyString = response.body?.string() ?: return false

            if (response.isSuccessful) {
                val json = JSONObject(bodyString)
                val newAccessToken = json.getString("access_token")
                val newRefreshToken = json.optString("refresh_token", refreshToken)
                runBlocking { tokenManager.saveTokens(newAccessToken, newRefreshToken) }
                true
            } else {
                runBlocking { tokenManager.clearTokens() }
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
