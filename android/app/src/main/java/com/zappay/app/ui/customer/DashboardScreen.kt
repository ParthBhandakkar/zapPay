package com.zappay.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.MandateDto
import com.zappay.app.data.remote.dto.StationDto
import com.zappay.app.data.remote.dto.ConsumerVehicleDto
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

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

    var showVaultSheet by remember { mutableStateOf(false) }
    var showCircleSheet by remember { mutableStateOf(false) }
    var showPuritySheet by remember { mutableStateOf(false) }
    var showSosSimulatorSheet by remember { mutableStateOf(false) }
    var alertSentToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Purple500),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("ZP", color = White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("ZapPay 2.0", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("Aakash Verma • Personal & Family Vault", fontSize = 11.sp, color = Gray600)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToQR) {
                        Icon(Icons.Default.QrCode, contentDescription = "My QR", tint = Purple500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Notification toast if alert triggered
            if (alertSentToast != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Green100),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green500)
                        Spacer(Modifier.width(8.dp))
                        Text(alertSentToast!!, color = Green500, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { alertSentToast = null }) { Text("Dismiss", fontSize = 11.sp) }
                    }
                }
            }

            // ── ZERO-WAIT AUTOPAY BANNER ─────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Purple700),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("UPI AUTOPAY • ZERO-WAIT FUELING", color = White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Green500.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text("ACTIVE", color = Green500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("₹${"%.2f".format(state.balance)}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = White)
                    Text("Auto-debits up to ₹2,500 without PIN at verified pumps", fontSize = 12.sp, color = White.copy(alpha = 0.8f))

                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onNavigateToWallet,
                            colors = ButtonDefaults.buttonColors(containerColor = White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Manage Limits", color = Purple700, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { showCircleSheet = true },
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(White)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("UPI Circle", color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── 360° VEHICLE IDENTITY & COMPLIANCE VAULT ─────────────────────
            val primaryVehicle = state.consumerVehicles.firstOrNull()
            VehicleVaultBannerCard(
                vehicle = primaryVehicle,
                onOpenVault = { showVaultSheet = true },
            )

            Spacer(Modifier.height(18.dp))

            // ── QUICK ACTION MATRIX ──────────────────────────────────────────
            Text("Ecosystem Utilities", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionCard(
                    title = "Compliance Vault",
                    subtitle = "RC, Insurance, PUC",
                    icon = Icons.Default.VerifiedUser,
                    color = Blue500,
                    modifier = Modifier.weight(1f),
                    onClick = { showVaultSheet = true },
                )
                QuickActionCard(
                    title = "UPI Circle",
                    subtitle = "Family Fuel Caps",
                    icon = Icons.Default.Group,
                    color = Purple500,
                    modifier = Modifier.weight(1f),
                    onClick = { showCircleSheet = true },
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionCard(
                    title = "Fuel Purity & Mileage",
                    subtitle = "Pump Scores & km/L",
                    icon = Icons.Default.Speed,
                    color = Green500,
                    modifier = Modifier.weight(1f),
                    onClick = { showPuritySheet = true },
                )
                QuickActionCard(
                    title = "Masked Call & SOS",
                    subtitle = "Passerby Alert Test",
                    icon = Icons.Default.PhoneCallback,
                    color = Color(0xFFE65100),
                    modifier = Modifier.weight(1f),
                    onClick = { showSosSimulatorSheet = true },
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── NEARBY CERTIFIED PUMPS WITH VERIFIED DENSITY ─────────────────
            Text("Nearby Verified Petrol Pumps", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))

            val stations = state.nearbyStations.ifEmpty {
                listOf(
                    StationDto(1, "Bharat Petroleum - Ring Road", "Bilaspur", 743.2, 836.5, 4.95, "0% DEALER SUBSIDIZED"),
                    StationDto(2, "Indian Oil Expressway Hub", "Raipur", 741.8, 835.0, 4.88, "0.5% MDR"),
                )
            }

            stations.forEach { station ->
                StationListItem(station = station)
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // ── MODALS / BOTTOM SHEETS ─────────────────────────────────────────────

    // 1. Compliance Vault Sheet
    if (showVaultSheet) {
        val v = state.consumerVehicles.firstOrNull()
        AlertDialog(
            onDismissRequest = { showVaultSheet = false },
            title = { Text("360° Vehicle Compliance Vault", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    DetailRow("Vehicle Number", v?.vrnNumber ?: "CG-10-AB-4582")
                    DetailRow("Vahan Verification", if (v?.vahanVerified != false) "VERIFIED (MoRTH)" else "PENDING")
                    DetailRow("Permitted Fuel", v?.fuelType ?: "PETROL ONLY")
                    DetailRow("Insurance Validity", v?.insuranceExpiryDate ?: "Valid till 2027-04-15")
                    DetailRow("PUC Validity", v?.pucExpiryDate ?: "Valid till 2026-12-10")
                    DetailRow("Tank Capacity", "${v?.tankCapacityLitres ?: 44.0} Litres")
                    DetailRow("Average Mileage", "${v?.avgMileageKmpl ?: 17.2} km/L")
                    DetailRow("Blood Group (SOS)", v?.bloodGroup ?: "O+")
                    DetailRow("Emergency Contact", "${v?.emergencyContactName ?: "Pooja Verma"} (${v?.emergencyContactPhone ?: "9876543211"})")
                }
            },
            confirmButton = {
                Button(onClick = { showVaultSheet = false }) { Text("Done") }
            },
        )
    }

    // 2. UPI Circle Sheet
    if (showCircleSheet) {
        AlertDialog(
            onDismissRequest = { showCircleSheet = false },
            title = { Text("NPCI UPI Circle (Delegated Fueling)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Delegate capped fueling to college students, family members, or domestic staff with time and rupee limits.", fontSize = 12.sp, color = Gray700)
                    Spacer(Modifier.height(12.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = Purple50), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(10.dp)) {
                            Text("Rohan Verma (Son - 2 Wheeler)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Monthly Cap: ₹1,500 • Fuel Lock: Petrol", fontSize = 12.sp, color = Gray600)
                            Text("Status: ACTIVE & DELEGATED", fontSize = 11.sp, color = Green500, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = Gray100), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(10.dp)) {
                            Text("Sunita Bai (Family Car Driver)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Per Fill Cap: ₹2,000 • Diesel Only", fontSize = 12.sp, color = Gray600)
                            Text("Status: ACTIVE & DELEGATED", fontSize = 11.sp, color = Green500, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCircleSheet = false }) { Text("Close") }
            },
        )
    }

    // 3. Fuel Purity & Mileage Sheet
    if (showPuritySheet) {
        var odoInput by remember { mutableStateOf("14350") }
        var litresInput by remember { mutableStateOf("10.0") }
        AlertDialog(
            onDismissRequest = { showPuritySheet = false },
            title = { Text("Fuel Purity & Mileage Calculator", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Log your odometer reading to compute exact km/L and help the community rate pump purity.", fontSize = 12.sp, color = Gray600)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = odoInput,
                        onValueChange = { odoInput = it },
                        label = { Text("Current Odometer (km)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = litresInput,
                        onValueChange = { litresInput = it },
                        label = { Text("Litres Filled") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPuritySheet = false
                    alertSentToast = "Odometer logged! Calculated mileage: 16.8 km/L. Pump density purity verified."
                }) { Text("Submit Reading") }
            },
            dismissButton = {
                TextButton(onClick = { showPuritySheet = false }) { Text("Cancel") }
            },
        )
    }

    // 4. Masked Parking & SOS Simulator
    if (showSosSimulatorSheet) {
        AlertDialog(
            onDismissRequest = { showSosSimulatorSheet = false },
            title = { Text("24/7 Sticker Utility (Simulator)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Test how a passerby or first responder scanning your vehicle sticker interacts with privacy protection.", fontSize = 12.sp, color = Gray600)
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            showSosSimulatorSheet = false
                            alertSentToast = "Masked Wrong Parking Alert sent via WhatsApp to vehicle owner. Number stayed hidden!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Simulate 'Wrong Parking Alert'")
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showSosSimulatorSheet = false
                            alertSentToast = "Simulated Lights On / Window Open notification dispatched."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gray700),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Simulate 'Lights On Alert'")
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showSosSimulatorSheet = false
                            alertSentToast = "🚨 Emergency SOS alert bridged to Pooja Verma with GPS coordinates."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Red500),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Simulate 'Emergency SOS Alert'")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSosSimulatorSheet = false }) { Text("Close") }
            },
        )
    }
}

// ── SUB-WIDGETS ────────────────────────────────────────────────────────────

@Composable
private fun VehicleVaultBannerCard(
    vehicle: ConsumerVehicleDto?,
    onOpenVault: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenVault),
        colors = CardDefaults.cardColors(containerColor = Gray100),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Purple500)
                    Spacer(Modifier.width(8.dp))
                    Text(vehicle?.vrnNumber ?: "CG-10-AB-4582", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Green100)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("VAHAN VERIFIED", color = Green500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(vehicle?.makeModel ?: "Tata Nexon EV / Petrol • 44L Tank", fontSize = 12.sp, color = Gray700)

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VaultStatusPill("Insurance: 2027", isValid = true)
                VaultStatusPill("PUC: Valid", isValid = true)
                VaultStatusPill("${vehicle?.avgMileageKmpl ?: 17.2} km/L", isValid = true)
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("View 360° Identity & Compliance Vault", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Purple500)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Purple500, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun VaultStatusPill(text: String, isValid: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isValid) Green100 else Red100)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isValid) Green500 else Red500)
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = Gray600)
        }
    }
}

@Composable
private fun StationListItem(station: StationDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(station.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(station.city, fontSize = 11.sp, color = Gray600)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Petrol Density: ${station.verifiedPetrolDensity ?: 742.0} kg/m³", fontSize = 11.sp, color = Green500, fontWeight = FontWeight.SemiBold)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("${station.trustScore ?: 5.0}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text(station.mdrMode ?: "0% DEALER FEE", fontSize = 9.sp, color = Purple500, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Gray600, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}
