package com.zappay.app.ui.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.EvConnectorDto
import com.zappay.app.data.remote.dto.EvStationDto
import com.zappay.app.ui.theme.*

// ── 1. SEGMENTED MODE SELECTOR PILL (FUEL ⇄ EV) ─────────────────────
@Composable
fun ModeSelectorPill(
    isEvMode: Boolean,
    onModeSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
        ) {
            // Fuel Mode Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!isEvMode) Purple700 else Color.Transparent)
                    .clickable { onModeSelected(false) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = if (!isEvMode) White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Fuel Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (!isEvMode) White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // EV Mode Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isEvMode) Teal500 else Color.Transparent)
                    .clickable { onModeSelected(true) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isEvMode) White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "EV Mode ⚡",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isEvMode) White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ── 2. EV LIVE CHARGING CARD ─────────────────────────────────────────
@Composable
fun EvLiveChargingCard(
    state: CustomerUiState,
    onStartCharge: () -> Unit,
    onStopCharge: () -> Unit,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isCharging) {
        // IDLE STATE: READY TO PLUG & CHARGE
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF07242B)),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "AUTOPAY PLUG & CHARGE",
                            color = White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricGreen.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text("READY", color = ElectricGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("₹${"%.2f".format(state.balance)}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = White)
                Text(
                    "Auto-debits per kWh at verified EV supercharging hubs without OTP or PIN.",
                    fontSize = 12.sp,
                    color = White.copy(alpha = 0.8f),
                )

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onStartCharge,
                        colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f),
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("⚡ Start Live Supercharge", color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onNavigateToWallet,
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(White)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.9f),
                    ) {
                        Text("Manage Limit", color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    } else {
        // ACTIVE CHARGING STATE: LIVE ANIMATED METRICS
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulseAlpha",
        )

        Card(
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = ElectricCyan.copy(alpha = pulseAlpha),
                    shape = RoundedCornerShape(20.dp),
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF04191F)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(Modifier.padding(18.dp)) {
                // Header row with animated charging indicator
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ElectricGreen.copy(alpha = pulseAlpha)),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "⚡ SUPERCHARGING ACTIVE",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                    }
                    Text(
                        "${state.chargingSpeedKw} kW DC",
                        color = ElectricGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Hero Battery Gauge
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text("BATTERY LEVEL", fontSize = 11.sp, color = White.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
                        Text("${state.evBatterySoC}%", fontSize = 42.sp, fontWeight = FontWeight.Black, color = White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Est. Range", fontSize = 11.sp, color = White.copy(alpha = 0.7f))
                        Text("${state.evRangeKm} km", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ElectricGreen)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Animated Linear Progress towards Target Limit
                LinearProgressIndicator(
                    progress = { (state.evBatterySoC.toFloat() / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElectricGreen,
                    trackColor = White.copy(alpha = 0.15f),
                )

                Spacer(Modifier.height(14.dp))

                // Live 4-Metrics Grid
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EvMetricTile(title = "ENERGY", value = "${state.chargingKwhDelivered} kWh", modifier = Modifier.weight(1f))
                    EvMetricTile(title = "SPEED", value = "${state.chargingSpeedKw} kW", modifier = Modifier.weight(1f))
                    EvMetricTile(title = "TIME TO ${state.evTargetSocLimit}%", value = "${state.chargingTimeRemainingMins}m", modifier = Modifier.weight(1f))
                    EvMetricTile(title = "COST", value = "₹${"%.2f".format(state.chargingSessionCost)}", modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Connected at ${state.chargingStationName} • ${state.chargingConnectorType}",
                    fontSize = 11.sp,
                    color = White.copy(alpha = 0.7f),
                )

                Spacer(Modifier.height(14.dp))

                // Stop Charging Button
                Button(
                    onClick = onStopCharge,
                    colors = ButtonDefaults.buttonColors(containerColor = Danger500),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stop Charging & Settle (₹${"%.2f".format(state.chargingSessionCost)})", color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun EvMetricTile(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.padding(horizontal = 6.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 9.sp, color = White.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
        }
    }
}

// ── 3. EV BATTERY & VEHICLE TELEMETRY CARD ───────────────────────────
@Composable
fun EvBatteryTelemetryCard(
    state: CustomerUiState,
    onToggleAc: () -> Unit,
    onTogglePortLock: () -> Unit,
    onRotateLimit: () -> Unit,
    onOpenBatteryVault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            // Vehicle identification row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Teal500, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Tata Nexon EV Max", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("MH12AB1234 • 40.5 kWh LFP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Teal500.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text("CONNECTED", color = Teal500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Battery bar & Range summary
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "State of Charge (SoC): ${state.evBatterySoC}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "⚡ ${state.evRangeKm} km Range",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Teal500,
                )
            }

            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (state.evBatterySoC.toFloat() / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (state.evBatterySoC > 20) Teal500 else Danger500,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(Modifier.height(10.dp))

            // SOH and specs chips
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EvInfoChip("SOH: ${state.evBatteryHealthSoh}%", isValid = true)
                EvInfoChip("342V • 28°C", isValid = true)
                EvInfoChip("Cap: ${state.evTargetSocLimit}%", isValid = true)
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            // Interactive Remote Controls Row
            Text("Smart Telematics & Remote Controls", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Pre-condition AC
                FilterChip(
                    selected = state.evCabinPreconditioned,
                    onClick = onToggleAc,
                    label = { Text(if (state.evCabinPreconditioned) "Cabin AC: 22°C (ON)" else "Cabin AC: OFF", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Teal500.copy(alpha = 0.2f),
                        selectedLabelColor = Teal500,
                        selectedLeadingIconColor = Teal500,
                    ),
                    modifier = Modifier.weight(1f),
                )

                // Port Lock
                FilterChip(
                    selected = state.evPortLocked,
                    onClick = onTogglePortLock,
                    label = { Text(if (state.evPortLocked) "Port: Locked" else "Port: Free", fontSize = 11.sp) },
                    leadingIcon = { Icon(if (state.evPortLocked) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Success500.copy(alpha = 0.15f),
                        selectedLabelColor = Success700,
                        selectedLeadingIconColor = Success700,
                    ),
                    modifier = Modifier.weight(1f),
                )

                // Target Limit
                FilterChip(
                    selected = true,
                    onClick = onRotateLimit,
                    label = { Text("Cap: ${state.evTargetSocLimit}%", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenBatteryVault),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text("View Deep Battery Diagnostics", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Teal500)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Teal500, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun EvInfoChip(text: String, isValid: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isValid) Teal500.copy(alpha = 0.12f) else Danger500.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isValid) Teal500 else Danger500)
    }
}

// ── 4. EV ECOSYSTEM UTILITIES MATRIX (2x2) ───────────────────────────
@Composable
fun EvEcosystemUtilitiesMatrix(
    onOpenReservation: () -> Unit,
    onOpenRoutePlanner: () -> Unit,
    onOpenBatteryVault: () -> Unit,
    onOpenCarbonCredits: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text("EV Ecosystem Utilities", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EvQuickActionCard(
                title = "Reserve Charger Gun",
                subtitle = "15-min Hold • Baner Hub",
                icon = Icons.Default.Timer,
                color = Teal500,
                modifier = Modifier.weight(1f),
                onClick = onOpenReservation,
            )
            EvQuickActionCard(
                title = "EV Route Planner",
                subtitle = "Fast Chargers on Route",
                icon = Icons.Default.Navigation,
                color = Blue500,
                modifier = Modifier.weight(1f),
                onClick = onOpenRoutePlanner,
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EvQuickActionCard(
                title = "Battery Health Vault",
                subtitle = "Diagnostics & SOH",
                icon = Icons.Default.BatteryChargingFull,
                color = Color(0xFF7C3AED),
                modifier = Modifier.weight(1f),
                onClick = onOpenBatteryVault,
            )
            EvQuickActionCard(
                title = "Green Carbon Credits",
                subtitle = "182 kg CO2 Saved",
                icon = Icons.Default.Eco,
                color = Green500,
                modifier = Modifier.weight(1f),
                onClick = onOpenCarbonCredits,
            )
        }
    }
}

@Composable
private fun EvQuickActionCard(
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── 5. EV CHARGING HUB ITEM CARD ─────────────────────────────────────
@Composable
fun EvStationCard(
    station: EvStationDto,
    onClick: () -> Unit,
    onReserve: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(station.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "${station.distanceKm} km away • ${station.address}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (station.availableGuns > 0) Success500.copy(alpha = 0.15f) else Danger500.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        "${station.availableGuns}/${station.totalGuns} Guns Free",
                        color = if (station.availableGuns > 0) Success500 else Danger500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Connectors & Tariff row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val primaryConn = station.connectors.firstOrNull()
                Text(
                    primaryConn?.let { "${it.type} • ${it.powerKw.toInt()}kW" } ?: "CCS2 Fast Charge",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Teal500,
                )
                Text(
                    primaryConn?.let { "₹${"%.2f".format(it.tariffPerKwh)}/kWh" } ?: "₹18.50/kWh",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Amenities Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                station.amenities.take(3).forEach { am ->
                    Text(
                        am,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))

            // Action Buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onReserve,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reserve Gun", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        val gmmUri = Uri.parse("google.navigation:q=${station.latitude},${station.longitude}")
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, gmmUri).apply {
                                setPackage("com.google.android.apps.maps")
                            })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Directions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── 6. EV CHARGING HUB DETAIL SHEET ──────────────────────────────────
@Composable
fun EvStationDetailDialog(
    station: EvStationDto,
    onDismiss: () -> Unit,
    onReserve: (gunId: String) -> Unit,
    onStartCharge: (stationName: String, connector: String) -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(station.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${station.operator} • ${station.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("Live Gun Status & Connectors", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))

                station.connectors.forEach { conn ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(conn.connectorId, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${conn.type} • ${conn.powerKw.toInt()} kW", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Tariff: ₹${"%.2f".format(conn.tariffPerKwh)}/kWh", fontSize = 11.sp, color = Teal500, fontWeight = FontWeight.SemiBold)
                            }
                            if (conn.status == "AVAILABLE") {
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onStartCharge(station.name, "${conn.type} (${conn.connectorId})")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp),
                                ) {
                                    Text("⚡ Plug In", fontSize = 11.sp, color = White)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Danger500.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text("OCCUPIED", fontSize = 10.sp, color = Danger500, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("Station Amenities: ${station.amenities.joinToString(", ")}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val firstFree = station.connectors.firstOrNull { it.status == "AVAILABLE" }?.connectorId ?: "GUN-01"
                    onDismiss()
                    onReserve(firstFree)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal500),
            ) {
                Text("Reserve 15 Mins")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

// ── 7. EV ROUTE PLANNER DIALOG ───────────────────────────────────────
@Composable
fun EvRoutePlannerDialog(
    currentRangeKm: Int,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚡ Intelligent EV Route Planner", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Current Battery Range: $currentRangeKm km", fontWeight = FontWeight.Bold, color = Teal500, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text("Fast charging stop recommendations for popular corridors with zero range anxiety:", fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Pune ➔ Mumbai Expressway (148 km)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("• Urse Toll Plaza (Km 42) — Statiq 150kW Hub (6 Guns)", fontSize = 11.sp)
                        Text("• Khalapur Food Mall (Km 92) — Tata Power 60kW (4 Guns)", fontSize = 11.sp)
                        Text("Status: REACHABLE WITHOUT RECHARGE", color = Success500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Pune ➔ Mahabaleshwar (120 km)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("• Shirwal MIDC — Jio-bp pulse 120kW Fast Gun", fontSize = 11.sp)
                        Text("Status: 1 OPTIONAL CHARGING STOP", color = Warning500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        },
    )
}

// ── 8. EV BATTERY VAULT & DIAGNOSTICS DIALOG ─────────────────────────
@Composable
fun EvBatteryVaultDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚡ 360° EV Battery Health Vault", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                EvMetricRow("Battery Chemistry", "LFP (Lithium Iron Phosphate)")
                EvMetricRow("Total Pack Capacity", "40.5 kWh")
                EvMetricRow("Usable Capacity", "38.2 kWh")
                EvMetricRow("State of Health (SOH)", "98.5% (Optimal)")
                EvMetricRow("Total Charge Cycles", "184 Cycles")
                EvMetricRow("Cell Voltage Variance", "± 0.012 V (Balanced)")
                EvMetricRow("Pack Operating Temp", "28.5°C (Cool)")
                EvMetricRow("OEM Warranty", "Valid till 2032 (1,60,000 km)")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        },
    )
}

// ── 9. GREEN CARBON CREDITS DIALOG ───────────────────────────────────
@Composable
fun EvCarbonCreditsDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🌿 ESG Green Carbon Certificate", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Congratulations on driving electric with ZapPay 2.0!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                EvMetricRow("Total EV Kilometres", "3,840 km")
                EvMetricRow("Petrol Saved", "228 Litres")
                EvMetricRow("CO2 Tailpipe Emissions Avoided", "182.4 kg CO2")
                EvMetricRow("Trees Saved Equivalent", "8.2 Trees")
                EvMetricRow("Green ESG Score", "A+ (99.2 / 100)")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Share Certificate") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun EvMetricRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
