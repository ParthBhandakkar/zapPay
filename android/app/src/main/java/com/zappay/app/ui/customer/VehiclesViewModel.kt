package com.zappay.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zappay.app.data.remote.dto.VehicleAddRequest
import com.zappay.app.data.remote.dto.VehicleDto
import com.zappay.app.data.remote.dto.VehicleUpdateRequest
import com.zappay.app.data.repository.UserRepository
import com.zappay.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehiclesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vehicles: List<VehicleDto> = emptyList(),
    val successMessage: String? = null,
)

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiclesUiState())
    val uiState: StateFlow<VehiclesUiState> = _uiState

    fun loadVehicles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = userRepository.getVehicles()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    vehicles = result.data,
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                else -> {}
            }
        }
    }

    fun addVehicle(vehicleNumber: String, vehicleType: String?, nickname: String?, isPrimary: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val request = VehicleAddRequest(
                vehicleNumber = vehicleNumber,
                vehicleType = vehicleType,
                nickname = nickname,
                isPrimary = isPrimary,
            )
            when (val result = userRepository.addVehicle(request)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                    )
                    loadVehicles()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                else -> {}
            }
        }
    }

    fun updateVehicle(id: Int, vehicleNumber: String?, vehicleType: String?, nickname: String?, isPrimary: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val request = VehicleUpdateRequest(
                vehicleNumber = vehicleNumber,
                vehicleType = vehicleType,
                nickname = nickname,
                isPrimary = isPrimary,
            )
            when (val result = userRepository.updateVehicle(id, request)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                    )
                    loadVehicles()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                else -> {}
            }
        }
    }

    fun removeVehicle(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = userRepository.removeVehicle(id)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                    )
                    loadVehicles()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                else -> {}
            }
        }
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
