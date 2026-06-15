package com.zappay.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.zappay.app.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "zappay_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(Constants.ACCESS_TOKEN_KEY, accessToken)
            .putString(Constants.REFRESH_TOKEN_KEY, refreshToken)
            .apply()
    }

    suspend fun getAccessToken(): String? = prefs.getString(Constants.ACCESS_TOKEN_KEY, null)
    suspend fun getRefreshToken(): String? = prefs.getString(Constants.REFRESH_TOKEN_KEY, null)

    suspend fun clearTokens() {
        prefs.edit().clear().apply()
    }

    suspend fun saveUserInfo(userId: Int, role: String, name: String, phone: String) {
        prefs.edit()
            .putInt(Constants.USER_ID_KEY, userId)
            .putString(Constants.USER_ROLE_KEY, role)
            .putString(Constants.USER_NAME_KEY, name)
            .putString(Constants.USER_PHONE_KEY, phone)
            .apply()
    }

    suspend fun getUserId(): Int = prefs.getInt(Constants.USER_ID_KEY, 0)
    suspend fun getUserRole(): String? = prefs.getString(Constants.USER_ROLE_KEY, null)
    suspend fun getUserName(): String? = prefs.getString(Constants.USER_NAME_KEY, null)
    suspend fun getUserPhone(): String? = prefs.getString(Constants.USER_PHONE_KEY, null)

    suspend fun isLoggedIn(): Boolean = getAccessToken() != null
}
