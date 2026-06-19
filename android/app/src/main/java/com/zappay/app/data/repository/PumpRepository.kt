package com.zappay.app.data.repository

import com.zappay.app.data.remote.api.ZapPayApi
import com.zappay.app.data.remote.dto.*
import com.zappay.app.util.Resource
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PumpRepository @Inject constructor(
    private val api: ZapPayApi,
) {
    suspend fun getMyPump(): Resource<PumpResponse> {
        return try {
            val response = api.getMyPump()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("No pump associated")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun registerPump(request: PumpRegisterRequest): Resource<GenericResponse> {
        return try {
            val response = api.registerPump(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    val json = org.json.JSONObject(errorBody ?: "")
                    val error = json.optJSONObject("error")
                    error?.optString("message") ?: json.optString("message", "Registration failed")
                } catch (_: Exception) {
                    errorBody ?: "Registration failed"
                }
                Resource.Error(message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getDashboard(pumpId: Int): Resource<PumpDashboardResponse> {
        return try {
            val response = api.getPumpDashboard(pumpId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load dashboard")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun validateQR(qrData: String): Resource<ValidatedCustomerResponse> {
        return try {
            val response = api.validateQR(QRValidateRequest(qrData))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Invalid QR")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun lookupVehicle(vehicleNumber: String): Resource<VehicleLookupResponse> {
        return try {
            val response = api.lookupVehicle(mapOf("vehicle_number" to vehicleNumber))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Lookup failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun processPurchase(request: FuelPurchaseRequest, idempotencyKey: String = UUID.randomUUID().toString()): Resource<GenericResponse> {
        return try {
            val req = request.copy(idempotencyKey = idempotencyKey)
            val response = api.fuelPurchase(req)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error(response.errorBody()?.string() ?: "Purchase failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun processPurchaseByVehicle(request: PurchaseByVehicleRequest, idempotencyKey: String = UUID.randomUUID().toString()): Resource<GenericResponse> {
        return try {
            val req = request.copy(idempotencyKey = idempotencyKey)
            val response = api.fuelPurchaseByVehicle(req)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error(response.errorBody()?.string() ?: "Purchase failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getNearbyPumps(latitude: Double, longitude: Double, radiusKm: Double = 10.0): Resource<NearbyPumpsResponse> {
        return try {
            val response = api.getNearbyPumps(latitude, longitude, radiusKm)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load nearby pumps")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getFuelPrices(pumpId: Int): Resource<FuelPricesResponse> {
        return try {
            val response = api.getPumpFuelPrices(pumpId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load fuel prices")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getPumpShifts(pumpId: Int): Resource<ShiftsResponse> {
        return try {
            val response = api.getPumpShifts(pumpId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load shifts")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun startShift(pumpId: Int, shiftType: String = "morning"): Resource<GenericResponse> {
        return try {
            val response = api.startShift(pumpId, ShiftStartRequest(shiftType))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to start shift")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun endShift(pumpId: Int): Resource<GenericResponse> {
        return try {
            val response = api.endShift(pumpId, mapOf("notes" to ""))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to end shift")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getInventory(pumpId: Int): Resource<PumpInventoryResponse> {
        return try {
            val response = api.getPumpInventory(pumpId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load inventory")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getSettings(pumpId: Int): Resource<PumpSettingsResponse> {
        return try {
            val response = api.getPumpSettings(pumpId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load settings")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun saveSettings(pumpId: Int, fuelTypes: String, fuelRates: String, isOpen: Boolean): Resource<PumpSettingsResponse> {
        return try {
            val response = api.savePumpSettings(SaveSettingsRequest(pumpId, fuelTypes, fuelRates, isOpen))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    val json = org.json.JSONObject(errorBody ?: "")
                    val error = json.optJSONObject("error")
                    error?.optString("message") ?: json.optString("message", "Failed to save settings")
                } catch (_: Exception) {
                    errorBody ?: "Failed to save settings"
                }
                Resource.Error(message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
