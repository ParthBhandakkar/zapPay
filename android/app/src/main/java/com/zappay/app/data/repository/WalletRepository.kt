package com.zappay.app.data.repository

import com.zappay.app.data.remote.api.ZapPayApi
import com.zappay.app.data.remote.dto.*
import com.zappay.app.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val api: ZapPayApi,
) {
    suspend fun getBalance(): Resource<WalletResponse> {
        return try {
            val response = api.getWalletBalance()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to fetch balance")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getSummary(): Resource<WalletSummaryResponse> {
        return try {
            val response = api.getWalletSummary()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to fetch summary")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun createRechargeOrder(amount: Double): Resource<RechargeOrderResponse> {
        return try {
            val response = api.createRechargeOrder(RechargeOrderRequest(amount))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to create order")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun testRecharge(amount: Double): Resource<GenericResponse> {
        return try {
            val response = api.testRecharge(amount)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Recharge failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
