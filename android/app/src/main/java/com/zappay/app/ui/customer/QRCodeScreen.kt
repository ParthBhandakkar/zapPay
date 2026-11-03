package com.zappay.app.ui.customer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVehicleInfo()
        viewModel.loadVehicles()
    }

    val hasVehicles = state.vehicles.isNotEmpty()
    val pageCount = maxOf(1, state.vehicles.size)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    LaunchedEffect(pagerState.currentPage) {
        if (state.vehicles.isNotEmpty()) {
            val vehicle = state.vehicles[pagerState.currentPage]
            if (!state.qrCodesByVehicle.containsKey(vehicle.id)) {
                viewModel.generateQR(vehicle.id)
            }
        }
    }

    Scaffold(
        topBar = { ZapPayTopBar(title = "My QR Codes", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            Text("Scan to Pay", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text("Show this code at the fuel pump", color = Neutral500, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))

            if (hasVehicles) {
                // Vehicle chip selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    state.vehicles.forEachIndexed { index, vehicle ->
                        FilterChip(
                            selected = index == pagerState.currentPage,
                            onClick = {},
                            label = { Text(vehicle.vehicleNumber, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary50,
                                selectedLabelColor = Primary700,
                            ),
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    val vehicle = state.vehicles[page]
                    val qrCode = state.qrCodesByVehicle[vehicle.id]

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // QR Card with premium border
                        Card(
                            modifier = Modifier
                                .size(300.dp)
                                .border(
                                    width = 3.dp,
                                    brush = GoldGradient,
                                    shape = RoundedCornerShape(24.dp),
                                ),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(6.dp),
                        ) {
                            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                                if (state.isLoading && qrCode == null) {
                                    CircularProgressIndicator(color = Primary500, strokeWidth = 3.dp)
                                } else if (qrCode != null) {
                                    val bitmap = remember(qrCode) { generateQRBitmap(qrCode, 512) }
                                    if (bitmap != null) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "QR for ${vehicle.vehicleNumber}",
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                            // Center brand mark
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Primary900),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text("Z", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
                                            }
                                        }
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Outlined.QrCode, contentDescription = null, tint = Neutral300, modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Tap to generate", color = Neutral500, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Vehicle info chip
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Primary50),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = Primary500, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(vehicle.vehicleNumber, fontWeight = FontWeight.SemiBold, color = Primary700, fontSize = 14.sp)
                                vehicle.vehicleType?.let {
                                    Text(" · $it", color = Neutral500, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        ZapPayButton(
                            text = "Refresh QR",
                            onClick = { viewModel.generateQR(vehicle.id) },
                            variant = ButtonVariant.OUTLINE,
                            isLoading = state.isLoading,
                            modifier = Modifier.widthIn(max = 220.dp),
                        )
                    }
                }
            } else {
                ZapPayEmptyState(
                    icon = Icons.Outlined.DirectionsCar,
                    title = "No vehicles added",
                    subtitle = "Add a vehicle in My Vehicles to generate QR codes for fuel payment",
                )
            }

            Spacer(Modifier.weight(1f))

            // Security badge
            Card(
                colors = CardDefaults.cardColors(containerColor = Success50),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Success500, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Encrypted & Secure", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Success700)
                        Text("Each QR is uniquely generated and expires after use", fontSize = 11.sp, color = Neutral500)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun generateQRBitmap(data: String, size: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (_: Exception) { null }
}
