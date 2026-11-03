package com.zappay.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zappay.app.data.remote.dto.NotificationDto
import com.zappay.app.data.repository.UserRepository
import com.zappay.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val notifications: List<NotificationDto> = emptyList(),
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = userRepository.getNotifications()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    notifications = result.data.notifications,
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                else -> {}
            }
        }
    }

    fun markAsRead(notifId: Int) {
        viewModelScope.launch {
            when (val result = userRepository.markNotificationRead(notifId)) {
                is Resource.Success -> loadNotifications()
                is Resource.Error -> _uiState.value = _uiState.value.copy(error = result.message)
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
