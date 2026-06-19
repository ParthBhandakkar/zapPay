package com.zappay.app.ui.pump

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zappay.app.data.remote.dto.ReceiptDto
import com.zappay.app.data.repository.TransactionRepository
import com.zappay.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptUiState(
    val isLoading: Boolean = true,
    val receipt: ReceiptDto? = null,
    val error: String? = null,
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository,
) : ViewModel() {

    private val transactionId: String = savedStateHandle["transactionId"] ?: ""

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState

    init {
        loadReceipt()
    }

    private fun loadReceipt() {
        viewModelScope.launch {
            _uiState.value = ReceiptUiState(isLoading = true)
            when (val result = repository.getReceipt(transactionId)) {
                is Resource.Success -> _uiState.value = ReceiptUiState(isLoading = false, receipt = result.data)
                is Resource.Error -> _uiState.value = ReceiptUiState(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }
}
