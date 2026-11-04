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
}
