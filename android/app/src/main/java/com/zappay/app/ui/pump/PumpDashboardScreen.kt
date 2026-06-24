package com.zappay.app.ui.pump

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.zappay.app.ui.components.*
import com.zappay.app.ui.customer.ShimmerLoadingScreen
import com.zappay.app.ui.theme.*
import com.zappay.app.util.formatDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpDashboardScreen(
    viewModel: PumpViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSetupPump: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }
    LaunchedEffect(state.pumpId) {
        if (state.pumpId != null) {
            viewModel.loadSettings()
        }
    }

    if (!state.hasPump) {
        Scaffold { padding ->
            ZapPayEmptyState(
                icon = Icons.Outlined.LocalGasStation,
                title = "No Pump Registered",
                subtitle = "Register your pump to start accepting ZapPay payments from customers.",
                actionText = "Register Pump",
                onAction = onSetupPump,
                modifier = Modifier.padding(padding),
            )
        }
        return
    }

    if (state.isLoading && state.totalTransactions == 0) {
        ShimmerLoadingScreen()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Gradient Header (Teal/Blue theme for Pump) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Teal700, Teal500),
                        start = Offset(0f, 0f),
                        end = Offset(1200f, 800f),
                    )
                )
                .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            getGreeting(),
                            fontSize = 14.sp,
                            color = Teal100,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            state.settings?.pumpName ?: "Pump Dashboard",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = Teal100)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Stats overview card in header
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HeaderStat("Today Rev", "₹ ${"%.0f".format(state.revenueToday)}")
                        Box(Modifier.width(1.dp).height(36.dp).background(White.copy(alpha = 0.2f)))
                        HeaderStat("Today #", "${state.transactionsToday}")
                        Box(Modifier.width(1.dp).height(36.dp).background(White.copy(alpha = 0.2f)))
                        HeaderStat("Total Rev", "₹ ${"%.0f".format(state.totalRevenue)}")
                    }
                }
            }
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(20.dp))

            // ── Pump Status Banner ──
            state.settings?.let { settings ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToSettings() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (settings.isOpen) Success50 else Warning50
                    ),
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (settings.isOpen) Success500.copy(alpha = 0.15f) else Warning500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (settings.isOpen) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (settings.isOpen) Success500 else Warning500,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (settings.isOpen) "Pump is Open" else "Pump is Closed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (settings.isOpen) Success600 else Warning600,
                            )
                            Text(
                                formatFuelRateSummary(settings.fuelTypes, settings.fuelRates),
                                fontSize = 12.sp,
                                color = Neutral500,
                            )
                        }
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Neutral400)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Quick Actions ──
            SectionHeader(title = "Quick Actions")
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PumpActionCard(
                    icon = Icons.Outlined.QrCodeScanner,
                    label = "Scan QR",
                    color = Primary500,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToScanner,
                )
                PumpActionCard(
                    icon = Icons.Outlined.BarChart,
                    label = "Analytics",
                    color = Success500,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAnalytics,
                )
                PumpActionCard(
                    icon = Icons.Outlined.Tune,
                    label = "Settings",
                    color = Accent600,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSettings,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Recent Transactions ──
            SectionHeader(title = "Recent Activity", action = "View All", onAction = onNavigateToAnalytics)
            Spacer(Modifier.height(8.dp))

            if (state.recentTransactions.isEmpty()) {
                ZapPayEmptyState(
                    icon = Icons.Outlined.Receipt,
                    title = "No transactions today",
                    subtitle = "Scan customer QR codes to process fuel payments.",
                )
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        state.recentTransactions.take(10).forEach { tx ->
                            TransactionItem(
                                title = tx.fuelType ?: tx.transactionType.replace("_", " ").replaceFirstChar { it.uppercase() },
                                subtitle = tx.createdAt.formatDate(),
                                amount = tx.amount,
                                isCredit = true, // To pump, all incoming payments are credits
                            )
                            if (tx != state.recentTransactions.take(10).last()) {
                                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
        Text(label, fontSize = 11.sp, color = White.copy(alpha = 0.7f))
    }
}

@Composable
private fun PumpActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
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

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning ☀️"
        hour < 17 -> "Good Afternoon 👋"
        else -> "Good Evening 🌙"
    }
}
