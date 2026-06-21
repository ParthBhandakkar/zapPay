package com.zappay.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.NearbyPumpDto
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyPumpsScreen(
    viewModel: NearbyPumpsViewModel,
    onBack: () -> Unit,
    onPumpClick: (NearbyPumpDto) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadNearbyPumps(19.0760, 72.8777, 50.0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Pumps", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.nearbyPumps.isEmpty() -> LoadingScreen()
                state.error != null && state.nearbyPumps.isEmpty() -> ErrorMessage(
                    message = state.error!!,
                    onRetry = { viewModel.loadNearbyPumps(19.0760, 72.8777, 50.0) },
                )
                state.nearbyPumps.isEmpty() -> ErrorMessage("No nearby pumps found")
                else -> LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                    items(state.nearbyPumps) { pump ->
                        PumpCard(pump = pump, onClick = { onPumpClick(pump) })
                        HorizontalDivider(color = Gray100)
                    }
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
    ZapPayCard(modifier = Modifier.padding(vertical = 6.dp), onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    pump.pumpName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (pump.isOpen != null) {
                    Text(
                        if (pump.isOpen) "Open" else "Closed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (pump.isOpen) Green500 else Red500,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (pump.isOpen) Green500.copy(alpha = 0.12f)
                                else Red500.copy(alpha = 0.12f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                pump.address,
                fontSize = 13.sp,
                color = Gray500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(2.dp))

            Text(
                "${"%.1f".format(pump.distanceKm)} km away",
                fontSize = 13.sp,
                color = Purple500,
            )

            if (!pump.fuelPrices.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Gray100)
                Spacer(Modifier.height(8.dp))
                pump.fuelPrices.forEach { fp ->
                    Text(
                        "${fp.fuelType}: ₹${"%.2f".format(fp.price)}/L",
                        fontSize = 13.sp,
                        color = Gray700,
                    )
                }
            }
        }
    }
}
