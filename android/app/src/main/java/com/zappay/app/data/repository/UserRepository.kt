package com.zappay.app.data.repository

import com.zappay.app.data.remote.api.ZapPayApi
import com.zappay.app.data.remote.dto.*
import com.zappay.app.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: ZapPayApi,
) {
    suspend fun getVehicles(): Resource<List<VehicleDto>> {
        return try {
            val response = api.getMyVehicles()
            if (response.isSuccessful) Resource.Success(response.body() ?: emptyList())
            else Resource.Error("Failed to load vehicles")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun addVehicle(request: VehicleAddRequest): Resource<GenericResponse> {
        return try {
            val response = api.addVehicle(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to add vehicle")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateVehicle(id: Int, request: VehicleUpdateRequest): Resource<GenericResponse> {
        return try {
            val response = api.updateVehicle(id, request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to update vehicle")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun removeVehicle(id: Int): Resource<GenericResponse> {
        return try {
            val response = api.removeVehicle(id)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to remove vehicle")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getNotifications(): Resource<NotificationsResponse> {
        return try {
            val response = api.getMyNotifications()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load notifications")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun markNotificationRead(notifId: Int): Resource<GenericResponse> {
        return try {
            val response = api.markNotificationRead(notifId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to mark as read")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun createSupportTicket(request: TicketCreateRequest): Resource<GenericResponse> {
        return try {
            val response = api.createSupportTicket(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to create ticket")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getSupportTickets(): Resource<TicketsResponse> {
        return try {
            val response = api.getMySupportTickets()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load tickets")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
