package com.zappay.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
        topBar = {
            TopAppBar(
                title = { Text("Support Tickets", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Purple500,
                contentColor = White,
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
                state.tickets.isEmpty() -> ErrorMessage("No support tickets")
                else -> LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                    items(state.tickets) { ticket ->
                        TicketCard(ticket = ticket)
                        HorizontalDivider(color = Gray100)
                    }
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
        "open" -> Yellow500
        "resolved" -> Green500
        "closed" -> Gray500
        else -> Gray500
    }
    val priorityColor = when (ticket.priority?.lowercase()) {
        "low" -> Green500
        "medium" -> Yellow500
        "high" -> Red500
        "urgent" -> Red500
        else -> Gray500
    }

    ZapPayCard(modifier = Modifier.padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                ticket.subject,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!ticket.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    ticket.description,
                    fontSize = 13.sp,
                    color = Gray500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (ticket.category != null) {
                    Badge(text = ticket.category, color = Purple500)
                }
                if (ticket.priority != null) {
                    Badge(text = ticket.priority, color = priorityColor)
                }
                if (ticket.status != null) {
                    Badge(text = ticket.status, color = statusColor)
                }
            }
            if (ticket.createdAt != null) {
                Spacer(Modifier.height(6.dp))
                Text(ticket.createdAt.formatDate(), fontSize = 12.sp, color = Gray500)
            }
        }
    }
}

@Composable
private fun Badge(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
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
    var categoryExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Ticket", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZapPayInput(
                    value = subject,
                    onValueChange = { subject = it },
                    label = "Subject",
                    placeholder = "Enter subject",
                    imeAction = ImeAction.Next,
                )
                ZapPayInput(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    placeholder = "Describe your issue",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                )
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedCategory.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        listOf("technical", "billing", "other").forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.replaceFirstChar { it.uppercase() }) },
                                onClick = { selectedCategory = cat; categoryExpanded = false },
                            )
                        }
                    }
                }
                // Priority dropdown
                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedPriority.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false },
                    ) {
                        listOf("low", "medium", "high", "urgent").forEach { pri ->
                            DropdownMenuItem(
                                text = { Text(pri.replaceFirstChar { it.uppercase() }) },
                                onClick = { selectedPriority = pri; priorityExpanded = false },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(subject, description.ifBlank { null }, selectedCategory, selectedPriority) },
                enabled = subject.isNotBlank() && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit", color = Purple500)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Gray500) }
        },
    )
}
