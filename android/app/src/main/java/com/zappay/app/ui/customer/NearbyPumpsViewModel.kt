package com.zappay.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zappay.app.data.remote.dto.NearbyPumpDto
import com.zappay.app.data.repository.PumpRepository
import com.zappay.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NearbyPumpsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val nearbyPumps: List<NearbyPumpDto> = emptyList(),
)

@HiltViewModel
class NearbyPumpsViewModel @Inject constructor(
    private val pumpRepository: PumpRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NearbyPumpsUiState())
    val uiState: StateFlow<NearbyPumpsUiState> = _uiState

    fun loadNearbyPumps(lat: Double, lng: Double, radius: Double = 10.0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = pumpRepository.getNearbyPumps(lat, lng, radius)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nearbyPumps = result.data.nearbyPumps,
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
