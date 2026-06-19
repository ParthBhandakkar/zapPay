package com.zappay.app.ui.pump

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import com.zappay.app.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpDashboardScreen(
    viewModel: PumpViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSetupPump: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }
    LaunchedEffect(state.pumpId) {
        if (state.pumpId != null) {
            viewModel.loadSettings()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pump Dashboard", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
                actions = {
                    TextButton(onClick = onNavigateToProfile) {
                        Text("Profile", color = Purple500)
                    }
                },
            )
        },
    ) { padding ->
        if (!state.hasPump) {
            NoPumpScreen(onSetupPump = onSetupPump, modifier = Modifier.padding(padding))
        } else if (state.isLoading && state.totalTransactions == 0) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // Stats row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("Today (₹)", "₹ ${"%.0f".format(state.revenueToday)}")
                    StatItem("Today (#)", "${state.transactionsToday}")
                    StatItem("Total Rev.", "₹ ${"%.0f".format(state.totalRevenue)}")
                    StatItem("Total Txns", "${state.totalTransactions}")
                }

                Spacer(Modifier.height(20.dp))

                state.settings?.let { settings ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (settings.isOpen) Green100 else Yellow100),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                if (settings.isOpen) "Pump open" else "Pump closed",
                                fontWeight = FontWeight.SemiBold,
                                color = if (settings.isOpen) Green500 else Yellow500,
                                fontSize = 14.sp,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(formatFuelRateSummary(settings.fuelTypes, settings.fuelRates), color = Gray700, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // Action buttons
                Text("Actions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ZapPayCard(modifier = Modifier.weight(1f), onClick = onNavigateToScanner) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("S", fontSize = 24.sp, color = Purple500)
                            Spacer(Modifier.height(4.dp))
                            Text("Scanner", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                    ZapPayCard(modifier = Modifier.weight(1f), onClick = onNavigateToSettings) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("G", fontSize = 24.sp, color = Purple500)
                            Spacer(Modifier.height(4.dp))
                            Text("Settings", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Recent transactions
                Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                Spacer(Modifier.height(8.dp))
                if (state.recentTransactions.isEmpty()) {
                    Text("No recent transactions", color = Gray500, modifier = Modifier.padding(vertical = 24.dp))
                } else {
                    state.recentTransactions.take(10).forEach { tx ->
                        TransactionItem(
                            title = tx.fuelType ?: tx.transactionType.replace("_", " "),
                            subtitle = tx.createdAt.formatDate(),
                            amount = tx.amount,
                            isCredit = false,
                        )
                        HorizontalDivider(color = Gray100)
                    }
                }
            }
        }
    }
}

private fun formatFuelRateSummary(fuelTypes: String, fuelRates: String): String {
    val types = fuelTypes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val rates = fuelRates.split(",").map { it.trim() }
    return types.mapIndexed { index, type ->
        "$type ₹${rates.getOrNull(index) ?: "0"}/L"
    }.joinToString("  ")
}

@Composable
private fun NoPumpScreen(onSetupPump: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No Pump Registered", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
        Spacer(Modifier.height(8.dp))
        Text("Register your pump to start accepting payments", color = Gray500)
        Spacer(Modifier.height(24.dp))
        ZapPayButton(text = "Register Pump", onClick = onSetupPump)
    }
}
