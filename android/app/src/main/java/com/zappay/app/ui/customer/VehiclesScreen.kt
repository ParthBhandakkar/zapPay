package com.zappay.app.ui.customer

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.VehicleDto
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    viewModel: VehiclesViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<VehicleDto?>(null) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var vehicleToRemove by remember { mutableStateOf<VehicleDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadVehicles() }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Vehicles", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Purple500,
                contentColor = White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
        } else if (state.vehicles.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No vehicles added yet", color = Gray500, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.vehicles) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onEdit = { editingVehicle = vehicle },
                        onRemove = {
                            vehicleToRemove = vehicle
                            showRemoveDialog = true
                        },
                    )
                }
            }
        }
    }

    if (showAddDialog || editingVehicle != null) {
        val vehicle = editingVehicle
        VehicleFormDialog(
            vehicle = vehicle,
            onDismiss = {
                showAddDialog = false
                editingVehicle = null
            },
            onSave = { number, type, nickname, primary ->
                if (vehicle != null) {
                    viewModel.updateVehicle(vehicle.id, number, type, nickname, primary)
                } else {
                    viewModel.addVehicle(number, type, nickname, primary)
                }
                showAddDialog = false
                editingVehicle = null
            },
        )
    }

    if (showRemoveDialog && vehicleToRemove != null) {
        AlertDialog(
            onDismissRequest = {
                showRemoveDialog = false
                vehicleToRemove = null
            },
            title = { Text("Remove Vehicle") },
            text = { Text("Are you sure you want to remove ${vehicleToRemove!!.vehicleNumber}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeVehicle(vehicleToRemove!!.id)
                    showRemoveDialog = false
                    vehicleToRemove = null
                }) { Text("Remove", color = Red500) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRemoveDialog = false
                    vehicleToRemove = null
                }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun VehicleCard(
    vehicle: VehicleDto,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(vehicle.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gray900)
                Spacer(Modifier.height(4.dp))
                vehicle.vehicleType?.let {
                    Text(it, fontSize = 14.sp, color = Gray500)
                }
                vehicle.nickname?.let {
                    Text(it, fontSize = 13.sp, color = Gray500)
                }
                if (vehicle.isPrimary) {
                    Spacer(Modifier.height(4.dp))
                    Text("Primary", fontSize = 12.sp, color = Purple500, fontWeight = FontWeight.Medium)
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Gray500)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    )
                    DropdownMenuItem(
                        text = { Text("Remove", color = Red500) },
                        onClick = { showMenu = false; onRemove() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Red500) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleFormDialog(
    vehicle: VehicleDto?,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?, Boolean) -> Unit,
) {
    var vehicleNumber by remember { mutableStateOf(vehicle?.vehicleNumber ?: "") }
    var vehicleType by remember { mutableStateOf(vehicle?.vehicleType ?: "") }
    var nickname by remember { mutableStateOf(vehicle?.nickname ?: "") }
    var isPrimary by remember { mutableStateOf(vehicle?.isPrimary ?: false) }
    var expanded by remember { mutableStateOf(false) }
    val vehicleTypes = listOf("Petrol", "Diesel", "CNG")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle != null) "Edit Vehicle" else "Add Vehicle", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZapPayInput(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = "Vehicle Number",
                    placeholder = "e.g. MH01AB1234",
                )

                Text("Vehicle Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = vehicleType,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple500,
                            unfocusedBorderColor = Gray200,
                        ),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        vehicleTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    vehicleType = type
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                ZapPayInput(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = "Nickname (optional)",
                    placeholder = "e.g. My Car",
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        colors = CheckboxDefaults.colors(checkedColor = Purple500),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Set as primary vehicle", fontSize = 14.sp, color = Gray900)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        vehicleNumber,
                        vehicleType.ifBlank { null },
                        nickname.ifBlank { null },
                        isPrimary,
                    )
                },
                enabled = vehicleNumber.isNotBlank(),
            ) { Text("Save", color = Purple500) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
