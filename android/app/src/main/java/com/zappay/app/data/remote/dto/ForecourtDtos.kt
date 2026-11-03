package com.zappay.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScanVehicleApiRequest(
    @Json(name = "qr_public_token") val qrPublicToken: String,
    @Json(name = "station_id") val stationId: Int? = 1,
)

@JsonClass(generateAdapter = true)
data class ScanVehicleApiResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "verified") val verified: Boolean,
    @Json(name = "qr_token") val qrToken: String?,
    @Json(name = "vehicle_id") val vehicleId: Int?,
    @Json(name = "vrn_number") val vrnNumber: String?,
    @Json(name = "make_model") val makeModel: String?,
    @Json(name = "permitted_fuel_type") val permittedFuelType: String?,
    @Json(name = "fuel_lock_message") val fuelLockMessage: String?,
    @Json(name = "max_per_txn_limit") val maxPerTxnLimit: Double?,
    @Json(name = "active_balance") val activeBalance: Double?,
    @Json(name = "owner_name") val ownerName: String?,
    @Json(name = "owner_phone_masked") val ownerPhoneMasked: String?,
    @Json(name = "geofence_verified") val geofenceVerified: Boolean?,
)

@JsonClass(generateAdapter = true)
data class VerifyMeterOCRApiRequest(
    @Json(name = "volume_litres") val volumeLitres: Double,
    @Json(name = "amount") val amount: Double,
    @Json(name = "fuel_rate") val fuelRate: Double,
    @Json(name = "density") val density: Double? = 742.0,
    @Json(name = "confidence_score") val confidenceScore: Double? = 0.99,
    @Json(name = "manual_override") val manualOverride: Boolean? = false,
)

@JsonClass(generateAdapter = true)
data class VerifyMeterOCRApiResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "math_verified") val mathVerified: Boolean,
    @Json(name = "difference") val difference: Double,
    @Json(name = "expected_amount") val expectedAmount: Double,
    @Json(name = "scanned_amount") val scannedAmount: Double,
    @Json(name = "scanned_litres") val scannedLitres: Double,
    @Json(name = "scanned_density") val scannedDensity: Double,
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
)

@JsonClass(generateAdapter = true)
data class ExecuteDebitApiRequest(
    @Json(name = "qr_public_token") val qrPublicToken: String,
    @Json(name = "fuel_dispensed") val fuelDispensed: String,
    @Json(name = "volume_litres") val volumeLitres: Double,
    @Json(name = "fuel_rate") val fuelRate: Double,
    @Json(name = "amount") val amount: Double,
    @Json(name = "station_id") val stationId: Int? = 1,
    @Json(name = "density") val density: Double? = 742.0,
    @Json(name = "manual_override") val manualOverride: Boolean? = false,
)

@JsonClass(generateAdapter = true)
data class ExecuteDebitApiResponse(
    @Json(name = "status") val status: String,
    @Json(name = "transaction_id") val transactionId: String,
    @Json(name = "npci_rrn") val npciRrn: String?,
    @Json(name = "vrn_number") val vrnNumber: String?,
    @Json(name = "fuel_dispensed") val fuelDispensed: String?,
    @Json(name = "volume_litres") val volumeLitres: Double?,
    @Json(name = "gross_amount") val grossAmount: Double?,
    @Json(name = "net_dealer_settlement") val netDealerSettlement: Double?,
    @Json(name = "soundbox_payload") val soundboxPayload: String?,
    @Json(name = "soundbox_tts_english") val soundboxTtsEnglish: String?,
    @Json(name = "completed_at") val completedAt: String?,
)

@JsonClass(generateAdapter = true)
data class ShiftSummaryApiResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "transactions_count") val transactionsCount: Int,
    @Json(name = "total_litres_dispensed") val totalLitresDispensed: Double,
    @Json(name = "total_collection_inr") val totalCollectionInr: Double,
    @Json(name = "zero_cash_discrepancy") val zeroCashDiscrepancy: Boolean,
)

// ── Consumer DTOs ─────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class VehicleDto(
    @Json(name = "id") val id: Int,
    @Json(name = "vrn_number") val vrnNumber: String,
    @Json(name = "make_model") val makeModel: String?,
    @Json(name = "fuel_type") val fuelType: String?,
    @Json(name = "vehicle_class") val vehicleClass: String?,
    @Json(name = "vahan_verified") val vahanVerified: Boolean?,
    @Json(name = "tank_capacity_litres") val tankCapacityLitres: Double?,
    @Json(name = "insurance_expiry_date") val insuranceExpiryDate: String?,
    @Json(name = "puc_expiry_date") val pucExpiryDate: String?,
    @Json(name = "avg_mileage_kmpl") val avgMileageKmpl: Double?,
    @Json(name = "last_odometer_km") val lastOdometerKm: Int?,
    @Json(name = "blood_group") val bloodGroup: String?,
    @Json(name = "emergency_contact_name") val emergencyContactName: String?,
    @Json(name = "emergency_contact_phone") val emergencyContactPhone: String?,
)

@JsonClass(generateAdapter = true)
data class VehiclesListResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "vehicles") val vehicles: List<VehicleDto>,
)

@JsonClass(generateAdapter = true)
data class MandateDto(
    @Json(name = "id") val id: Int,
    @Json(name = "rail_type") val railType: String,
    @Json(name = "ummn_reference") val ummnReference: String?,
    @Json(name = "payer_vpa") val payerVpa: String?,
    @Json(name = "max_limit") val maxLimit: Double,
    @Json(name = "status") val status: String,
    @Json(name = "delegated_to_name") val delegatedToName: String?,
    @Json(name = "delegated_to_phone") val delegatedToPhone: String?,
    @Json(name = "valid_until") val validUntil: String?,
)

@JsonClass(generateAdapter = true)
data class MandatesListResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "mandates") val mandates: List<MandateDto>,
)

@JsonClass(generateAdapter = true)
data class StationDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "city") val city: String,
    @Json(name = "verified_petrol_density") val verifiedPetrolDensity: Double?,
    @Json(name = "verified_diesel_density") val verifiedDieselDensity: Double?,
    @Json(name = "trust_score") val trustScore: Double?,
    @Json(name = "mdr_mode") val mdrMode: String?,
)

@JsonClass(generateAdapter = true)
data class NearbyStationsResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "stations") val stations: List<StationDto>,
)

