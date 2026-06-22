package com.zappay.app.ui.customer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.zappay.app.data.remote.dto.VehicleDto
import com.zappay.app.ui.components.ErrorMessage
import com.zappay.app.ui.components.ZapPayButton
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
        topBar = {
            TopAppBar(
                title = { Text("My QR Codes", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            if (hasVehicles) {
                // Vehicle indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    state.vehicles.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                                .background(
                                    if (index == pagerState.currentPage) Purple500 else Gray200,
                                    CircleShape,
                                ),
                        )
                    }
                }
            }

            Text("Scan to Pay", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(Modifier.height(4.dp))
            Text("Show this code at the pump", color = Gray500, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))

            if (hasVehicles) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    val vehicle = state.vehicles[page]
                    val qrCode = state.qrCodesByVehicle[vehicle.id]

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Purple50),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Vehicle:", fontWeight = FontWeight.SemiBold, color = Purple500, fontSize = 13.sp)
                                Spacer(Modifier.width(8.dp))
                                Text("${vehicle.vehicleNumber} ${vehicle.vehicleType?.let { "($it)" } ?: ""}", color = Gray900, fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.size(280.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(4.dp),
                        ) {
                            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                                if (state.isLoading && qrCode == null) {
                                    CircularProgressIndicator(color = Purple500)
                                } else if (qrCode != null) {
                                    val bitmap = remember(qrCode) { generateQRBitmap(qrCode, 512) }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "QR for ${vehicle.vehicleNumber}",
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Tap Generate", color = Gray500, fontSize = 14.sp)
                                        Spacer(Modifier.height(8.dp))
                                        ZapPayButton(
                                            text = "Generate QR",
                                            onClick = { viewModel.generateQR(vehicle.id) },
                                            isLoading = state.isLoading,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ZapPayButton(
                                text = "Generate New",
                                onClick = { viewModel.generateQR(vehicle.id) },
                                variant = com.zappay.app.ui.components.ButtonVariant.OUTLINE,
                            )
                        }
                    }
                }
            } else {
                // No vehicles — prompt user to add one
                Card(
                    colors = CardDefaults.cardColors(containerColor = Yellow100),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No vehicles found", fontWeight = FontWeight.SemiBold, color = Gray900)
                        Spacer(Modifier.height(4.dp))
                        Text("Add a vehicle in My Vehicles to generate QR codes", color = Gray700, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Yellow100),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Swipe left/right to see QR codes for your other vehicles. Each code is encrypted.",
                    color = Gray700, fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }
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
