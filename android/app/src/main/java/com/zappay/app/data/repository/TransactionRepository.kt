package com.zappay.app.data.repository

import com.zappay.app.data.remote.api.ZapPayApi
import com.zappay.app.data.remote.dto.TransactionHistoryResponse
import com.zappay.app.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val api: ZapPayApi,
) {
    suspend fun getHistory(page: Int = 1, pageSize: Int = 20): Resource<TransactionHistoryResponse> {
        return try {
            val response = api.getTransactionHistory(page, pageSize)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load transactions")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
