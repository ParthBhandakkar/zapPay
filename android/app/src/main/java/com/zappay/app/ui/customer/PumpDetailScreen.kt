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
        topBar = { ZapPayTopBar(title = state.pumpName, onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            
            // Status badge + distance
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ZapPayBadge(
                    text = if (state.isOpen) "Open" else "Closed",
                    color = if (state.isOpen) Success500 else Danger500,
                )
                Text(
                    "${"%.1f".format(state.distanceKm)} km away",
                    fontSize = 14.sp,
                    color = Primary500,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Address
            Text(
                state.address,
                fontSize = 14.sp,
                color = Neutral500,
            )

            Spacer(Modifier.height(32.dp))

            // Fuel Prices section
            SectionHeader("Fuel Prices")

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
                    colors = CardDefaults.cardColors(containerColor = Neutral50),
                ) {
                    Text(
                        "No fuel prices available",
                        modifier = Modifier.padding(24.dp),
                        color = Neutral500,
                        fontSize = 14.sp,
                    )
                }
                else -> {
                    // Price table
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Neutral100)
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                            ) {
                                Text("Fuel Type", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f), color = Neutral700)
                                Text("Price", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Neutral700)
                            }

                            state.fuelPrices.forEachIndexed { index, fp ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        fp.fuelType,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "₹${"%.2f".format(fp.price)}/L",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary500,
                                    )
                                }
                                if (index < state.fuelPrices.lastIndex) {
                                    HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Pump info card
            SectionHeader("Pump Information")

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary50),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    InfoRow("Pump ID", "#${state.pumpId}")
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Name", state.pumpName)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Address", state.address)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Distance", "${"%.1f".format(state.distanceKm)} km")
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Status", if (state.isOpen) "Open" else "Closed")
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Fuel Types Available", state.fuelPrices.joinToString(", ") { it.fuelType }.ifEmpty { "N/A" })
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Neutral500)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
