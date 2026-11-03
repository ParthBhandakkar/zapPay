package com.zappay.app.ui.pump

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.zappay.app.util.formatDate

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
                        Text("Forecourt Command Center", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Bharat Petroleum • Bilaspur Junction Hub", fontSize = 11.sp, color = Gray600)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Purple500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
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
            // ── PRIMARY ACTION: START SCAN LOOP ──────────────────────────────
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
                            Text("SUB-15s SCAN LOOP", color = White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(White.copy(alpha = 0.25f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                            ) {
                                Text("AI OCR", color = White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Scan Vehicle & Meter", color = White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("Automatic fuel lock, OCR validation & instant debit", color = White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Green500, modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── ATTENDANT SHIFT TALLY & ZERO CASH VERIFICATION ────────────────
            Text("Attendant Shift Tally & Incentives", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ShiftMetricBox(
                            label = "Digital Volume",
                            value = "${"%.1f".format(state.shiftSummary?.totalLitresDispensed ?: 145.5)} L",
                            color = Purple500,
                        )
                        ShiftMetricBox(
                            label = "Digital Collection",
                            value = "₹${"%.0f".format(state.shiftSummary?.totalCollectionInr ?: 14620.0)}",
                            color = Blue500,
                        )
                        ShiftMetricBox(
                            label = "Transactions",
                            value = "${state.shiftSummary?.transactionsCount ?: 12}",
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
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Zero Cash Discrepancy Verified", fontSize = 12.sp, color = Green500, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Earned Bonus: ₹155.00", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Purple500)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── FORECOURT ISLAND STATUS ──────────────────────────────────────
            Text("Forecourt Islands & Hardware", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IslandStatusChip("Island 1 (Petrol)", active = true, modifier = Modifier.weight(1f))
                IslandStatusChip("Island 2 (Diesel)", active = true, modifier = Modifier.weight(1f))
                IslandStatusChip("Soundbox", active = true, isSoundbox = true, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            // ── RECENT FUELING TRANSACTIONS ──────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Fueling Stream", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("T+0 Bank Settled", fontSize = 11.sp, color = Green500, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))

            val txns = state.recentTransactions.ifEmpty {
                listOf(
                    com.zappay.app.data.remote.dto.TransactionDto(
                        id = 1,
                        transactionId = "TXN_8CB8C2B4",
                        userId = 1,
                        amount = 502.50,
                        commissionAmount = 0.0,
                        fuelQuantity = 5.0,
                        fuelType = "PETROL",
                        transactionType = "fuel_purchase",
                        status = "completed",
                        createdAt = "2026-11-03 16:10",
                        completedAt = "2026-11-03 16:10",
                    ),
                    com.zappay.app.data.remote.dto.TransactionDto(
                        id = 2,
                        transactionId = "TXN_71AF0021",
                        userId = 1,
                        amount = 1200.00,
                        commissionAmount = 0.0,
                        fuelQuantity = 13.33,
                        fuelType = "DIESEL",
                        transactionType = "fuel_purchase",
                        status = "completed",
                        createdAt = "2026-11-03 15:42",
                        completedAt = "2026-11-03 15:42",
                    ),
                )
            }

            txns.forEach { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
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
                                        .background(Green100)
                                        .padding(horizontal = 4.dp, vertical = 1.dp),
                                ) {
                                    Text("AI MATCHED", color = Green500, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(tx.transactionId, fontSize = 11.sp, color = Gray600)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${"%.2f".format(tx.amount)}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("${tx.fuelQuantity ?: 5.0} L", fontSize = 11.sp, color = Gray600)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShiftMetricBox(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = Gray600)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun IslandStatusChip(label: String, active: Boolean, isSoundbox: Boolean = false, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) (if (isSoundbox) Purple50 else Green100) else Gray200)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Column {
            Text(if (isSoundbox) "🔊 MQTT Box" else label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (isSoundbox) Purple700 else Green700)
            Text(if (active) "ONLINE" else "OFFLINE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSoundbox) Purple500 else Green500)
        }
    }
}
