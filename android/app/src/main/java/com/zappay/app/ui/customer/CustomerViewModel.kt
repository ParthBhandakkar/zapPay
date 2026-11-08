package com.zappay.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zappay.app.data.local.TokenManager
import com.zappay.app.data.remote.api.ZapPayApi
import com.zappay.app.data.remote.dto.ConsumerVehicleDto
import com.zappay.app.data.remote.dto.MandateDto
import com.zappay.app.data.remote.dto.StationDto
import com.zappay.app.data.remote.dto.TransactionDto
import com.zappay.app.data.remote.dto.UserProfileDto
import com.zappay.app.data.remote.dto.VehicleDto
import com.zappay.app.data.remote.dto.EvStationDto
import com.zappay.app.data.remote.dto.EvConnectorDto
import kotlin.math.roundToInt
import com.zappay.app.data.repository.QRRepository
import com.zappay.app.data.repository.TransactionRepository
import com.zappay.app.data.repository.UserRepository
import com.zappay.app.data.repository.WalletRepository
import com.zappay.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerUiState(
    val isLoading: Boolean = false,
    val balance: Double = 2500.0,
    val totalRecharged: Double = 0.0,
    val totalSpent: Double = 0.0,
    val monthlySpending: Double = 0.0,
    val transactions: List<TransactionDto> = emptyList(),
    val qrData: String? = null,
    val qrExpiresAt: String? = null,
    val error: String? = null,
    val rechargeSuccess: Boolean = false,
    val vehicleNumber: String = "",
    val vehicleType: String = "",
    val vehicles: List<VehicleDto> = emptyList(),
    val qrCodesByVehicle: Map<Int, String> = emptyMap(),
    val selectedVehicleIndex: Int = 0,
    val profile: UserProfileDto? = null,
    val profileSaving: Boolean = false,
    val profileSaved: Boolean = false,

    // ZapPay 2.0 Forecourt Ecosystem
    val consumerVehicles: List<ConsumerVehicleDto> = emptyList(),
    val mandates: List<MandateDto> = emptyList(),
    val nearbyStations: List<StationDto> = emptyList(),
    val alertSentMessage: String? = null,

    // ZapPay 2.0 EV Mode Ecosystem
    val isEvMode: Boolean = false,
    val evBatterySoC: Int = 68,
    val evRangeKm: Int = 284,
    val evBatteryHealthSoh: Double = 98.5,
    val evCabinPreconditioned: Boolean = false,
    val evPortLocked: Boolean = true,
    val evTargetSocLimit: Int = 80,
    val isCharging: Boolean = false,
    val chargingSpeedKw: Double = 48.5,
    val chargingKwhDelivered: Double = 14.6,
    val chargingTimeRemainingMins: Int = 18,
    val chargingSessionCost: Double = 270.10,
    val chargingStationName: String = "Tata Power EZ Charge - Baner Hub",
    val chargingConnectorType: String = "CCS2 60kW DC (Gun #2)",
    val reservedGunId: String? = null,
    val reservedStationName: String? = null,
    val reservationTimeLeftMins: Int = 15,
    val evStations: List<EvStationDto> = emptyList(),
    val selectedEvStation: EvStationDto? = null,
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val qrRepository: QRRepository,
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository,
    private val api: ZapPayApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            loadProfile()
            loadWalletSummary()
            loadTransactions()
            loadVehicleInfo()
            loadConsumerVehicles()
            loadVehicles()
            loadMandates()
            loadNearbyStations()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun loadConsumerVehicles() {
        viewModelScope.launch {
            try {
                val res = api.getConsumerVehicles()
                if (res.isSuccessful && res.body() != null) {
                    val cVehicles = res.body()!!.vehicles
                    val fallbackVehicles = if (_uiState.value.vehicles.isEmpty()) {
                        cVehicles.mapIndexed { idx, cv ->
                            VehicleDto(
                                id = cv.id,
                                userId = 1,
                                vehicleNumber = cv.vrnNumber,
                                vehicleType = "Car",
                                nickname = cv.makeModel,
                                isPrimary = idx == 0,
                                isActive = true,
                            )
                        }
                    } else _uiState.value.vehicles
                    val newQrMap = _uiState.value.qrCodesByVehicle.toMutableMap()
                    cVehicles.forEach { cv ->
                        if (!newQrMap.containsKey(cv.id)) {
                            newQrMap[cv.id] = "ZP-TAG-${cv.vrnNumber}"
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        consumerVehicles = cVehicles,
                        vehicles = fallbackVehicles,
                        qrCodesByVehicle = newQrMap,
                    )
                }
            } catch (e: Exception) {
                // Handled gracefully
            }
        }
    }

    fun loadMandates() {
        viewModelScope.launch {
            try {
                val res = api.getConsumerMandates()
                if (res.isSuccessful && res.body() != null) {
                    _uiState.value = _uiState.value.copy(mandates = res.body()!!.mandates)
                }
            } catch (e: Exception) {
                // Handled gracefully
            }
        }
    }

    fun loadNearbyStations() {
        viewModelScope.launch {
            try {
                val res = api.getNearbyStations()
                if (res.isSuccessful && res.body() != null) {
                    _uiState.value = _uiState.value.copy(nearbyStations = res.body()!!.stations)
                }
            } catch (e: Exception) {
                // Handled gracefully
            }
        }
    }

    fun loadVehicleInfo() {
        viewModelScope.launch {
            val vn = tokenManager.getVehicleNumber() ?: ""
            val vt = tokenManager.getVehicleType() ?: ""
            _uiState.value = _uiState.value.copy(vehicleNumber = vn, vehicleType = vt)
        }
    }

    fun saveVehicleInfo(vehicleNumber: String, vehicleType: String) {
        viewModelScope.launch {
            tokenManager.saveVehicleInfo(vehicleNumber, vehicleType)
            _uiState.value = _uiState.value.copy(vehicleNumber = vehicleNumber, vehicleType = vehicleType)
        }
    }

    private suspend fun loadWalletSummary() {
        when (val result = walletRepository.getSummary()) {
            is Resource.Success -> {
                _uiState.value = _uiState.value.copy(
                    balance = result.data.balance,
                    totalRecharged = result.data.totalRecharged,
                    totalSpent = result.data.totalSpent,
                    monthlySpending = result.data.monthlySpending,
                )
            }
            is Resource.Error -> _uiState.value = _uiState.value.copy(error = result.message)
            else -> {}
        }
    }

    private suspend fun loadTransactions() {
        when (val result = transactionRepository.getHistory(page = 1, pageSize = 10)) {
            is Resource.Success -> _uiState.value = _uiState.value.copy(transactions = result.data.transactions)
            else -> {}
        }
    }

    fun recharge(amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = walletRepository.testRecharge(amount)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, rechargeSuccess = true, error = null)
                    loadDashboard()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun generateQR(vehicleId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = qrRepository.generateQR(vehicleId = vehicleId)) {
                is Resource.Success -> {
                    val newMap = _uiState.value.qrCodesByVehicle.toMutableMap()
                    if (vehicleId != null) newMap[vehicleId] = result.data.qrCode
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        qrData = result.data.qrCode,
                        qrExpiresAt = result.data.expiresAt,
                        qrCodesByVehicle = newMap,
                    )
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun loadVehicles() {
        viewModelScope.launch {
            when (val result = userRepository.getVehicles()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(vehicles = result.data)
                    result.data.forEachIndexed { _, v ->
                        qrRepository.generateQR(vehicleId = v.id).let { qrResult ->
                            if (qrResult is Resource.Success) {
                                val newMap = _uiState.value.qrCodesByVehicle.toMutableMap()
                                newMap[v.id] = qrResult.data.qrCode
                                _uiState.value = _uiState.value.copy(qrCodesByVehicle = newMap)
                            }
                        }
                    }
                }
                is Resource.Error -> {}
                else -> {}
            }
        }
    }

    fun selectVehicleIndex(index: Int) {
        _uiState.value = _uiState.value.copy(selectedVehicleIndex = index)
    }

    fun loadProfile() {
        viewModelScope.launch {
            when (val result = userRepository.getProfile()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(profile = result.data)
                else -> {}
            }
        }
    }

    fun saveProfile(body: Map<String, String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(profileSaving = true, profileSaved = false)
            when (val result = userRepository.updateProfile(body)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    profile = result.data,
                    profileSaving = false,
                    profileSaved = true,
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    profileSaving = false,
                    error = result.message,
                )
                else -> {}
            }
        }
    }

    fun clearProfileSaved() {
        _uiState.value = _uiState.value.copy(profileSaved = false)
    }

    fun clearRechargeSuccess() {
        _uiState.value = _uiState.value.copy(rechargeSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // ── EV Mode & Charging ───────────────────────────────────────────
    private var chargingJob: kotlinx.coroutines.Job? = null

    val defaultEvStations = listOf(
        EvStationDto(
            id = 101,
            name = "Tata Power EZ Charge - Baner Expressway Hub",
            operator = "Tata Power",
            address = "Baner-Pashan Link Road, Near Highway Exit",
            city = "Pune",
            distanceKm = 1.2,
            latitude = 18.5590,
            longitude = 73.7868,
            totalGuns = 4,
            availableGuns = 3,
            connectors = listOf(
                EvConnectorDto("GUN-TP-01", "CCS2 (DC Fast)", 60.0, 18.50, "AVAILABLE"),
                EvConnectorDto("GUN-TP-02", "CCS2 (DC Fast)", 60.0, 18.50, "AVAILABLE"),
                EvConnectorDto("GUN-TP-03", "Type 2 (AC)", 22.0, 14.00, "AVAILABLE"),
                EvConnectorDto("GUN-TP-04", "CCS2 (DC Fast)", 60.0, 18.50, "OCCUPIED"),
            ),
            amenities = listOf("Café", "Restroom", "24x7 Security", "Free WiFi"),
            rating = 4.92,
            isOpen = true,
        ),
        EvStationDto(
            id = 102,
            name = "Jio-bp pulse Supercharging - Hinjawadi",
            operator = "Jio-bp pulse",
            address = "Hinjawadi Phase 1, Near Cognizant Circle",
            city = "Pune",
            distanceKm = 3.8,
            latitude = 18.5913,
            longitude = 73.7389,
            totalGuns = 4,
            availableGuns = 2,
            connectors = listOf(
                EvConnectorDto("GUN-JBP-01", "Dual CCS2 (Ultra Fast)", 120.0, 19.20, "AVAILABLE"),
                EvConnectorDto("GUN-JBP-02", "Dual CCS2 (Ultra Fast)", 120.0, 19.20, "AVAILABLE"),
                EvConnectorDto("GUN-JBP-03", "CCS2 (DC Fast)", 60.0, 18.50, "OCCUPIED"),
                EvConnectorDto("GUN-JBP-04", "Type 2 (AC)", 22.0, 14.00, "OCCUPIED"),
            ),
            amenities = listOf("24/7 Food Court", "Restroom", "Air Pressure"),
            rating = 4.88,
            isOpen = true,
        ),
        EvStationDto(
            id = 103,
            name = "Statiq Mega EV Hub - Urse Toll Plaza",
            operator = "Statiq",
            address = "Mumbai-Pune Expressway Km 42, Urse",
            city = "Pune",
            distanceKm = 8.4,
            latitude = 18.7214,
            longitude = 73.6821,
            totalGuns = 8,
            availableGuns = 6,
            connectors = listOf(
                EvConnectorDto("GUN-STQ-01", "CCS2 (Ultra Fast)", 150.0, 17.80, "AVAILABLE"),
                EvConnectorDto("GUN-STQ-02", "CCS2 (Ultra Fast)", 150.0, 17.80, "AVAILABLE"),
                EvConnectorDto("GUN-STQ-03", "CCS2 (DC Fast)", 60.0, 17.80, "AVAILABLE"),
                EvConnectorDto("GUN-STQ-04", "Type 2 (AC)", 22.0, 13.50, "AVAILABLE"),
            ),
            amenities = listOf("Lounge", "Restaurant", "Clean Washrooms"),
            rating = 4.95,
            isOpen = true,
        ),
        EvStationDto(
            id = 104,
            name = "Zeon Fast Charging - Balewadi High Street",
            operator = "Zeon",
            address = "Balewadi High Street Mall Parking, B1",
            city = "Pune",
            distanceKm = 4.1,
            latitude = 18.5721,
            longitude = 73.7745,
            totalGuns = 2,
            availableGuns = 1,
            connectors = listOf(
                EvConnectorDto("GUN-ZN-01", "CCS2 (DC Fast)", 50.0, 18.00, "AVAILABLE"),
                EvConnectorDto("GUN-ZN-02", "Type 2 (AC)", 22.0, 14.00, "OCCUPIED"),
            ),
            amenities = listOf("Mall Access", "Food & Dining"),
            rating = 4.82,
            isOpen = true,
        ),
    )

    fun setEvMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isEvMode = enabled)
        if (enabled && _uiState.value.evStations.isEmpty()) {
            loadEvStations()
        }
    }

    fun loadEvStations() {
        viewModelScope.launch {
            try {
                val res = api.getEvStations()
                if (res.isSuccessful && res.body() != null && res.body()!!.stations.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(evStations = res.body()!!.stations)
                } else {
                    _uiState.value = _uiState.value.copy(evStations = defaultEvStations)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(evStations = defaultEvStations)
            }
        }
    }

    fun startLiveCharging(stationName: String? = null, connector: String? = null) {
        chargingJob?.cancel()
        val stName = stationName ?: _uiState.value.chargingStationName
        val conn = connector ?: _uiState.value.chargingConnectorType
        _uiState.value = _uiState.value.copy(
            isCharging = true,
            chargingStationName = stName,
            chargingConnectorType = conn,
            chargingSpeedKw = 48.2,
            chargingKwhDelivered = 0.4,
            chargingSessionCost = 7.40,
            chargingTimeRemainingMins = 24,
            alertSentMessage = "Charging handshake complete. Auto-charging locked to $stName!",
        )
        chargingJob = viewModelScope.launch {
            while (_uiState.value.isCharging) {
                kotlinx.coroutines.delay(1500)
                val currentKwh = _uiState.value.chargingKwhDelivered + 0.35
                val currentCost = currentKwh * 18.50
                val currentSoc = if (_uiState.value.evBatterySoC < _uiState.value.evTargetSocLimit) {
                    _uiState.value.evBatterySoC + 1
                } else {
                    _uiState.value.evBatterySoC
                }
                val remainingMins = maxOf(1, _uiState.value.chargingTimeRemainingMins - 1)
                _uiState.value = _uiState.value.copy(
                    chargingKwhDelivered = (currentKwh * 10.0).roundToInt() / 10.0,
                    chargingSessionCost = (currentCost * 100.0).roundToInt() / 100.0,
                    evBatterySoC = currentSoc,
                    evRangeKm = (currentSoc * 4.18).roundToInt(),
                    chargingTimeRemainingMins = remainingMins,
                )
                if (currentSoc >= _uiState.value.evTargetSocLimit) {
                    stopLiveCharging()
                    break
                }
            }
        }
    }

    fun stopLiveCharging() {
        chargingJob?.cancel()
        val finalCost = _uiState.value.chargingSessionCost
        val finalKwh = _uiState.value.chargingKwhDelivered
        val newBal = maxOf(0.0, _uiState.value.balance - finalCost)
        val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        val newTx = TransactionDto(
            id = (_uiState.value.transactions.maxOfOrNull { it.id } ?: 100) + 1,
            transactionId = "TXN-EV-" + System.currentTimeMillis().toString().takeLast(6),
            userId = 1,
            amount = finalCost,
            commissionAmount = 5.0,
            fuelQuantity = finalKwh,
            fuelType = "EV Charging (CCS2)",
            pumpName = _uiState.value.chargingStationName,
            transactionType = "ev_charging",
            status = "completed",
            createdAt = now,
            completedAt = now,
        )
        _uiState.value = _uiState.value.copy(
            isCharging = false,
            balance = newBal,
            transactions = listOf(newTx) + _uiState.value.transactions,
            alertSentMessage = "Charging stopped! $finalKwh kWh dispensed. ₹${"%.2f".format(finalCost)} auto-debited from wallet.",
        )
    }

    fun toggleCabinPreconditioning() {
        val newState = !_uiState.value.evCabinPreconditioned
        _uiState.value = _uiState.value.copy(
            evCabinPreconditioned = newState,
            alertSentMessage = if (newState) "Cabin AC Pre-conditioning activated to 22°C (Smart Thermostat)" else "Cabin Pre-conditioning turned off",
        )
    }

    fun togglePortLock() {
        val newState = !_uiState.value.evPortLocked
        _uiState.value = _uiState.value.copy(
            evPortLocked = newState,
            alertSentMessage = if (newState) "Charging port locked securely" else "Charging port unlocked",
        )
    }

    fun setTargetSocLimit(limit: Int) {
        _uiState.value = _uiState.value.copy(
            evTargetSocLimit = limit,
            alertSentMessage = "Battery charge limit set to $limit% (Optimized for battery longevity)",
        )
    }

    fun reserveGun(station: EvStationDto, gunId: String) {
        reserveGun(gunId, station.name)
    }

    fun reserveGun(gunId: String, stationName: String) {
        _uiState.value = _uiState.value.copy(
            reservedGunId = gunId,
            reservedStationName = stationName,
            reservationTimeLeftMins = 15,
            alertSentMessage = "Gun $gunId reserved at $stationName for 15 mins. Zero-wait bay unlocked!",
        )
    }

    fun selectEvStation(station: EvStationDto?) {
        _uiState.value = _uiState.value.copy(selectedEvStation = station)
    }

    fun clearAlertSentMessage() {
        _uiState.value = _uiState.value.copy(alertSentMessage = null)
    }
}
