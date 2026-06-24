package com.zappay.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import com.zappay.app.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketsScreen(
    viewModel: SupportTicketsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadTickets() }

    LaunchedEffect(state.createSuccess) {
        if (state.createSuccess) {
            snackbarHostState.showSnackbar("Support ticket created successfully")
            viewModel.clearCreateSuccess()
        }
    }

    Scaffold(
        topBar = { ZapPayTopBar(title = "Support Tickets", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Primary500,
                contentColor = White,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create ticket")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.tickets.isEmpty() -> LoadingScreen()
                state.error != null && state.tickets.isEmpty() -> ErrorMessage(
                    message = state.error!!,
                    onRetry = { viewModel.loadTickets() },
                )
                state.tickets.isEmpty() -> ZapPayEmptyState(
                    icon = Icons.Outlined.HeadsetMic,
                    title = "How can we help?",
                    subtitle = "Create a support ticket if you face any issues with payments or the app.",
                    actionText = "Create Ticket",
                    onAction = { showCreateDialog = true }
                )
                else -> LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(state.tickets) { ticket ->
                        TicketCard(ticket = ticket)
                    }
                    item { Spacer(Modifier.height(80.dp)) } // Space for FAB
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTicketDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { subject, description, category, priority ->
                viewModel.createTicket(subject, description, category, priority)
                showCreateDialog = false
            },
            isLoading = state.isLoading,
        )
    }
}

@Composable
private fun TicketCard(ticket: com.zappay.app.data.remote.dto.SupportTicketDto) {
    val statusColor = when (ticket.status?.lowercase()) {
        "open" -> Warning500
        "resolved" -> Success500
        "closed" -> Neutral500
        else -> Neutral500
    }
    val priorityColor = when (ticket.priority?.lowercase()) {
        "low" -> Success500
        "medium" -> Warning500
        "high" -> Danger500
        "urgent" -> Danger700
        else -> Neutral500
    }

    ZapPayCard {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    ticket.subject,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (ticket.status != null) {
                    Spacer(Modifier.width(8.dp))
                    ZapPayBadge(text = ticket.status, color = statusColor)
                }
            }
            
            if (!ticket.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    ticket.description,
                    fontSize = 13.sp,
                    color = Neutral500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ticket.category != null) {
                        ZapPayBadge(text = ticket.category, color = Primary500)
                    }
                    if (ticket.priority != null) {
                        ZapPayBadge(text = ticket.priority, color = priorityColor)
                    }
                }
                if (ticket.createdAt != null) {
                    Text(ticket.createdAt.formatDate(), fontSize = 12.sp, color = Neutral400)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTicketDialog(
    onDismiss: () -> Unit,
    onCreate: (subject: String, description: String?, category: String, priority: String) -> Unit,
    isLoading: Boolean,
) {
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("technical") }
    var selectedPriority by remember { mutableStateOf("medium") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Support Ticket", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ZapPayInput(
                    value = subject,
                    onValueChange = { subject = it },
                    label = "Subject",
                    placeholder = "Brief description of the issue",
                    imeAction = ImeAction.Next,
                )
                
                ZapPayInput(
                    value = description,
                    onValueChange = { description = it },
                    label = "Details",
                    placeholder = "Please explain the issue in detail",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                )
                
                Column {
                    Text("Category", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Neutral700)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("technical" to "Tech", "billing" to "Billing", "other" to "Other").forEach { (cat, label) ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary50,
                                    selectedLabelColor = Primary700,
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(subject, description.ifBlank { null }, selectedCategory, selectedPriority) },
                enabled = subject.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Primary500)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = White, strokeWidth = 2.dp)
                } else {
                    Text("Submit Ticket")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Neutral600) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
