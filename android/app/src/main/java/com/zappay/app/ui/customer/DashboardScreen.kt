package com.zappay.app.ui.customer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import java.util.Calendar

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

    if (state.isLoading && state.balance == 0.0) {
        ShimmerLoadingScreen()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Gradient Header with Greeting ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Primary900, Primary700, Primary600),
                            start = Offset(0f, 0f),
                            end = Offset(1200f, 800f),
                        )
                    )
                    .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
            ) {
                Column {
                    // Top row: Greeting + notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                getGreeting(),
                                fontSize = 14.sp,
                                color = Primary200,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                state.profile?.fullName ?: "User",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = onNavigateToNotifications) {
                                Icon(Icons.Outlined.Notifications, contentDescription = "Alerts", tint = Primary200)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Wallet Balance Card (embedded in header) ──
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.1f)),
                        elevation = CardDefaults.cardElevation(0.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Wallet,
                                        contentDescription = null,
                                        tint = Accent400,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Balance", color = Primary200, fontSize = 13.sp)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "₹ ${"%.2f".format(state.balance)}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White,
                                    letterSpacing = (-1).sp,
                                )
                            }
                            Button(
                                onClick = onNavigateToWallet,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Accent500,
                                    contentColor = Primary900,
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Stats Row ──
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        HeaderStat("This Month", "₹ ${"%.0f".format(state.monthlySpending)}")
                        Box(Modifier.width(1.dp).height(36.dp).background(White.copy(alpha = 0.2f)))
                        HeaderStat("Total Spent", "₹ ${"%.0f".format(state.totalSpent)}")
                        Box(Modifier.width(1.dp).height(36.dp).background(White.copy(alpha = 0.2f)))
                        HeaderStat("Transactions", "${state.transactions.size}")
                    }
                }
            }

            // ── Content Area ──
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(20.dp))

                // ── Banner Carousel ──
                PromoBannerCarousel()

                Spacer(Modifier.height(24.dp))

                // ── Quick Actions ──
                SectionHeader(title = "Quick Actions")
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Outlined.Wallet,
                        label = "Wallet",
                        color = Primary500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWallet,
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.QrCodeScanner,
                        label = "My QR",
                        color = Teal500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToQR,
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.DirectionsCar,
                        label = "Vehicles",
                        color = Accent600,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVehicles,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Outlined.LocalGasStation,
                        label = "Pumps",
                        color = Success500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToNearbyPumps,
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.HeadsetMic,
                        label = "Support",
                        color = Neutral600,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTickets,
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.Notifications,
                        label = "Alerts",
                        color = Warning500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToNotifications,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Recent Activity ──
                SectionHeader(title = "Recent Activity")
                Spacer(Modifier.height(8.dp))

                if (state.transactions.isEmpty()) {
                    ZapPayEmptyState(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "No transactions yet",
                        subtitle = "Your fuel purchase history will appear here",
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

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Promotional Banner Carousel ──
@Composable
private fun PromoBannerCarousel() {
    data class BannerData(
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val gradient: Brush,
    )

    val banners = listOf(
        BannerData(
            "₹50 Cashback",
            "On your first fuel purchase above ₹500",
            Icons.Outlined.LocalOffer,
            Brush.linearGradient(
                colors = listOf(Primary700, Teal700),
                start = Offset.Zero,
                end = Offset(800f, 400f),
            ),
        ),
        BannerData(
            "Invite & Earn",
            "Get ₹100 for every friend who joins ZapPay",
            Icons.Outlined.PersonAdd,
            Brush.linearGradient(
                colors = listOf(Accent700, Accent500),
                start = Offset.Zero,
                end = Offset(800f, 400f),
            ),
        ),
        BannerData(
            "Fuel Price Alert",
            "Set alerts to know when prices drop near you",
            Icons.Outlined.TrendingDown,
            Brush.linearGradient(
                colors = listOf(Success700, Teal500),
                start = Offset.Zero,
                end = Offset(800f, 400f),
            ),
        ),
    )

    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(end = 0.dp),
            pageSpacing = 12.dp,
        ) { page ->
            val banner = banners[page]
            ZapPayBanner(
                title = banner.title,
                subtitle = banner.subtitle,
                icon = banner.icon,
                gradient = banner.gradient,
            )
        }
        Spacer(Modifier.height(10.dp))
        // Page indicators
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(banners.size) { index ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(
                            width = if (index == pagerState.currentPage) 20.dp else 6.dp,
                            height = 6.dp,
                        )
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (index == pagerState.currentPage) Primary500 else Neutral200,
                        ),
                )
            }
        }
    }
}

// ── Quick Action Card ──
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
                    .background(color = color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── Header Stat ──
@Composable
private fun HeaderStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
        Text(label, fontSize = 11.sp, color = White.copy(alpha = 0.6f))
    }
}

// ── Shimmer Loading ──
@Composable
fun ShimmerLoadingScreen() {
    val translateAnim = shimmerTranslateAnim()
    val brush = shimmerBrush(translateAnim)

    Box(
        Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)).background(brush)
            )
            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(16.dp)).background(brush))
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
                repeat(3) {
                    Box(Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(16.dp)).background(brush))
                }
            }
        }
    }
}

// ── Greeting Helper ──
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning ☀️"
        hour < 17 -> "Good Afternoon 👋"
        else -> "Good Evening 🌙"
    }
}
