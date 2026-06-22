package com.zappay.app.ui.customer

import androidx.compose.animation.*
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
import com.zappay.app.ui.theme.*
import com.zappay.app.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    viewModel: CustomerViewModel,
    onNavigateToWallet: () -> Unit,
    onNavigateToQR: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToTickets: () -> Unit = {},
    onNavigateToNearbyPumps: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Alerts", tint = Gray700)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = Gray700)
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading && state.balance == 0.0) {
            ShimmerLoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                BalanceCard(
                    balance = state.balance,
                    onAddMoney = onNavigateToWallet,
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatItem("Monthly Spend", "₹ ${"%.0f".format(state.monthlySpending)}")
                    StatItem("Total Spent", "₹ ${"%.0f".format(state.totalSpent)}")
                    StatItem("Transactions", "${state.transactions.size}")
                }

                Spacer(Modifier.height(24.dp))

                Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Outlined.Wallet,
                        label = "Wallet",
                        color = Purple500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWallet,
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.QrCodeScanner,
                        label = "My QR",
                        color = Blue500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToQR,
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Outlined.DirectionsCar,
                        label = "Vehicles",
                        color = Green500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVehicles,
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.Notifications,
                        label = "Alerts",
                        color = Yellow500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToNotifications,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Outlined.HeadsetMic,
                        label = "Support",
                        color = Gray700,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTickets,
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.LocalGasStation,
                        label = "Pumps",
                        color = Purple500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToNearbyPumps,
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                Spacer(Modifier.height(8.dp))

                if (state.transactions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.Outlined.Receipt, contentDescription = null, tint = Gray300, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No recent transactions", color = Gray500, fontSize = 14.sp)
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
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
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Gray100)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color = color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Gray900)
        }
    }
}

@Composable
fun ShimmerLoadingScreen() {
    val shimmerColors = listOf(
        Gray100,
        Gray100.copy(alpha = 0.3f),
        Gray100,
    )
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value),
    )

    Box(
        Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)).background(brush)
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                repeat(3) {
                    Box(Modifier.size(80.dp, 60.dp).clip(RoundedCornerShape(12.dp)).background(brush))
                }
            }
            Spacer(Modifier.height(24.dp))
            Box(Modifier.width(140.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) {
                    Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(16.dp)).background(brush))
                }
            }
        }
    }
}
