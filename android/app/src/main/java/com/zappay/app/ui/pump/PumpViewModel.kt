package com.zappay.app.ui.pump

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zappay.app.data.repository.PumpRepository
import com.zappay.app.data.repository.WalletRepository
import com.zappay.app.data.remote.dto.*
import com.zappay.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PumpUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // Dashboard
    val totalTransactions: Int = 0,
    val totalRevenue: Double = 0.0,
    val transactionsToday: Int = 0,
    val revenueToday: Double = 0.0,
    val recentTransactions: List<TransactionDto> = emptyList(),
    val pumpId: Int? = null,
    val hasPump: Boolean = true,

    // QR validation
    val scannedCustomer: ValidatedCustomerResponse? = null,

    // Vehicle lookup
    val lookedUpVehicle: VehicleLookupResponse? = null,

    // Purchase
    val purchaseSuccess: Boolean = false,
    val purchaseMessage: String? = null,
    val purchaseCustomerName: String? = null,
    val purchaseCustomerPhone: String? = null,
    val purchaseVehicleNumber: String? = null,
    val purchaseFuelType: String? = null,
    val purchaseFuelQuantity: Double? = null,
    val purchaseFuelRate: Double? = null,

    // Settings
    val settings: PumpSettingsData? = null,
    val settingsSaved: Boolean = false,

    // Pump registration
    val registrationSuccess: Boolean = false,
)

@HiltViewModel
class PumpViewModel @Inject constructor(
    private val pumpRepository: PumpRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PumpUiState())
    val uiState: StateFlow<PumpUiState> = _uiState

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val pumpResult = pumpRepository.getMyPump()) {
                is Resource.Success -> {
                    val pump = pumpResult.data
                    _uiState.value = _uiState.value.copy(pumpId = pump.id, hasPump = true)
                    loadPumpDashboard(pump.id)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasPump = false,
                        error = pumpResult.message,
                    )
                }
                else -> {}
            }
        }
    }

    private suspend fun loadPumpDashboard(pumpId: Int) {
        when (val result = pumpRepository.getDashboard(pumpId)) {
            is Resource.Success -> {
                val d = result.data
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalTransactions = d.totalTransactions,
                    totalRevenue = d.totalRevenue,
                    transactionsToday = d.transactionsToday,
                    revenueToday = d.revenueToday,
                    recentTransactions = d.recentTransactions ?: emptyList(),
                )
            }
            is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            else -> {}
        }
    }

    fun validateQR(qrData: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, scannedCustomer = null)
            when (val result = pumpRepository.validateQR(qrData)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, scannedCustomer = result.data)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun lookupVehicle(number: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, lookedUpVehicle = null)
            when (val result = pumpRepository.lookupVehicle(number)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, lookedUpVehicle = result.data)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun processPurchase(
        qrCode: String?,
        userId: Int?,
        fuelType: String,
        fuelQuantity: Double,
        fuelRate: Double,
    ) {
        val pumpId = _uiState.value.pumpId ?: return
        val customer = _uiState.value.scannedCustomer
        val vehicle = _uiState.value.lookedUpVehicle
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = if (qrCode != null) {
                pumpRepository.processPurchase(
                    FuelPurchaseRequest(qrCode, pumpId, fuelType, fuelQuantity, fuelRate)
                )
            } else if (userId != null) {
                pumpRepository.processPurchaseByVehicle(
                    PurchaseByVehicleRequest(userId, pumpId, fuelType, fuelQuantity, fuelRate)
                )
            } else {
                Resource.Error("No customer selected")
            }
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        purchaseSuccess = true,
                        purchaseMessage = result.data.message,
                        purchaseCustomerName = customer?.userName ?: vehicle?.userName,
                        purchaseCustomerPhone = customer?.userPhone ?: vehicle?.userPhone,
                        purchaseVehicleNumber = customer?.vehicleNumber ?: vehicle?.vehicleNumber,
                        purchaseFuelType = fuelType,
                        purchaseFuelQuantity = fuelQuantity,
                        purchaseFuelRate = fuelRate,
                    )
                    loadDashboard()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun registerPump(request: PumpRegisterRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = pumpRepository.registerPump(request)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, registrationSuccess = true)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            var pumpId = _uiState.value.pumpId
            if (pumpId == null) {
                when (val result = pumpRepository.getMyPump()) {
                    is Resource.Success -> {
                        pumpId = result.data.id
                        _uiState.value = _uiState.value.copy(pumpId = pumpId)
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(error = result.message)
                        return@launch
                    }
                    else -> return@launch
                }
            }
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, settingsSaved = false)
            when (val result = pumpRepository.getSettings(pumpId!!)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, settings = result.data.data)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun saveSettings(fuelTypes: String, fuelRates: String, isOpen: Boolean) {
        viewModelScope.launch {
            var pumpId = _uiState.value.pumpId
            if (pumpId == null) {
                when (val result = pumpRepository.getMyPump()) {
                    is Resource.Success -> {
                        pumpId = result.data.id
                        _uiState.value = _uiState.value.copy(pumpId = pumpId)
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(error = result.message)
                        return@launch
                    }
                    else -> return@launch
                }
            }
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, settingsSaved = false)
            when (val result = pumpRepository.saveSettings(pumpId!!, fuelTypes, fuelRates, isOpen)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    settings = result.data.data,
                    settingsSaved = true,
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun clearPurchaseSuccess() {
        _uiState.value = _uiState.value.copy(
            purchaseSuccess = false,
            purchaseMessage = null,
            purchaseCustomerName = null,
            purchaseCustomerPhone = null,
            purchaseVehicleNumber = null,
            purchaseFuelType = null,
            purchaseFuelQuantity = null,
            purchaseFuelRate = null,
        )
    }

    fun clearRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(registrationSuccess = false)
    }

    fun clearSettingsSaved() {
        _uiState.value = _uiState.value.copy(settingsSaved = false)
    }

    fun clearScannedCustomer() {
        _uiState.value = _uiState.value.copy(scannedCustomer = null)
    }

    fun clearLookedUpVehicle() {
        _uiState.value = _uiState.value.copy(lookedUpVehicle = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
