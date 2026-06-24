package com.zappay.app.ui.pump

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.TransactionDto
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import com.zappay.app.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpAnalyticsScreen(
    viewModel: PumpViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val transactions = state.recentTransactions

    val fuelBreakdown = remember(transactions) {
        transactions
            .filter { it.fuelType != null }
            .groupBy { it.fuelType!! }
            .map { (fuel, list) ->
                FuelBreakdown(
                    fuelType = fuel,
                    count = list.size,
                    revenue = list.sumOf { it.amount },
                    avgAmount = if (list.isNotEmpty()) list.sumOf { it.amount } / list.size else 0.0,
                )
            }
            .sortedByDescending { it.revenue }
    }

    val totalCommission = remember(transactions) { transactions.sumOf { it.commissionAmount } }
    val avgTransaction = if (state.totalTransactions > 0) state.totalRevenue / state.totalTransactions else 0.0

    Scaffold(
        topBar = {
            ZapPayTopBar(
                title = "Analytics",
                onBack = {}, // Removed back button since it's typically a bottom tab
                containerColor = Color.Transparent,
                contentColor = White,
                showBackButton = false
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            // Gradient Header matching PumpDashboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Teal700, Teal500),
                            start = Offset(0f, 0f),
                            end = Offset(1200f, 800f),
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header content
                Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.BarChart, contentDescription = null, tint = White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Performance Overview", fontSize = 13.sp, color = Teal100)
                            Text("Analytics", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    
                    // Stats overview card in header
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.1f)),
                        elevation = CardDefaults.cardElevation(0.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatPill("Total Rev", "₹ ${"%.0f".format(state.totalRevenue)}")
                            Box(Modifier.width(1.dp).height(36.dp).background(White.copy(alpha = 0.2f)))
                            StatPill("Commission", "₹ ${"%.0f".format(totalCommission)}")
                            Box(Modifier.width(1.dp).height(36.dp).background(White.copy(alpha = 0.2f)))
                            StatPill("Avg/Txn", "₹ ${"%.0f".format(avgTransaction)}")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    // Today vs Total comparison
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Today vs All Time", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(20.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                CompareStat("Revenue Today", "₹ ${"%.0f".format(state.revenueToday)}", Teal500)
                                Box(Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                CompareStat("Revenue Total", "₹ ${"%.0f".format(state.totalRevenue)}", Neutral700)
                            }
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                CompareStat("Today's Txns", "${state.transactionsToday}", Teal500)
                                Box(Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                CompareStat("Total Txns", "${state.totalTransactions}", Neutral700)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Fuel type breakdown
                    if (fuelBreakdown.isNotEmpty()) {
                        SectionHeader("Fuel Type Breakdown")
                        Spacer(Modifier.height(12.dp))
                        fuelBreakdown.forEach { breakdown ->
                            FuelBreakdownCard(breakdown)
                            Spacer(Modifier.height(12.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // All recent transactions
                    SectionHeader("All Transactions")
                    Spacer(Modifier.height(12.dp))

                    if (transactions.isEmpty()) {
                        ZapPayEmptyState(
                            icon = Icons.Outlined.Receipt,
                            title = "No transactions yet",
                            subtitle = "Completed payments will appear here."
                        )
                    } else {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(Modifier.padding(vertical = 4.dp)) {
                                transactions.forEach { tx ->
                                    TransactionItem(
                                        title = tx.fuelType ?: tx.transactionType.replace("_", " ").replaceFirstChar { it.uppercase() },
                                        subtitle = "${tx.userName ?: "Customer"} • ${tx.createdAt.formatDate()}",
                                        amount = tx.amount,
                                        isCredit = true,
                                    )
                                    if (tx != transactions.last()) {
                                        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
        Text(label, fontSize = 11.sp, color = White.copy(alpha = 0.7f))
    }
}

@Composable
private fun CompareStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(120.dp)) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = Neutral500)
    }
}

@Composable
private fun FuelBreakdownCard(breakdown: FuelBreakdown) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Accent50),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.LocalGasStation, contentDescription = null, tint = Accent500, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(breakdown.fuelType, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${breakdown.count} transactions", fontSize = 12.sp, color = Neutral500)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹ ${"%.0f".format(breakdown.revenue)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("avg ₹ ${"%.0f".format(breakdown.avgAmount)}", fontSize = 12.sp, color = Neutral500)
            }
        }
    }
}

private data class FuelBreakdown(
    val fuelType: String,
    val count: Int,
    val revenue: Double,
    val avgAmount: Double,
)
