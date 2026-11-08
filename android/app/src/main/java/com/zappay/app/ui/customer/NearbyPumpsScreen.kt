package com.zappay.app.ui.customer

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.EvConnectorDto
import com.zappay.app.data.remote.dto.EvStationDto
import com.zappay.app.data.remote.dto.NearbyPumpDto
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import com.zappay.app.util.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyPumpsScreen(
    viewModel: NearbyPumpsViewModel,
    locationHelper: LocationHelper,
    onBack: () -> Unit,
    onPumpClick: (NearbyPumpDto) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var filterMode by remember { mutableStateOf(0) } // 0 = Fuel, 1 = EV Fast Hubs
    var locationLoaded by remember { mutableStateOf(false) }
    var locationPermissionRequested by remember { mutableStateOf(false) }

    val evStations = remember {
        listOf(
            EvStationDto(
                id = 1,
                name = "Tata Power EZ Charge - Baner",
                operator = "Tata Power",
                address = "Near Balewadi High St, Baner",
                city = "Pune",
                distanceKm = 1.8,
                latitude = 18.5590,
                longitude = 73.7868,
                totalGuns = 4,
                availableGuns = 3,
                connectors = listOf(
                    EvConnectorDto("GUN-01", "CCS2", 60.0, 18.5, "AVAILABLE"),
                    EvConnectorDto("GUN-02", "CCS2", 60.0, 18.5, "OCCUPIED"),
                    EvConnectorDto("GUN-03", "Type 2", 22.0, 15.0, "AVAILABLE"),
                    EvConnectorDto("GUN-04", "Type 2", 22.0, 15.0, "AVAILABLE"),
                ),
                amenities = listOf("Café Coffee Day", "Restroom", "24/7 Security", "WiFi"),
                rating = 4.8,
            ),
            EvStationDto(
                id = 2,
                name = "Jio-bp pulse Fast Charging Hub",
                operator = "Jio-bp pulse",
                address = "Phase 1, Rajiv Gandhi Infotech Park",
                city = "Hinjawadi",
                distanceKm = 3.4,
                latitude = 18.5913,
                longitude = 73.7389,
                totalGuns = 4,
                availableGuns = 2,
                connectors = listOf(
                    EvConnectorDto("GUN-01", "CCS2", 120.0, 19.0, "AVAILABLE"),
                    EvConnectorDto("GUN-02", "CCS2", 120.0, 19.0, "AVAILABLE"),
                    EvConnectorDto("GUN-03", "CCS2", 60.0, 18.0, "OCCUPIED"),
                    EvConnectorDto("GUN-04", "Type 2", 22.0, 15.0, "OCCUPIED"),
                ),
                amenities = listOf("Wild Bean Cafe", "EV Lounge", "ATM"),
                rating = 4.9,
            ),
            EvStationDto(
                id = 3,
                name = "Statiq EV Mega Hub - Urse",
                operator = "Statiq",
                address = "Pune-Mumbai Expressway, Urse Toll Plaza",
                city = "Urse",
                distanceKm = 12.5,
                latitude = 18.7200,
                longitude = 73.6500,
                totalGuns = 6,
                availableGuns = 5,
                connectors = listOf(
                    EvConnectorDto("GUN-01", "CCS2", 150.0, 17.8, "AVAILABLE"),
                    EvConnectorDto("GUN-02", "CCS2", 150.0, 17.8, "AVAILABLE"),
                    EvConnectorDto("GUN-03", "CCS2", 120.0, 17.8, "AVAILABLE"),
                    EvConnectorDto("GUN-04", "CCS2", 60.0, 17.0, "AVAILABLE"),
                    EvConnectorDto("GUN-05", "CCS2", 60.0, 17.0, "AVAILABLE"),
                    EvConnectorDto("GUN-06", "Type 2", 22.0, 14.5, "OCCUPIED"),
                ),
                amenities = listOf("Food Mall", "Kids Play Area", "Lounge"),
                rating = 4.7,
            ),
            EvStationDto(
                id = 4,
                name = "Zeon Electric Hub - Balewadi",
                operator = "Zeon",
                address = "Balewadi High Street Parking P2",
                city = "Balewadi",
                distanceKm = 4.2,
                latitude = 18.5720,
                longitude = 73.7740,
                totalGuns = 2,
                availableGuns = 2,
                connectors = listOf(
                    EvConnectorDto("GUN-01", "CCS2", 50.0, 18.0, "AVAILABLE"),
                    EvConnectorDto("GUN-02", "Type 2", 22.0, 15.0, "AVAILABLE"),
                ),
                amenities = listOf("Shopping", "Restaurants", "Covered Parking"),
                rating = 4.6,
            ),
        )
    }

    val fallbackFuelPumps = remember {
        listOf(
            NearbyPumpDto(1, "Bharat Petroleum - Ring Road Hub", "Bilaspur Ring Road, Near High Court", "Bilaspur", 22.0797, 82.1409, 1.2, listOf("PETROL", "DIESEL"), true, null),
            NearbyPumpDto(2, "Indian Oil Expressway Smart Forecourt", "Raipur Highway, Bilaspur", "Bilaspur", 22.0500, 82.1200, 2.6, listOf("PETROL", "DIESEL", "XP95"), true, null),
            NearbyPumpDto(3, "Hindustan Petroleum - City Central", "Vyapar Vihar, Bilaspur", "Bilaspur", 22.0850, 82.1550, 3.8, listOf("PETROL", "DIESEL"), true, null),
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionRequested = true
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            locationHelper.getFreshLocation { loc ->
                if (loc != null) {
                    viewModel.loadNearbyPumps(loc.latitude, loc.longitude, 50.0)
                }
                locationLoaded = true
            }
        } else {
            viewModel.loadNearbyPumps(19.0760, 72.8777, 2000.0)
            locationLoaded = true
        }
    }

    LaunchedEffect(Unit) {
        if (locationHelper.hasLocationPermission()) {
            locationHelper.getFreshLocation { loc ->
                if (loc != null) {
                    viewModel.loadNearbyPumps(loc.latitude, loc.longitude, 50.0)
                } else {
                    viewModel.loadNearbyPumps(19.0760, 72.8777, 2000.0)
                }
                locationLoaded = true
            }
        } else if (!locationPermissionRequested) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            viewModel.loadNearbyPumps(19.0760, 72.8777, 2000.0)
            locationLoaded = true
        }
    }

    Scaffold(
        topBar = { ZapPayTopBar(title = if (filterMode == 1) "EV Charging Hubs" else "Nearby Pumps", onBack = onBack) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filterMode == 0,
                    onClick = { filterMode = 0 },
                    label = { Text("⛽ Petrol / Diesel", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary50,
                        selectedLabelColor = Primary700,
                    ),
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = filterMode == 1,
                    onClick = { filterMode = 1 },
                    label = { Text("⚡ EV Fast Hubs", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Teal500.copy(alpha = 0.15f),
                        selectedLabelColor = Teal500,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            if (filterMode == 1) {
                // EV Hubs list
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(evStations) { stn ->
                        EvStationCard(
                            station = stn,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode("${stn.name}, ${stn.address}")}"))
                                context.startActivity(intent)
                            },
                            onReserve = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode("${stn.name}, ${stn.address}")}"))
                                context.startActivity(intent)
                            },
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            } else {
                val pumpsToDisplay = state.nearbyPumps.ifEmpty { fallbackFuelPumps }
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(pumpsToDisplay) { pump ->
                        PumpCard(pump = pump, onClick = { onPumpClick(pump) })
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PumpCard(
    pump: com.zappay.app.data.remote.dto.NearbyPumpDto,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    ZapPayCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Primary50),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.LocalGasStation, contentDescription = null, tint = Primary500, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pump.pumpName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (pump.isOpen != null) {
                        Spacer(Modifier.width(8.dp))
                        ZapPayBadge(
                            text = if (pump.isOpen) "Open" else "Closed",
                            color = if (pump.isOpen) Success500 else Danger500
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    pump.address,
                    fontSize = 13.sp,
                    color = Neutral500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${"%.1f".format(pump.distanceKm)} km away",
                        fontSize = 13.sp,
                        color = Primary500,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            val gmmUri = Uri.parse("google.navigation:q=${pump.latitude},${pump.longitude}")
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, gmmUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                })
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Directions", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
