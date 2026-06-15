package com.zappay.app.ui.customer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
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

    LaunchedEffect(Unit) { viewModel.generateQR() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My QR Code", fontWeight = FontWeight.SemiBold) },
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
            Text("Scan to Pay", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(Modifier.height(8.dp))
            Text("Show this code at the pump", color = Gray500, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = Purple500)
            } else if (state.qrData != null) {
                // QR Code
                Card(
                    modifier = Modifier.size(280.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(4.dp),
                ) {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        val bitmap = remember(state.qrData) {
                            generateQRBitmap(state.qrData!!, 512)
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Text("Failed to generate QR", color = Red500)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                if (state.qrExpiresAt != null) {
                    Text("Expires: ${state.qrExpiresAt}", color = Gray500, fontSize = 12.sp)
                }

                Spacer(Modifier.height(24.dp))
                ZapPayButton(text = "Generate New Code", onClick = { viewModel.generateQR() }, variant = com.zappay.app.ui.components.ButtonVariant.OUTLINE)
            } else {
                ErrorMessage(state.error ?: "Failed to load QR code", onRetry = { viewModel.generateQR() })
            }

            Spacer(Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Yellow100),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Your QR code is encrypted and expires automatically for security.",
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
