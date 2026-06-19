package com.zappay.app.ui.pump

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.mlkit.vision.common.InputImage
import com.zappay.app.data.remote.dto.PumpSettingsData
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
    var scanMode by remember { mutableStateOf("qr") }
    var manualInput by remember { mutableStateOf("") }

    var scannedQrData by remember { mutableStateOf<String?>(null) }
    var showPurchase by remember { mutableStateOf(false) }
    var fuelType by remember { mutableStateOf("Petrol") }
    var fuelQuantity by remember { mutableStateOf("") }
    var fuelRate by remember { mutableStateOf("") }
    var cameraActive by remember { mutableStateOf(true) }
    val fuelOptions = remember(state.settings) { parseFuelOptions(state.settings) }
    val selectedFuelRate = fuelOptions.firstOrNull { it.name == fuelType }?.rate
    val rateLockedBySettings = selectedFuelRate != null && selectedFuelRate > 0.0
    val pumpIsOpen = state.settings?.isOpen ?: true

    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    LaunchedEffect(state.pumpId) {
        if (state.pumpId != null) {
            viewModel.loadSettings()
        }
    }

    LaunchedEffect(fuelOptions) {
        if (fuelOptions.isNotEmpty() && fuelOptions.none { it.name == fuelType }) {
            fuelType = fuelOptions.first().name
        }
    }

    LaunchedEffect(fuelType, fuelOptions) {
        val configuredRate = fuelOptions.firstOrNull { it.name == fuelType }?.rate
        if (configuredRate != null && configuredRate > 0.0) {
            fuelRate = formatRate(configuredRate)
        }
    }

    LaunchedEffect(state.scannedCustomer) {
        val customer = state.scannedCustomer
        if (customer?.valid == true && scannedQrData != null) {
            cameraActive = false
            showPurchase = true
        }
    }

    LaunchedEffect(state.purchaseSuccess) {
        if (state.purchaseSuccess) {
            showPurchase = false
            scannedQrData = null
            cameraActive = true
            scanMode = "qr"
            manualInput = ""
            fuelQuantity = ""
            viewModel.clearPurchaseSuccess()
            viewModel.clearScannedCustomer()
            viewModel.clearLookedUpVehicle()
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
            if (state.error != null && !showPurchase) {
                Card(colors = CardDefaults.cardColors(containerColor = Red100), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(state.error!!, color = Red500, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                    TextButton(onClick = {
                        scannedQrData = null
                        cameraActive = scanMode == "qr"
                        viewModel.clearError()
                    }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Dismiss", color = Purple500, fontSize = 12.sp)
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = scanMode == "qr", onClick = {
                    scanMode = "qr"
                    manualInput = ""
                    cameraActive = true
                    scannedQrData = null
                    viewModel.clearScannedCustomer()
                    viewModel.clearLookedUpVehicle()
                }, label = { Text("QR Code") })
                FilterChip(selected = scanMode == "vehicle", onClick = {
                    scanMode = "vehicle"
                    manualInput = ""
                    cameraActive = false
                    scannedQrData = null
                    viewModel.clearScannedCustomer()
                    viewModel.clearLookedUpVehicle()
                }, label = { Text("Vehicle Number") })
            }

            if (scanMode == "qr") {
                if (cameraActive) {
                    if (hasCameraPermission) {
                        Box(Modifier.fillMaxWidth().height(300.dp).background(Gray100)) {
                            QRCodeScanner(modifier = Modifier.fillMaxSize()) { qrData ->
                                if (!state.isLoading && scannedQrData == null) {
                                    scannedQrData = qrData
                                    viewModel.validateQR(qrData)
                                }
                            }
                        }
                    } else {
                        Card(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)) {
                            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text("Camera permission required", color = Gray500)
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Grant Permission", color = Purple500) }
                            }
                        }
                    }
                }

                if (scannedQrData == null) {
                    Spacer(Modifier.height(8.dp))
                    Text("Or enter QR data manually:", color = Gray500, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = manualInput,
                            onValueChange = { manualInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("QR code data", fontSize = 13.sp) },
                            singleLine = true,
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            if (manualInput.isNotBlank()) {
                                scannedQrData = manualInput
                                viewModel.validateQR(manualInput)
                            }
                        }, enabled = manualInput.isNotEmpty() && !state.isLoading) {
                            Text("Validate")
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                ZapPayInput(value = manualInput, onValueChange = { manualInput = it }, label = "Vehicle Number", placeholder = "MH01AB1234", keyboardType = KeyboardType.Ascii)
                Spacer(Modifier.height(12.dp))
                ZapPayButton(text = "Lookup Vehicle", onClick = {
                    scannedQrData = null
                    viewModel.lookupVehicle(manualInput)
                }, enabled = manualInput.length >= 6 && !state.isLoading)
            }

            val vehicle = state.lookedUpVehicle
            if (vehicle?.found == true && !showPurchase) {
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Blue100)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Vehicle Found", fontWeight = FontWeight.Bold, color = Blue500)
                        Spacer(Modifier.height(8.dp))
                        Text("Owner: ${vehicle.userName ?: "N/A"}", fontSize = 14.sp)
                        Text("Phone: ${vehicle.userPhone ?: "N/A"}", fontSize = 14.sp)
                        Text("Balance: ₹ ${"%.2f".format(vehicle.walletBalance ?: 0.0)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        vehicle.vehicleNumber?.let { Text("Vehicle: $it", fontSize = 14.sp) }
                        Spacer(Modifier.height(12.dp))
                        ZapPayButton(text = "Create Purchase", onClick = { showPurchase = true })
                    }
                }
            }
        }
    }

    if (showPurchase) {
        val customer = state.scannedCustomer
        val lookupVehicle = state.lookedUpVehicle

        AlertDialog(
            onDismissRequest = {
                showPurchase = false
                cameraActive = true
                scannedQrData = null
                viewModel.clearScannedCustomer()
                viewModel.clearLookedUpVehicle()
            },
            title = { Text("New Fuel Purchase", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    if (customer?.valid == true) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Green100),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Customer", fontWeight = FontWeight.Bold, color = Green500, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Name: ${customer.userName ?: "N/A"}", fontSize = 14.sp)
                                Text("Phone: ${customer.userPhone ?: "N/A"}", fontSize = 14.sp)
                                Text("Balance: ₹ ${"%.2f".format(customer.walletBalance ?: 0.0)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                customer.vehicleNumber?.let { Text("Vehicle: $it", fontSize = 14.sp) }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    } else if (lookupVehicle?.found == true) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Blue100),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Vehicle Owner", fontWeight = FontWeight.Bold, color = Blue500, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Owner: ${lookupVehicle.userName ?: "N/A"}", fontSize = 14.sp)
                                Text("Phone: ${lookupVehicle.userPhone ?: "N/A"}", fontSize = 14.sp)
                                Text("Balance: ₹ ${"%.2f".format(lookupVehicle.walletBalance ?: 0.0)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                lookupVehicle.vehicleNumber?.let { Text("Vehicle: $it", fontSize = 14.sp) }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Text("Fuel Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fuelOptions.forEach { option ->
                            FilterChip(selected = fuelType == option.name, onClick = { fuelType = option.name }, label = { Text(option.name, fontSize = 13.sp) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    if (!pumpIsOpen) {
                        Card(colors = CardDefaults.cardColors(containerColor = Yellow100), modifier = Modifier.fillMaxWidth()) {
                            Text("Pump is marked closed in settings.", color = Gray700, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = fuelQuantity,
                        onValueChange = { fuelQuantity = it },
                        label = { Text("Quantity (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fuelRate,
                        onValueChange = { fuelRate = it },
                        label = { Text("Rate (₹/L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !rateLockedBySettings,
                        supportingText = if (rateLockedBySettings) {{ Text("Auto-set from pump settings", fontSize = 11.sp, color = Gray500) }} else {{ Text("No saved rate for this fuel. Enter rate manually.", fontSize = 11.sp, color = Gray500) }},
                    )

                    if (fuelQuantity.toDoubleOrNull() != null && fuelRate.toDoubleOrNull() != null) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Purple50),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "Total: ₹ ${"%.2f".format(fuelQuantity.toDouble() * fuelRate.toDouble())}",
                                fontWeight = FontWeight.Bold,
                                color = Purple500,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = fuelQuantity.toDoubleOrNull() ?: 0.0
                        val rate = fuelRate.toDoubleOrNull() ?: 0.0
                        if (qty > 0 && rate > 0) {
                            viewModel.processPurchase(
                                qrCode = scannedQrData,
                                userId = lookupVehicle?.userId ?: customer?.userId,
                                fuelType = fuelType,
                                fuelQuantity = qty,
                                fuelRate = rate,
                            )
                        }
                    },
                    enabled = (fuelQuantity.toDoubleOrNull() ?: 0.0) > 0 && (fuelRate.toDoubleOrNull() ?: 0.0) > 0 && pumpIsOpen && !state.isLoading,
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Process Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPurchase = false
                    cameraActive = true
                    scannedQrData = null
                    viewModel.clearScannedCustomer()
                    viewModel.clearLookedUpVehicle()
                }) { Text("Cancel") }
            },
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

private data class FuelOption(
    val name: String,
    val rate: Double?,
)

private fun parseFuelOptions(settings: PumpSettingsData?): List<FuelOption> {
    val types = settings?.fuelTypes
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.takeIf { it.isNotEmpty() }
        ?: listOf("Petrol", "Diesel", "CNG")
    val rates = settings?.fuelRates
        ?.split(",")
        ?.map { it.trim().toDoubleOrNull() }
        ?: listOf(104.50, 92.30, 78.00)

    return types.mapIndexed { index, type ->
        FuelOption(name = type, rate = rates.getOrNull(index))
    }
}

private fun formatRate(rate: Double): String {
    return "%.2f".format(rate)
}
