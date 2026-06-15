package com.zappay.app.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun CustomerDashboardScreen(
    viewModel: CustomerViewModel,
    onNavigateToWallet: () -> Unit,
    onNavigateToQR: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
    ) { padding ->
        if (state.isLoading && state.balance == 0.0) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // Balance Card
                BalanceCard(
                    balance = state.balance,
                    onAddMoney = onNavigateToWallet,
                )

                Spacer(Modifier.height(20.dp))

                // Stats
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatItem("Monthly Spend", "₹ ${"%.0f".format(state.monthlySpending)}")
                    StatItem("Total Spent", "₹ ${"%.0f".format(state.totalSpent)}")
                    StatItem("Transactions", "${state.transactions.size}")
                }

                Spacer(Modifier.height(20.dp))

                // Quick Actions
                Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ZapPayCard(modifier = Modifier.weight(1f), onClick = onNavigateToWallet) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("W", fontSize = 24.sp, color = Purple500)
                            Spacer(Modifier.height(4.dp))
                            Text("Wallet", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                    ZapPayCard(modifier = Modifier.weight(1f), onClick = onNavigateToQR) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("Q", fontSize = 24.sp, color = Purple500)
                            Spacer(Modifier.height(4.dp))
                            Text("My QR", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Recent Transactions
                Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                Spacer(Modifier.height(8.dp))

                if (state.transactions.isEmpty()) {
                    Text("No recent transactions", color = Gray500, modifier = Modifier.padding(vertical = 24.dp))
                } else {
                    state.transactions.take(5).forEach { tx ->
                        TransactionItem(
                            title = tx.fuelType ?: tx.transactionType.replace("_", " "),
                            subtitle = tx.createdAt.formatDate(),
                            amount = tx.amount,
                            isCredit = tx.transactionType == "wallet_recharge",
                        )
                        if (tx != state.transactions.take(5).last()) {
                            HorizontalDivider(color = Gray100)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
