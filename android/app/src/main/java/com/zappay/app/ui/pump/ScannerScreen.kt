package com.zappay.app.ui.pump

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: PumpViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var scanMode by remember { mutableStateOf("qr") } // "qr" or "vehicle"
    var vehicleNumber by remember { mutableStateOf("") }

    // Purchase state
    var showPurchaseSheet by remember { mutableStateOf(false) }
    var fuelType by remember { mutableStateOf("Petrol") }
    var fuelQuantity by remember { mutableStateOf("") }
    var fuelRate by remember { mutableStateOf("") }

    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(state.purchaseSuccess) {
        if (state.purchaseSuccess) {
            showPurchaseSheet = false
            viewModel.clearPurchaseSuccess()
            viewModel.clearScannedCustomer()
            viewModel.clearLookedUpVehicle()
            vehicleNumber = ""
            fuelQuantity = ""
            fuelRate = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan & Pay", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            // Error
            if (state.error != null && !showPurchaseSheet) {
                Card(colors = CardDefaults.cardColors(containerColor = Red100), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(state.error!!, color = Red500, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                    if (state.error != null) {
                        TextButton(onClick = { viewModel.clearError() }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("Dismiss", color = Purple500, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Mode toggle
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = scanMode == "qr", onClick = { scanMode = "qr" }, label = { Text("QR Code") })
                FilterChip(selected = scanMode == "vehicle", onClick = { scanMode = "vehicle" }, label = { Text("Vehicle Number") })
            }

            when (scanMode) {
                "qr" -> {
                    // Camera preview with QR scanning
                    if (hasCameraPermission) {
                        Box(Modifier.fillMaxWidth().height(300.dp).background(Gray100)) {
                            QRCodeScanner(modifier = Modifier.fillMaxSize()) { qrData ->
                                viewModel.validateQR(qrData)
                            }
                        }
                    } else {
                        Card(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)) {
                            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text("Camera permission required", color = Gray500)
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = { /* request permission */ }) { Text("Grant Permission", color = Purple500) }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Or enter QR data manually:", color = Gray500, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = vehicleNumber,
                            onValueChange = { vehicleNumber = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("QR code data", fontSize = 13.sp) },
                            singleLine = true,
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { viewModel.validateQR(vehicleNumber) }, enabled = vehicleNumber.isNotEmpty()) {
                            Text("Validate")
                        }
                    }
                }
                "vehicle" -> {
                    Spacer(Modifier.height(12.dp))
                    ZapPayInput(value = vehicleNumber, onValueChange = { vehicleNumber = it }, label = "Vehicle Number", placeholder = "MH01AB1234", keyboardType = KeyboardType.Ascii)
                    Spacer(Modifier.height(12.dp))
                    ZapPayButton(text = "Lookup Vehicle", onClick = { viewModel.lookupVehicle(vehicleNumber) }, enabled = vehicleNumber.length >= 6 && !state.isLoading)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Scanned customer details
            val customer = state.scannedCustomer
            if (customer?.valid == true) {
                CustomerFoundCard(customer = customer, onPurchase = { showPurchaseSheet = true })
            }

            // Vehicle lookup result
            val vehicle = state.lookedUpVehicle
            if (vehicle?.found == true) {
                VehicleFoundCard(vehicle = vehicle, onPurchase = { showPurchaseSheet = true })
            }
        }
    }

    // Purchase bottom sheet
    if (showPurchaseSheet) {
        AlertDialog(
            onDismissRequest = { showPurchaseSheet = false },
            title = { Text("New Fuel Purchase", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("Fuel Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Petrol", "Diesel", "CNG").forEach { ft ->
                            FilterChip(selected = fuelType == ft, onClick = { fuelType = ft }, label = { Text(ft, fontSize = 13.sp) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = fuelQuantity, onValueChange = { fuelQuantity = it }, label = { Text("Quantity (L)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = fuelRate, onValueChange = { fuelRate = it }, label = { Text("Rate (₹/L)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    if (fuelQuantity.toDoubleOrNull() != null && fuelRate.toDoubleOrNull() != null) {
                        Spacer(Modifier.height(8.dp))
                        Text("Total: ₹ ${"%.2f".format(fuelQuantity.toDouble() * fuelRate.toDouble())}", fontWeight = FontWeight.Bold, color = Purple500)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = fuelQuantity.toDoubleOrNull() ?: 0.0
                        val rate = fuelRate.toDoubleOrNull() ?: 0.0
                        if (qty > 0 && rate > 0) {
                            val scanCustomer = state.scannedCustomer
                            val lookupVehicle = state.lookedUpVehicle
                            viewModel.processPurchase(
                                qrCode = scanCustomer?.let { vehicleNumber.ifEmpty { null } },
                                userId = lookupVehicle?.userId ?: scanCustomer?.userId,
                                fuelType = fuelType,
                                fuelQuantity = qty,
                                fuelRate = rate,
                            )
                        }
                    },
                    enabled = fuelQuantity.toDoubleOrNull() ?: 0.0 > 0 && fuelRate.toDoubleOrNull() ?: 0.0 > 0 && !state.isLoading,
                ) { Text("Process Payment") }
            },
            dismissButton = { TextButton(onClick = { showPurchaseSheet = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun QRCodeScanner(
    modifier: Modifier = Modifier,
    onCodeScanned: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val analyzer = remember { ImageAnalysis.Analyzer { imageProxy ->
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            onCodeScanned(value)
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    } }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer) }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun CustomerFoundCard(customer: com.zappay.app.data.remote.dto.ValidatedCustomerResponse, onPurchase: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Green100)) {
        Column(Modifier.padding(16.dp)) {
            Text("Customer Found", fontWeight = FontWeight.Bold, color = Green500)
            Spacer(Modifier.height(8.dp))
            Text("Name: ${customer.userName ?: "N/A"}", fontSize = 14.sp)
            Text("Phone: ${customer.userPhone ?: "N/A"}", fontSize = 14.sp)
            Text("Balance: ₹ ${"%.2f".format(customer.walletBalance ?: 0.0)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            customer.vehicleNumber?.let { Text("Vehicle: $it", fontSize = 14.sp) }
            Spacer(Modifier.height(12.dp))
            ZapPayButton(text = "Create Purchase", onClick = onPurchase)
        }
    }
}

@Composable
private fun VehicleFoundCard(vehicle: com.zappay.app.data.remote.dto.VehicleLookupResponse, onPurchase: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Blue100)) {
        Column(Modifier.padding(16.dp)) {
            Text("Vehicle Found", fontWeight = FontWeight.Bold, color = Blue500)
            Spacer(Modifier.height(8.dp))
            Text("Owner: ${vehicle.userName ?: "N/A"}", fontSize = 14.sp)
            Text("Phone: ${vehicle.userPhone ?: "N/A"}", fontSize = 14.sp)
            Text("Balance: ₹ ${"%.2f".format(vehicle.walletBalance ?: 0.0)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            vehicle.vehicleNumber?.let { Text("Vehicle: $it", fontSize = 14.sp) }
            Spacer(Modifier.height(12.dp))
            ZapPayButton(text = "Create Purchase", onClick = onPurchase)
        }
    }
}
