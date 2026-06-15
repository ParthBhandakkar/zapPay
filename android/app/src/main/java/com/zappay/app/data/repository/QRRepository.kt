package com.zappay.app.data.repository

import com.zappay.app.data.remote.api.ZapPayApi
import com.zappay.app.data.remote.dto.GenericResponse
import com.zappay.app.data.remote.dto.QRCodeGenerateRequest
import com.zappay.app.data.remote.dto.QRCodeResponse
import com.zappay.app.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRRepository @Inject constructor(
    private val api: ZapPayApi,
) {
    suspend fun getMyQRCodes(): Resource<List<QRCodeResponse>> {
        return try {
            val response = api.getMyQRCodes()
            if (response.isSuccessful) Resource.Success(response.body() ?: emptyList())
            else Resource.Error("Failed to load QR codes")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun generateQR(qrType: String = "mobile"): Resource<QRCodeResponse> {
        return try {
            val response = api.generateQR(QRCodeGenerateRequest(qrType))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to generate QR")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deactivateQR(id: Int): Resource<GenericResponse> {
        return try {
            val response = api.deactivateQR(id)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to deactivate QR")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
