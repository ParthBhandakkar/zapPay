package com.zappay.app.ui.pump

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.theme.*

private data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    BottomNavItem("Dashboard", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Scanner", Icons.Outlined.QrCodeScanner, Icons.Outlined.QrCodeScanner),
    BottomNavItem("Analytics", Icons.Outlined.BarChart, Icons.Outlined.BarChart),
    BottomNavItem("Settings", Icons.Outlined.Tune, Icons.Outlined.Tune),
    BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun PumpMainScreen(
    pumpViewModel: PumpViewModel,
    onNavigateToSetupPump: () -> Unit,
    onLogout: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary500,
                            selectedTextColor = Primary500,
                            unselectedIconColor = Neutral400,
                            unselectedTextColor = Neutral400,
                            indicatorColor = Primary50,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> PumpDashboardScreen(
                    viewModel = pumpViewModel,
                    onNavigateToScanner = { selectedTab = 1 },
                    onNavigateToSettings = { selectedTab = 3 },
                    onSetupPump = onNavigateToSetupPump,
                    onNavigateToProfile = { selectedTab = 4 },
                    onNavigateToAnalytics = { selectedTab = 2 },
                )
                1 -> ScannerScreen(
                    viewModel = pumpViewModel,
                    onBack = { selectedTab = 0 },
                )
                2 -> PumpAnalyticsScreen(
                    viewModel = pumpViewModel,
                )
                3 -> PumpSettingsScreen(
                    viewModel = pumpViewModel,
                    onBack = { selectedTab = 0 },
                )
                4 -> PumpProfileScreen(
                    viewModel = pumpViewModel,
                    onBack = { selectedTab = 0 },
                    onLogout = onLogout,
                )
            }
        }
    }
}
