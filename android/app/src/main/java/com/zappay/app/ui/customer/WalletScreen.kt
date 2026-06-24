package com.zappay.app.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import com.zappay.app.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadDashboard(); viewModel.clearRechargeSuccess() }

    Scaffold(
        topBar = { ZapPayTopBar(title = "Wallet", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            BalanceCard(balance = state.balance)

            Spacer(Modifier.height(24.dp))

            SectionHeader(title = "Add Money")
            Spacer(Modifier.height(12.dp))

            ZapPayInput(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount (₹)",
                placeholder = "Enter amount",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                leadingIcon = Icons.Outlined.CurrencyRupee,
            )
            Spacer(Modifier.height(12.dp))

            // Quick amounts
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(500, 1000, 2000, 5000).forEach { amt ->
                    FilterChip(
                        selected = amount.toDoubleOrNull() == amt.toDouble(),
                        onClick = { amount = amt.toString() },
                        label = { Text("₹$amt", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary50,
                            selectedLabelColor = Primary700,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            ZapPayButton(
                text = if (state.isLoading) "Processing..." else "Proceed to Pay",
                onClick = { viewModel.recharge(amount.toDoubleOrNull() ?: 0.0) },
                isLoading = state.isLoading,
                enabled = (amount.toDoubleOrNull() ?: 0.0) >= 100,
            )

            // Error
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                ErrorMessage(state.error!!)
            }

            Spacer(Modifier.height(24.dp))

            // Recent transactions
            SectionHeader(title = "Recent Transactions")
            Spacer(Modifier.height(8.dp))

            if (state.transactions.isEmpty()) {
                ZapPayEmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No transactions",
                    subtitle = "Your recent transactions will appear here",
                )
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        state.transactions.take(5).forEach { tx ->
                            TransactionItem(
                                title = tx.fuelType ?: tx.transactionType.replace("_", " "),
                                subtitle = tx.createdAt.formatDate(),
                                amount = tx.amount,
                                isCredit = tx.transactionType == "wallet_recharge",
                            )
                            if (tx != state.transactions.take(5).last()) {
                                HorizontalDivider(
                                    Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
