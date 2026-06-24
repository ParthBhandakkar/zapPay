package com.zappay.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.NotificationDto
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import com.zappay.app.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadNotifications() }

    Scaffold(
        topBar = { ZapPayTopBar(title = "Notifications", onBack = onBack) },
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
        } else if (state.notifications.isEmpty()) {
            ZapPayEmptyState(
                icon = Icons.Outlined.NotificationsNone,
                title = "No notifications yet",
                subtitle = "We'll let you know when there's an update, offer, or payment receipt.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(state.notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = { viewModel.markAsRead(notification.id) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationDto,
    onClick: () -> Unit,
) {
    val isRead = notification.isRead
    val bgColor = if (!isRead) Primary50 else MaterialTheme.colorScheme.surface
    
    // Determine icon based on content
    val titleLower = notification.title.lowercase()
    val icon = when {
        titleLower.contains("payment") || titleLower.contains("paid") -> Icons.Outlined.Receipt
        titleLower.contains("wallet") || titleLower.contains("recharge") -> Icons.Outlined.AccountBalanceWallet
        titleLower.contains("offer") || titleLower.contains("cashback") -> Icons.Outlined.LocalOffer
        else -> Icons.Outlined.Notifications
    }
    
    val iconTint = when {
        isRead -> Neutral500
        titleLower.contains("payment") -> Success500
        titleLower.contains("offer") -> Accent600
        else -> Primary500
    }
    
    val iconBg = if (isRead) Neutral100 else iconTint.copy(alpha = 0.15f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    notification.title, 
                    fontWeight = if (isRead) FontWeight.Medium else FontWeight.SemiBold, 
                    fontSize = 15.sp, 
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (!isRead) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary500))
                }
            }
            if (!notification.body.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(notification.body, fontSize = 13.sp, color = Neutral500, maxLines = 2)
            }
            notification.createdAt?.let {
                Spacer(Modifier.height(8.dp))
                Text(it.formatDate(), fontSize = 11.sp, color = Neutral400)
            }
        }
    }
}
