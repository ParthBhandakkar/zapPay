package com.zappay.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
        } else if (state.notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No notifications yet", color = Gray500, fontSize = 14.sp)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(state.notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = { viewModel.markAsRead(notification.id) },
                    )
                    HorizontalDivider(color = Gray100)
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
    val bgColor = if (!notification.isRead) Purple50 else White
    val icon = if (notification.isRead) Icons.Default.CheckCircle else Icons.Default.Email
    val iconBg = if (notification.isRead) Gray100 else Purple200
    val iconTint = if (notification.isRead) Gray500 else Purple500

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
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(notification.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Gray900)
            if (!notification.body.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(notification.body, fontSize = 13.sp, color = Gray500, maxLines = 2)
            }
            notification.createdAt?.let {
                Spacer(Modifier.height(4.dp))
                Text(it.formatDate(), fontSize = 11.sp, color = Gray300)
            }
        }
    }
}
