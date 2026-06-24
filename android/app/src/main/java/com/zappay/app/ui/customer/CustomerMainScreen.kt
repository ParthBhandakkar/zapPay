package com.zappay.app.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.theme.*

private data class CustomerNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    CustomerNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
    CustomerNavItem("Pay", Icons.Filled.QrCode, Icons.Outlined.QrCode),
    CustomerNavItem("Pumps", Icons.Filled.LocalGasStation, Icons.Outlined.LocalGasStation),
    CustomerNavItem("History", Icons.Filled.Receipt, Icons.Outlined.ReceiptLong),
    CustomerNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun CustomerMainScreen(
    selectedTab: Int = 0,
    onTabChanged: (Int) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { onTabChanged(index) },
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
        content(padding)
    }
}
