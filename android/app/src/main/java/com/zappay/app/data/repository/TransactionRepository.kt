package com.zappay.app.data.repository

import com.zappay.app.data.remote.api.ZapPayApi
import com.zappay.app.data.remote.dto.ReceiptDto
import com.zappay.app.data.remote.dto.TransactionDto
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

    suspend fun getDetail(transactionId: String): Resource<TransactionDto> {
        return try {
            val response = api.getTransactionDetails(transactionId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load transaction detail")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getReceipt(transactionId: String): Resource<ReceiptDto> {
        return try {
            val response = api.getTransactionReceipt(transactionId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load receipt")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
