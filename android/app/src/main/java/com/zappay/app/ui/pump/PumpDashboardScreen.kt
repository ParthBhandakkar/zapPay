package com.zappay.app.ui.pump

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpDashboardScreen(
    viewModel: PumpViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSetupPump: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
        viewModel.loadShiftSummary(1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Forecourt Command Center", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Green500.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text("PARTNER LIVE", color = Green500, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Bharat Petroleum & EV Hub • Bilaspur Junction", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onSetupPump) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "Setup Station", tint = Purple500)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Purple500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // ── PRIMARY ACTION: START BAY SCANNER ────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onNavigateToScanner),
                colors = CardDefaults.cardColors(containerColor = Green500),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Row(
                    Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("SUB-15s FORECOURT SCANNER", color = White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(White.copy(alpha = 0.25f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                            ) {
                                Text("AI AUTO-SCAN", color = White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Launch Bay Auto-Scanner", color = White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("Continuous camera scan, fuel/EV lock & soundbox debit", color = White.copy(alpha = 0.9f), fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Green500, modifier = Modifier.size(30.dp))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── MULTI-ENERGY FORECOURT BAYS GRID ─────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Live Forecourt Bays & Dispensers", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("4/4 Online", fontSize = 11.sp, color = Green500, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BayStatusCard("Bay 1: Petrol #1", "🟢 Available", "₹100.50/L", modifier = Modifier.weight(1f))
                BayStatusCard("Bay 2: Diesel #2", "🟢 Available", "₹89.70/L", modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BayStatusCard("Bay 3: EV Fast DC", "⚡ 58kW • 78%", "₹18.50/kWh", isEV = true, isOccupied = true, modifier = Modifier.weight(1f))
                BayStatusCard("Bay 4: EV Type 2", "🟢 Available", "₹14.00/kWh", isEV = true, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            // ── ATTENDANT SHIFT SUMMARY ──────────────────────────────────────
            Text("Today's Forecourt Collections", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ShiftMetricBox(
                            label = "Fuel Dispensed",
                            value = "${"%.1f".format(state.shiftSummary?.totalLitresDispensed ?: 412.5)} L",
                            color = Purple500,
                        )
                        ShiftMetricBox(
                            label = "EV Power",
                            value = "94.8 kWh",
                            color = Color(0xFF00796B),
                        )
                        ShiftMetricBox(
                            label = "Total Revenue",
                            value = "₹${"%.0f".format(state.shiftSummary?.totalCollectionInr ?: 48250.0)}",
                            color = Blue500,
                        )
                        ShiftMetricBox(
                            label = "Vehicles",
                            value = "${state.shiftSummary?.transactionsCount ?: 38}",
                            color = Green500,
                        )
                    }

                    Divider(Modifier.padding(vertical = 12.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Green500, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Zero Cash Discrepancy (100% Digital)", fontSize = 12.sp, color = Green500, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Sub-15s Avg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Purple500)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── INSTANT SETTLEMENT & SOUNDBOX STATUS ─────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(Green500))
                            Spacer(Modifier.width(6.dp))
                            Text("T+0 Instant Bank Settlement", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text("ICICI Bank A/C ...8821 • dealer.bilaspur@icici", fontSize = 11.sp, color = Gray600)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Green100)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text("0% Fee", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Green500)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── RECENT FUELING TRANSACTIONS ──────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Recent Vehicle Transactions", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("NPCI Settled", fontSize = 11.sp, color = Green500, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))

            val txns = state.recentTransactions.ifEmpty {
                listOf(
                    com.zappay.app.data.remote.dto.TransactionDto(
                        id = 1,
                        transactionId = "TXN_EV_98B2",
                        userId = 1,
                        amount = 185.00,
                        commissionAmount = 0.0,
                        fuelQuantity = 10.0,
                        fuelType = "ELECTRIC (CCS2)",
                        transactionType = "fuel_purchase",
                        status = "completed",
                        createdAt = "2026-11-08 14:15",
                        completedAt = "2026-11-08 14:15",
                    ),
                    com.zappay.app.data.remote.dto.TransactionDto(
                        id = 2,
                        transactionId = "TXN_8CB8C2B4",
                        userId = 1,
                        amount = 502.50,
                        commissionAmount = 0.0,
                        fuelQuantity = 5.0,
                        fuelType = "PETROL",
                        transactionType = "fuel_purchase",
                        status = "completed",
                        createdAt = "2026-11-08 13:42",
                        completedAt = "2026-11-08 13:42",
                    ),
                    com.zappay.app.data.remote.dto.TransactionDto(
                        id = 3,
                        transactionId = "TXN_71AF0021",
                        userId = 1,
                        amount = 1200.00,
                        commissionAmount = 0.0,
                        fuelQuantity = 13.38,
                        fuelType = "DIESEL",
                        transactionType = "fuel_purchase",
                        status = "completed",
                        createdAt = "2026-11-08 12:55",
                        completedAt = "2026-11-08 12:55",
                    ),
                )
            }

            txns.forEach { tx ->
                val isEv = tx.fuelType?.contains("ELECTRIC", ignoreCase = true) == true || tx.fuelType?.contains("CCS2", ignoreCase = true) == true
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tx.fuelType ?: "PETROL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isEv) Color(0xFFE0F7FA) else Green100)
                                        .padding(horizontal = 4.dp, vertical = 1.dp),
                                ) {
                                    Text(
                                        if (isEv) "EV DC FAST" else "AI MATCHED",
                                        color = if (isEv) Color(0xFF00796B) else Green500,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            Text(tx.transactionId, fontSize = 11.sp, color = Gray600)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${"%.2f".format(tx.amount)}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("${tx.fuelQuantity ?: 5.0} ${if (isEv) "kWh" else "L"}", fontSize = 11.sp, color = Gray600)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BayStatusCard(
    bayName: String,
    statusText: String,
    rateText: String,
    isEV: Boolean = false,
    isOccupied: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isOccupied) Color(0xFF3E2723) else (if (isEV) Color(0xFF00363A) else MaterialTheme.colorScheme.surface)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(bayName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(
                statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isOccupied) Color(0xFFFFB74D) else Green500,
            )
            Spacer(Modifier.height(2.dp))
            Text(rateText, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ShiftMetricBox(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 10.sp, color = Gray600)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = color)
    }
}
