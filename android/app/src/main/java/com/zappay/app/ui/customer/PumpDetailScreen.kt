package com.zappay.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpDetailScreen(
    viewModel: PumpDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.pumpName, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
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
            // Status badge + distance
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (state.isOpen) "● Open" else "● Closed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.isOpen) Green500 else Red500,
                )
                Text(
                    "${"%.1f".format(state.distanceKm)} km away",
                    fontSize = 14.sp,
                    color = Purple500,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Address
            Text(
                state.address,
                fontSize = 14.sp,
                color = Gray700,
            )

            Spacer(Modifier.height(24.dp))

            // Fuel Prices section
            Text(
                "Fuel Prices",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900,
            )

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> LoadingScreen()
                state.error != null -> ErrorMessage(
                    message = state.error!!,
                    onRetry = { viewModel.clearError() },
                )
                state.fuelPrices.isEmpty() -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Gray50),
                ) {
                    Text(
                        "No fuel prices available",
                        modifier = Modifier.padding(24.dp),
                        color = Gray500,
                        fontSize = 14.sp,
                    )
                }
                else -> {
                    // Price header
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Gray100, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text("Fuel Type", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text("Price", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    Column(Modifier.fillMaxWidth()) {
                        state.fuelPrices.forEachIndexed { index, fp ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(if (index % 2 == 0) White else Gray50)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    fp.fuelType,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    "₹${"%.2f".format(fp.price)}/L",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Purple500,
                                )
                            }
                            if (index < state.fuelPrices.lastIndex) {
                                HorizontalDivider(color = Gray100)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Pump info card
            Text(
                "Pump Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900,
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Purple50),
            ) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("Pump ID", "#${state.pumpId}")
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Name", state.pumpName)
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Address", state.address)
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Distance", "${"%.1f".format(state.distanceKm)} km")
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Status", if (state.isOpen) "Open" else "Closed")
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Fuel Types Available", state.fuelPrices.joinToString(", ") { it.fuelType }.ifEmpty { "N/A" })
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Gray500)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray900)
    }
}
