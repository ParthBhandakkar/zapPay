package com.zappay.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zappay.app.data.remote.dto.SupportTicketDto
import com.zappay.app.data.remote.dto.TicketCreateRequest
import com.zappay.app.data.repository.UserRepository
import com.zappay.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupportTicketsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tickets: List<SupportTicketDto> = emptyList(),
    val createSuccess: Boolean = false,
)

@HiltViewModel
class SupportTicketsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportTicketsUiState())
    val uiState: StateFlow<SupportTicketsUiState> = _uiState

    fun loadTickets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = userRepository.getSupportTickets()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tickets = result.data.tickets,
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                else -> {}
            }
        }
    }

    fun createTicket(subject: String, description: String?, category: String, priority: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val request = TicketCreateRequest(
                subject = subject,
                description = description,
                category = category,
                priority = priority,
            )
            when (val result = userRepository.createSupportTicket(request)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createSuccess = true,
                    )
                    loadTickets()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                else -> {}
            }
        }
    }

    fun clearCreateSuccess() {
        _uiState.value = _uiState.value.copy(createSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
