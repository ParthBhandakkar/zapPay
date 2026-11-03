package com.zappay.app.ui.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zappay.app.data.remote.dto.FuelPriceDto
import com.zappay.app.data.repository.PumpRepository
import com.zappay.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PumpDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pumpId: Int = 0,
    val pumpName: String = "",
    val address: String = "",
    val distanceKm: Double = 0.0,
    val isOpen: Boolean = true,
    val fuelPrices: List<FuelPriceDto> = emptyList(),
)

@HiltViewModel
class PumpDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pumpRepository: PumpRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PumpDetailUiState())
    val uiState: StateFlow<PumpDetailUiState> = _uiState

    init {
        val pumpId = savedStateHandle.get<String>("pumpId")?.toIntOrNull() ?: 0
        val pumpName = savedStateHandle.get<String>("pumpName") ?: "Pump"
        val address = savedStateHandle.get<String>("address") ?: ""
        val distanceKm = savedStateHandle.get<String>("distanceKm")?.toDoubleOrNull() ?: 0.0
        val isOpen = savedStateHandle.get<String>("isOpen")?.toBoolean() ?: true

        _uiState.value = _uiState.value.copy(
            pumpId = pumpId, pumpName = pumpName,
            address = address, distanceKm = distanceKm, isOpen = isOpen,
        )

        if (pumpId > 0) loadFuelPrices(pumpId)
    }

    private fun loadFuelPrices(pumpId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = pumpRepository.getFuelPrices(pumpId)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    fuelPrices = result.data.fuelPrices,
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                else -> _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
