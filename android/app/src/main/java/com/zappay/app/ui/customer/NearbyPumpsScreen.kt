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
    var locationLoaded by remember { mutableStateOf(false) }
    var locationPermissionRequested by remember { mutableStateOf(false) }

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
        topBar = { ZapPayTopBar(title = "Nearby Pumps", onBack = onBack) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.nearbyPumps.isEmpty() -> LoadingScreen()
                state.error != null && state.nearbyPumps.isEmpty() -> ErrorMessage(
                    message = state.error!!,
                    onRetry = {
                        if (locationHelper.hasLocationPermission()) {
                            locationHelper.getFreshLocation { loc ->
                                viewModel.loadNearbyPumps(loc?.latitude ?: 19.0760, loc?.longitude ?: 72.8777, 2000.0)
                            }
                        } else {
                            viewModel.loadNearbyPumps(19.0760, 72.8777, 2000.0)
                        }
                    },
                )
                state.nearbyPumps.isEmpty() -> ZapPayEmptyState(
                    icon = Icons.Outlined.LocalGasStation,
                    title = "No nearby pumps found",
                    subtitle = "We couldn't find any partner pumps in your area. Try increasing your search radius or come back later.",
                    actionText = "Refresh Search",
                    onAction = {
                        if (locationHelper.hasLocationPermission()) {
                            locationHelper.getFreshLocation { loc ->
                                viewModel.loadNearbyPumps(loc?.latitude ?: 19.0760, loc?.longitude ?: 72.8777, 2000.0)
                            }
                        } else {
                            viewModel.loadNearbyPumps(19.0760, 72.8777, 2000.0)
                        }
                    }
                )
                else -> LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(state.nearbyPumps) { pump ->
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
