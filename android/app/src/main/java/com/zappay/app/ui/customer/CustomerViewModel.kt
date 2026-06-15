package com.zappay.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zappay.app.data.remote.dto.TransactionDto
import com.zappay.app.data.repository.QRRepository
import com.zappay.app.data.repository.TransactionRepository
import com.zappay.app.data.repository.WalletRepository
import com.zappay.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerUiState(
    val isLoading: Boolean = false,
    val balance: Double = 0.0,
    val totalRecharged: Double = 0.0,
    val totalSpent: Double = 0.0,
    val monthlySpending: Double = 0.0,
    val transactions: List<TransactionDto> = emptyList(),
    val qrData: String? = null,
    val qrExpiresAt: String? = null,
    val error: String? = null,
    val rechargeSuccess: Boolean = false,
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val qrRepository: QRRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            loadWalletSummary()
            loadTransactions()
            _uiState.value = _uiState.value.copy(isLoading = false)
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

    fun generateQR() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = qrRepository.generateQR()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        qrData = result.data.qrCode,
                        qrExpiresAt = result.data.expiresAt,
                    )
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun clearRechargeSuccess() {
        _uiState.value = _uiState.value.copy(rechargeSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
