package com.zappay.app.ui.customer

import androidx.compose.foundation.background
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
        topBar = { ZapPayTopBar(title = "My Vehicles", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary500,
                contentColor = White,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
        } else if (state.vehicles.isEmpty()) {
            ZapPayEmptyState(
                icon = Icons.Outlined.DirectionsCar,
                title = "No vehicles added",
                subtitle = "Add your vehicles to easily pay for fuel and track expenses.",
                actionText = "Add Vehicle",
                onAction = { showAddDialog = true },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                
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
                
                item { Spacer(Modifier.height(80.dp)) } // Space for FAB
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
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Danger500) },
            title = { Text("Remove Vehicle", fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to remove ${vehicleToRemove!!.vehicleNumber}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeVehicle(vehicleToRemove!!.id)
                        showRemoveDialog = false
                        vehicleToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger500)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRemoveDialog = false
                    vehicleToRemove = null
                }) { Text("Cancel", color = Neutral600) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
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

    val vehicleIcon = when(vehicle.vehicleType?.lowercase()) {
        "bike", "scooty" -> Icons.Outlined.TwoWheeler
        "auto" -> Icons.Outlined.ElectricRickshaw
        else -> Icons.Outlined.DirectionsCar
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Vehicle Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Primary50),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(vehicleIcon, contentDescription = null, tint = Primary500, modifier = Modifier.size(24.dp))
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(vehicle.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        if (vehicle.isPrimary) {
                            Spacer(Modifier.width(8.dp))
                            ZapPayBadge("Primary", color = Accent600)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        vehicle.vehicleType?.let {
                            Text(it, fontSize = 13.sp, color = Neutral500)
                        }
                        if (vehicle.vehicleType != null && vehicle.nickname != null) {
                            Text(" • ", fontSize = 13.sp, color = Neutral400)
                        }
                        vehicle.nickname?.let {
                            Text(it, fontSize = 13.sp, color = Neutral500)
                        }
                    }
                }
                
                // Options Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Neutral500)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Neutral700) },
                        )
                        DropdownMenuItem(
                            text = { Text("Remove", color = Danger500) },
                            onClick = { showMenu = false; onRemove() },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Danger500) },
                        )
                    }
                }
            }
            
            // Total spent stats (Placeholder for future)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth().background(Neutral50).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocalGasStation, contentDescription = null, tint = Primary400, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ready for payment", fontSize = 12.sp, color = Neutral600)
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
    
    val vehicleTypes = listOf("Car", "Bike", "Scooty", "Auto", "Truck")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle != null) "Edit Vehicle" else "Add Vehicle", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ZapPayInput(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it.uppercase() },
                    label = "Vehicle Number",
                    placeholder = "e.g. MH01AB1234",
                )

                Column {
                    Text("Vehicle Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Neutral700)
                    Spacer(Modifier.height(8.dp))
                    // Replace Dropdown with premium chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        vehicleTypes.take(3).forEach { type ->
                            FilterChip(
                                selected = vehicleType == type,
                                onClick = { vehicleType = type },
                                label = { Text(type, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary50,
                                    selectedLabelColor = Primary700,
                                ),
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        vehicleTypes.drop(3).forEach { type ->
                            FilterChip(
                                selected = vehicleType == type,
                                onClick = { vehicleType = type },
                                label = { Text(type, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary50,
                                    selectedLabelColor = Primary700,
                                ),
                            )
                        }
                    }
                }

                ZapPayInput(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = "Nickname (optional)",
                    placeholder = "e.g. My Daily Commute",
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Primary50).padding(8.dp)
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        colors = CheckboxDefaults.colors(checkedColor = Primary500),
                    )
                    Text("Set as primary vehicle", fontSize = 14.sp, color = Primary900, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        vehicleNumber,
                        vehicleType.ifBlank { null },
                        nickname.ifBlank { null },
                        isPrimary,
                    )
                },
                enabled = vehicleNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary500)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Neutral600) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
