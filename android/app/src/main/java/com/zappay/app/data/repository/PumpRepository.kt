package com.zappay.app.data.repository

import com.zappay.app.data.remote.api.ZapPayApi
import com.zappay.app.data.remote.dto.*
import com.zappay.app.util.Resource
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
            else Resource.Error(response.errorBody()?.string() ?: "Registration failed")
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

    suspend fun processPurchase(request: FuelPurchaseRequest): Resource<GenericResponse> {
        return try {
            val response = api.fuelPurchase(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error(response.errorBody()?.string() ?: "Purchase failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun processPurchaseByVehicle(request: PurchaseByVehicleRequest): Resource<GenericResponse> {
        return try {
            val response = api.fuelPurchaseByVehicle(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error(response.errorBody()?.string() ?: "Purchase failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getSettings(pumpId: Int): Resource<GenericResponse> {
        return try {
            val response = api.getPumpSettings(pumpId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load settings")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun saveSettings(body: Map<String, Any>): Resource<GenericResponse> {
        return try {
            val response = api.savePumpSettings(body)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to save settings")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    // ── ZapPay 2.0 Forecourt Operations ─────────────────────────────────
    suspend fun scanForecourtVehicle(token: String, stationId: Int = 1): Resource<ScanVehicleApiResponse> {
        return try {
            val response = api.scanVehicle(ScanVehicleApiRequest(qrPublicToken = token, stationId = stationId))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Vehicle verification failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network connection error")
        }
    }

    suspend fun verifyMeterOCR(
        volumeLitres: Double,
        amount: Double,
        fuelRate: Double,
        density: Double = 742.0,
        manualOverride: Boolean = false,
    ): Resource<VerifyMeterOCRApiResponse> {
        return try {
            val response = api.verifyMeterOCR(
                VerifyMeterOCRApiRequest(
                    volumeLitres = volumeLitres,
                    amount = amount,
                    fuelRate = fuelRate,
                    density = density,
                    manualOverride = manualOverride,
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Meter OCR validation failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network connection error")
        }
    }

    suspend fun executeForecourtDebit(
        qrToken: String,
        fuelDispensed: String,
        volumeLitres: Double,
        fuelRate: Double,
        amount: Double,
        stationId: Int = 1,
        density: Double = 742.0,
        manualOverride: Boolean = false,
    ): Resource<ExecuteDebitApiResponse> {
        return try {
            val response = api.executeDebit(
                ExecuteDebitApiRequest(
                    qrPublicToken = qrToken,
                    fuelDispensed = fuelDispensed,
                    volumeLitres = volumeLitres,
                    fuelRate = fuelRate,
                    amount = amount,
                    stationId = stationId,
                    density = density,
                    manualOverride = manualOverride,
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Debit execution failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network connection error")
        }
    }

    suspend fun getForecourtShiftSummary(stationId: Int = 1): Resource<ShiftSummaryApiResponse> {
        return try {
            val response = api.getShiftSummary(stationId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to fetch shift summary")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
