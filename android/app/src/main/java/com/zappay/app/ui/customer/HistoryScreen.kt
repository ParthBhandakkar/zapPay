package com.zappay.app.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import com.zappay.app.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var filter by remember { mutableStateOf("all") }

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    Scaffold(
        topBar = { ZapPayTopBar(title = "Transaction History", onBack = onBack) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Filter chips
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("all" to "All", "fuel_purchase" to "Fuel", "wallet_recharge" to "Wallet").forEach { (key, label) ->
                    FilterChip(
                        selected = filter == key,
                        onClick = { filter = key },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary50,
                            selectedLabelColor = Primary700,
                        ),
                    )
                }
            }

            Text(
                "${state.transactions.size} transactions",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = Neutral500,
                fontSize = 13.sp,
            )

            if (state.transactions.isEmpty()) {
                ZapPayEmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No transactions yet",
                    subtitle = "Your fuel purchases and wallet recharges will appear here",
                )
            } else {
                val filtered = if (filter == "all") state.transactions
                else state.transactions.filter { it.transactionType == filter }

                LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                    items(filtered) { tx ->
                        TransactionItem(
                            title = tx.fuelType ?: tx.transactionType.replace("_", " ").replaceFirstChar { it.uppercase() },
                            subtitle = tx.createdAt.formatDate(),
                            amount = tx.amount,
                            isCredit = tx.transactionType == "wallet_recharge",
                            onClick = { onTransactionClick(tx.transactionId) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
