package com.zappay.app.ui.pump

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zappay.app.data.remote.dto.ScanVehicleApiResponse
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.Executors

enum class ForecourtStep {
    SCAN_VEHICLE_QR,
    SCAN_DISPENSER_METER,
    CONFIRM_AND_DEBIT,
    RECEIPT_SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: PumpViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var currentStep by remember { mutableStateOf(ForecourtStep.SCAN_VEHICLE_QR) }
    var fuelRate by remember { mutableStateOf("100.50") }
    var scannedLitres by remember { mutableStateOf("5.00") }
    var scannedAmount by remember { mutableStateOf("502.50") }
    var scannedDensity by remember { mutableStateOf("742.5") }
    var isManualOverride by remember { mutableStateOf(false) }

    // Android TextToSpeech for Audio Soundbox Confirmation
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("hi", "IN")
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // Camera permission check & launcher
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Adapt rates when vehicle fuel lock is detected
    val vehicle = state.forecourtVerifiedVehicle
    val isElectric = vehicle?.permittedFuelType?.contains("ELECTRIC", ignoreCase = true) == true ||
        vehicle?.permittedFuelType?.contains("CCS2", ignoreCase = true) == true ||
        vehicle?.permittedFuelType?.contains("EV", ignoreCase = true) == true

    LaunchedEffect(vehicle) {
        if (vehicle != null) {
            if (isElectric) {
                fuelRate = "18.50"
                scannedLitres = "10.00" // 10 kWh
                scannedAmount = "185.00"
            } else if (vehicle.permittedFuelType?.contains("DIESEL", ignoreCase = true) == true) {
                fuelRate = "89.70"
                scannedLitres = "10.00"
                scannedAmount = "897.00"
            } else {
                fuelRate = "100.50"
                scannedLitres = "5.00"
                scannedAmount = "502.50"
            }
        }
    }

    // Handle auto-debit success
    LaunchedEffect(state.purchaseSuccess, state.debitResult) {
        if (state.purchaseSuccess && state.debitResult != null) {
            currentStep = ForecourtStep.RECEIPT_SUCCESS
            // Speak Soundbox audio confirmation!
            val speechText = state.soundboxVoiceMessage ?: "ZapPay Praapt: 502 Rupees"
            tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "ZAPPAY_TTS")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Bay Auto-Scanner", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Green500.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text("CAMERA LIVE", color = Green500, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Text("Bilaspur Hub • Auto-Debit & TTS Soundbox", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.resetForecourtFlow()
                        currentStep = ForecourtStep.SCAN_VEHICLE_QR
                    }) {
                        Text("Reset", color = Purple500, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Step Progress Indicator
            StepIndicatorRow(currentStep = currentStep)

            // Error banner
            if (state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Red100),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Red500)
                        Spacer(Modifier.width(8.dp))
                        Text(state.error!!, color = Red500, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    }
                }
            }

            when (currentStep) {
                // ── STEP 1: VEHICLE BAY QR AUTOSCAN ─────────────────────────
                ForecourtStep.SCAN_VEHICLE_QR -> {
                    VehicleQRScanSection(
                        hasCameraPermission = hasCameraPermission,
                        verifiedVehicle = state.forecourtVerifiedVehicle,
                        isLoading = state.isLoading,
                        isElectric = isElectric,
                        fuelRate = fuelRate,
                        scannedAmount = scannedAmount,
                        onAmountPreset = { amt, units ->
                            scannedAmount = amt
                            scannedLitres = units
                        },
                        onQRDetected = { token ->
                            viewModel.scanForecourtVehicle(token)
                        },
                        onProceedToMeter = {
                            currentStep = ForecourtStep.SCAN_DISPENSER_METER
                        },
                        onDirectExecuteDebit = {
                            val qrToken = vehicle?.qrToken ?: "zp_cg_4582"
                            val fuel = vehicle?.permittedFuelType ?: "PETROL"
                            val l = scannedLitres.toDoubleOrNull() ?: 5.0
                            val r = fuelRate.toDoubleOrNull() ?: 100.50
                            val amt = scannedAmount.toDoubleOrNull() ?: 502.50
                            val d = scannedDensity.toDoubleOrNull() ?: 742.5
                            viewModel.executeForecourtDebit(
                                qrToken = qrToken,
                                fuelDispensed = fuel,
                                volumeLitres = l,
                                fuelRate = r,
                                amount = amt,
                                density = d,
                                manualOverride = false,
                            )
                        },
                    )
                }

                // ── STEP 2: AI DISPENSER METER OCR ──────────────────────────
                ForecourtStep.SCAN_DISPENSER_METER -> {
                    MeterOCRSection(
                        hasCameraPermission = hasCameraPermission,
                        vehicleVRN = vehicle?.vrnNumber ?: "CG-10-AB-4582",
                        permittedFuel = vehicle?.permittedFuelType ?: "PETROL",
                        isElectric = isElectric,
                        fuelRate = fuelRate,
                        scannedLitres = scannedLitres,
                        scannedAmount = scannedAmount,
                        scannedDensity = scannedDensity,
                        isManual = isManualOverride,
                        onRateChange = { fuelRate = it },
                        onLitresChange = { scannedLitres = it },
                        onAmountChange = { scannedAmount = it },
                        onDensityChange = { scannedDensity = it },
                        onToggleManual = { isManualOverride = !isManualOverride },
                        onMeterReadingDetected = { units, amount, density ->
                            scannedLitres = "%.2f".format(units)
                            scannedAmount = "%.2f".format(amount)
                            scannedDensity = "%.1f".format(density)
                        },
                        onProceedToDebit = {
                            currentStep = ForecourtStep.CONFIRM_AND_DEBIT
                        },
                    )
                }

                // ── STEP 3: CONFIRM & EXECUTE DEBIT ─────────────────────────
                ForecourtStep.CONFIRM_AND_DEBIT -> {
                    ConfirmDebitSection(
                        vehicle = vehicle,
                        isElectric = isElectric,
                        fuelRate = fuelRate.toDoubleOrNull() ?: 100.50,
                        volumeLitres = scannedLitres.toDoubleOrNull() ?: 5.0,
                        amount = scannedAmount.toDoubleOrNull() ?: 502.50,
                        density = scannedDensity.toDoubleOrNull() ?: 742.5,
                        isManual = isManualOverride,
                        isLoading = state.isLoading,
                        onConfirmDebit = {
                            val qrToken = vehicle?.qrToken ?: "zp_cg_4582"
                            val fuel = vehicle?.permittedFuelType ?: "PETROL"
                            val l = scannedLitres.toDoubleOrNull() ?: 5.0
                            val r = fuelRate.toDoubleOrNull() ?: 100.50
                            val amt = scannedAmount.toDoubleOrNull() ?: 502.50
                            val d = scannedDensity.toDoubleOrNull() ?: 742.5
                            viewModel.executeForecourtDebit(
                                qrToken = qrToken,
                                fuelDispensed = fuel,
                                volumeLitres = l,
                                fuelRate = r,
                                amount = amt,
                                density = d,
                                manualOverride = isManualOverride,
                            )
                        },
                        onBackToMeter = {
                            currentStep = ForecourtStep.SCAN_DISPENSER_METER
                        },
                    )
                }

                // ── STEP 4: DIGITAL RECEIPT & SOUNDBOX ANNOUNCEMENT ─────────
                ForecourtStep.RECEIPT_SUCCESS -> {
                    ReceiptSuccessSection(
                        debitResult = state.debitResult,
                        soundboxVoiceMessage = state.soundboxVoiceMessage,
                        onNextVehicle = {
                            viewModel.resetForecourtFlow()
                            currentStep = ForecourtStep.SCAN_VEHICLE_QR
                        },
                        onReplayAudio = {
                            val speechText = state.soundboxVoiceMessage ?: "ZapPay Praapt"
                            tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "ZAPPAY_REPLAY")
                        },
                    )
                }
            }
        }
    }
}

// ── STEP INDICATOR ──────────────────────────────────────────────────────────

@Composable
private fun StepIndicatorRow(currentStep: ForecourtStep) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepDot("1. Scan Bay", currentStep >= ForecourtStep.SCAN_VEHICLE_QR)
        StepLine(currentStep >= ForecourtStep.SCAN_DISPENSER_METER)
        StepDot("2. Meter OCR", currentStep >= ForecourtStep.SCAN_DISPENSER_METER)
        StepLine(currentStep >= ForecourtStep.CONFIRM_AND_DEBIT)
        StepDot("3. Debit", currentStep >= ForecourtStep.CONFIRM_AND_DEBIT)
        StepLine(currentStep >= ForecourtStep.RECEIPT_SUCCESS)
        StepDot("4. Settled", currentStep == ForecourtStep.RECEIPT_SUCCESS)
    }
}

@Composable
private fun StepDot(label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (active) Purple500 else Gray300),
            contentAlignment = Alignment.Center,
        ) {
            if (active) {
                Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(14.dp))
            }
        }
        Text(
            label,
            fontSize = 9.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) Purple500 else Gray600,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun RowScope.StepLine(active: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .padding(horizontal = 4.dp)
            .background(if (active) Purple500 else Gray300)
    )
}

// ── STEP 1: VEHICLE BAY QR AUTOSCAN ─────────────────────────────────────────

@Composable
private fun VehicleQRScanSection(
    hasCameraPermission: Boolean,
    verifiedVehicle: ScanVehicleApiResponse?,
    isLoading: Boolean,
    isElectric: Boolean,
    fuelRate: String,
    scannedAmount: String,
    onAmountPreset: (String, String) -> Unit,
    onQRDetected: (String) -> Unit,
    onProceedToMeter: () -> Unit,
    onDirectExecuteDebit: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        if (verifiedVehicle == null) {
            // Live continuous bay camera mode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(Color(0xFF0F172A)),
            ) {
                if (hasCameraPermission) {
                    CameraBarcodePreview(onCodeScanned = onQRDetected)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Point camera at vehicle QR sticker", color = White, fontSize = 13.sp)
                    }
                }

                // Targeting Reticle with sweeping animated laser
                val infiniteTransition = rememberInfiniteTransition(label = "laser")
                val scanLineY by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "scanLineY",
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                ) {
                    // Framing box
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, if (isElectric) Color(0xFF00E5FF) else Green500, RoundedCornerShape(16.dp))
                    )

                    // Sweeping laser
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (240 * scanLineY).dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, if (isElectric) Color(0xFF00E5FF) else Green500, Color.Transparent)
                                )
                            )
                    )
                }

                // Bay status pill on top of camera
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Green500)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("BAY CAMERA AUTO-SCAN ACTIVE", color = White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Bottom help note
                Text(
                    "Auto-detects vehicle windshield sticker within 200ms",
                    color = White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            // Test vehicle simulator buttons
            Text(
                "BAY CAMERA SIMULATION (TEST INCOMING VEHICLE)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onQRDetected("zp_cg_4582") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⛽ Petrol Car", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("MH12AB1234", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedButton(
                    onClick = { onQRDetected("zappay://ev/station/1/user/1/charge_bay_1") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00B0FF)),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚡ Fast EV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                        Text("Nexon CCS2", fontSize = 10.sp, color = Color(0xFF80DEEA))
                    }
                }
                OutlinedButton(
                    onClick = { onQRDetected("zp_fleet_9011") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚚 Fleet", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("DL01TX9011", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            // ── VEHICLE VERIFIED & PRE-AUTHORIZED CARD ──────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isElectric) Color(0xFFE0F7FA) else Green100),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isElectric) Icons.Default.ElectricBolt else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isElectric) Color(0xFF00838F) else Green500,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "VEHICLE PRE-AUTHORIZED",
                                fontWeight = FontWeight.Black,
                                color = if (isElectric) Color(0xFF00838F) else Green500,
                                fontSize = 13.sp,
                            )
                        }
                        Text("Sub-15s Mode", fontSize = 11.sp, color = Neutral700, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(verifiedVehicle.vrnNumber ?: "MH-12-AB-1234", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Neutral900)
                    Text(verifiedVehicle.makeModel ?: "Tata Nexon (Parth Dev)", fontSize = 13.sp, color = Neutral700)

                    Spacer(Modifier.height(12.dp))

                    // CRITICAL FUEL / CONNECTOR LOCK BADGE
                    val fuel = verifiedVehicle.permittedFuelType ?: "PETROL"
                    val isDiesel = fuel.contains("DIESEL", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isElectric -> Color(0xFF00695C)
                                    isDiesel -> Color(0xFFD84315)
                                    else -> Color(0xFF2E7D32)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isElectric) Icons.Default.EvStation else Icons.Default.Lock,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isElectric) "CRITICAL CONNECTOR LOCK: $fuel ONLY" else "CRITICAL FUEL LOCK: $fuel ONLY",
                                color = White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("NPCI Mandate Limit: ₹${"%.0f".format(verifiedVehicle.maxPerTxnLimit ?: 2500.0)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Neutral800)
                        Text("Zero-Wait AutoPay Active", fontSize = 12.sp, color = Neutral700, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(14.dp))
                    Divider(color = Gray300)
                    Spacer(Modifier.height(14.dp))

                    // Quick Dispense Presets
                    Text(
                        if (isElectric) "SELECT CHARGING ENERGY PRESET" else "SELECT FUEL AMOUNT PRESET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray700,
                    )
                    Spacer(Modifier.height(8.dp))

                    if (isElectric) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PresetPill("5 kWh", "₹92.50", scannedAmount == "92.50") {
                                onAmountPreset("92.50", "5.00")
                            }
                            PresetPill("10 kWh", "₹185", scannedAmount == "185.00") {
                                onAmountPreset("185.00", "10.00")
                            }
                            PresetPill("20 kWh", "₹370", scannedAmount == "370.00") {
                                onAmountPreset("370.00", "20.00")
                            }
                            PresetPill("Smart 80%", "₹462", scannedAmount == "462.50") {
                                onAmountPreset("462.50", "25.00")
                            }
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PresetPill("₹200", "1.99L", scannedAmount == "200.00") {
                                onAmountPreset("200.00", "1.99")
                            }
                            PresetPill("₹500", "4.98L", scannedAmount == "502.50" || scannedAmount == "500.00") {
                                onAmountPreset("502.50", "5.00")
                            }
                            PresetPill("₹1,000", "9.95L", scannedAmount == "1000.00") {
                                onAmountPreset("1000.00", "9.95")
                            }
                            PresetPill("₹2,000", "19.9L", scannedAmount == "2000.00") {
                                onAmountPreset("2000.00", "19.90")
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // PRIMARY ACTION: ONE-TAP AUTO-DEBIT
                    ZapPayButton(
                        text = if (isLoading) "Executing Auto-Debit..." else "⚡ Authorize & Settle via AutoPay (₹$scannedAmount)",
                        onClick = onDirectExecuteDebit,
                        isLoading = isLoading,
                    )

                    Spacer(Modifier.height(8.dp))

                    // SECONDARY: AI DISPENSER METER OCR
                    OutlinedButton(
                        onClick = onProceedToMeter,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Verify Dispenser Meter OCR (Optional)", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.PresetPill(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Purple500 else White
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Gray300) else null,
    ) {
        Column(
            Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isSelected) White else Neutral800,
            )
            Text(
                subtitle,
                fontSize = 10.sp,
                color = if (isSelected) White.copy(alpha = 0.8f) else Gray600,
            )
        }
    }
}

// ── STEP 2: AI DISPENSER METER OCR ──────────────────────────────────────────

@Composable
private fun MeterOCRSection(
    hasCameraPermission: Boolean,
    vehicleVRN: String,
    permittedFuel: String,
    isElectric: Boolean,
    fuelRate: String,
    scannedLitres: String,
    scannedAmount: String,
    scannedDensity: String,
    isManual: Boolean,
    onRateChange: (String) -> Unit,
    onLitresChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDensityChange: (String) -> Unit,
    onToggleManual: () -> Unit,
    onMeterReadingDetected: (Double, Double, Double) -> Unit,
    onProceedToDebit: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            if (isElectric) "Step 2: EV Meter / Charger Display Match" else "Step 2: AI Dispenser Meter OCR (Meter-to-Bill Match)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
        Text(
            "Targeting $vehicleVRN • Fill $permittedFuel",
            fontSize = 12.sp,
            color = Gray600,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Live ML Kit Camera OCR
        if (!isManual && hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
            ) {
                CameraMeterTextAnalyzer(
                    fuelRate = fuelRate.toDoubleOrNull() ?: 100.50,
                    onReadingFound = onMeterReadingDetected,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(120.dp)
                        .align(Alignment.Center)
                        .border(2.dp, Purple500, RoundedCornerShape(12.dp)),
                )
                Text(
                    if (isElectric) "Point camera at EV Charger LED Screen" else "Point camera at Dispenser Volume / ₹ Display",
                    color = White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Detected readings card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Live Meter Readings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = onToggleManual) {
                        Text(if (isManual) "Switch to OCR" else "Manual Override", fontSize = 12.sp, color = Purple500)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scannedLitres,
                        onValueChange = onLitresChange,
                        label = { Text(if (isElectric) "Energy (kWh)" else "Volume (Litres)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = fuelRate,
                        onValueChange = onRateChange,
                        label = { Text(if (isElectric) "Tariff (₹/kWh)" else "Rate (₹/L)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scannedAmount,
                        onValueChange = onAmountChange,
                        label = { Text("Total Amount (₹)") },
                        modifier = Modifier.weight(1.2f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = if (isElectric) "96.8% Eff" else scannedDensity,
                        onValueChange = onDensityChange,
                        label = { Text(if (isElectric) "Grid Sync" else "Density (kg/m³)") },
                        modifier = Modifier.weight(1f),
                        enabled = !isElectric,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        ZapPayButton(
            text = "Proceed to Auto-Debit (₹$scannedAmount) →",
            onClick = onProceedToDebit,
            enabled = (scannedAmount.toDoubleOrNull() ?: 0.0) > 0.0,
        )
    }
}

// ── STEP 3: CONFIRM & EXECUTE DEBIT ─────────────────────────────────────────

@Composable
private fun ConfirmDebitSection(
    vehicle: ScanVehicleApiResponse?,
    isElectric: Boolean,
    fuelRate: Double,
    volumeLitres: Double,
    amount: Double,
    density: Double,
    isManual: Boolean,
    isLoading: Boolean,
    onConfirmDebit: () -> Unit,
    onBackToMeter: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Step 3: Instant UPI AutoPay Execution", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(14.dp))

            DetailRow("Vehicle VRN", vehicle?.vrnNumber ?: "CG-10-AB-4582")
            DetailRow(if (isElectric) "Connector Dispensed" else "Fuel Dispensed", vehicle?.permittedFuelType ?: "PETROL")
            DetailRow(if (isElectric) "Energy Consumed" else "Volume", if (isElectric) "$volumeLitres kWh" else "$volumeLitres Litres")
            DetailRow(if (isElectric) "Electricity Tariff" else "Rate", if (isElectric) "₹$fuelRate / kWh" else "₹$fuelRate / Litre")
            DetailRow(if (isElectric) "Charger Health" else "Fuel Density", if (isElectric) "96.8% DC Fast" else "$density kg/m³ (Pure)")
            DetailRow("Anti-Pilferage Proof", if (isManual) "Supervisor Overridden" else "AI OCR Verified (99.2%)")

            Divider(Modifier.padding(vertical = 12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Debit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("₹${"%.2f".format(amount)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Purple500)
            }

            Spacer(Modifier.height(20.dp))
            ZapPayButton(
                text = if (isLoading) "Processing Auto-Debit..." else "⚡ Execute Auto-Debit Now",
                onClick = onConfirmDebit,
                enabled = !isLoading,
            )

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBackToMeter, modifier = Modifier.fillMaxWidth()) {
                Text("Adjust Readings")
            }
        }
    }
}

// ── STEP 4: DIGITAL RECEIPT & SOUNDBOX ANNOUNCEMENT WITH AUTO-REDIRECT ───────

@Composable
private fun ReceiptSuccessSection(
    debitResult: com.zappay.app.data.remote.dto.ExecuteDebitApiResponse?,
    soundboxVoiceMessage: String?,
    onNextVehicle: () -> Unit,
    onReplayAudio: () -> Unit,
) {
    // 3-SECOND AUTOMATIC COUNTDOWN TIMER
    var remainingSeconds by remember { mutableStateOf(3) }
    var progress by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        val totalMs = 3000L
        val stepMs = 50L
        var elapsed = 0L
        while (elapsed < totalMs) {
            delay(stepMs)
            elapsed += stepMs
            progress = (totalMs - elapsed).toFloat() / totalMs
            remainingSeconds = ((totalMs - elapsed) / 1000 + 1).toInt().coerceAtLeast(0)
        }
        // Lane clears automatically!
        onNextVehicle()
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Green100),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(8.dp))
            Text("PAYMENT SETTLED", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Green500)
            Text("NPCI UPI AutoPay Zero-Wait Debit", fontSize = 12.sp, color = Gray700)

            Spacer(Modifier.height(14.dp))
            Text("₹${"%.2f".format(debitResult?.grossAmount ?: 502.50)}", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Neutral900)
            Text(debitResult?.vrnNumber ?: "CG-10-AB-4582", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Neutral800)

            Spacer(Modifier.height(14.dp))

            // Audio Soundbox Pill
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("🔊 Soundbox Announcement", fontSize = 11.sp, color = Neutral700)
                        Text(soundboxVoiceMessage ?: "ZapPay Praapt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Neutral900)
                    }
                    IconButton(onClick = onReplayAudio) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Replay Audio", tint = Purple500)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            DetailRow("Transaction ID", debitResult?.transactionId ?: "TXN_8CB8C2B4")
            DetailRow("NPCI RRN", debitResult?.npciRrn ?: "632214590399")
            DetailRow("Units Dispensed", "${debitResult?.volumeLitres ?: 5.0} Units")
            DetailRow("Settlement", "T+0 Instant Credit to Merchant")

            Spacer(Modifier.height(18.dp))

            // ── AUTOMATED 3-SECOND LANE CLEAR COUNTDOWN ────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(White)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Green500,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Lane clears in ${remainingSeconds}s...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green500,
                        )
                    }
                    Text("Auto-Reset", fontSize = 10.sp, color = Neutral700)
                }

                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = Green500,
                    trackColor = Gray300,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Camera will automatically resume scanning for next vehicle",
                    fontSize = 10.sp,
                    color = Neutral700,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(14.dp))

            ZapPayButton(
                text = "Next Vehicle Now (${remainingSeconds}s) →",
                onClick = onNextVehicle,
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Neutral700, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Neutral900)
    }
}

// ── ML KIT CAMERA PREVIEWS ──────────────────────────────────────────────────

@Composable
private fun CameraBarcodePreview(
    onCodeScanned: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val analyzer = remember {
        ImageAnalysis.Analyzer { imageProxy ->
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
        }
    }

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
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun CameraMeterTextAnalyzer(
    fuelRate: Double,
    onReadingFound: (Double, Double, Double) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val analyzer = remember {
        ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        // Extract numbers from meter display
                        val numbers = Regex("""\d+(\.\d+)?""").findAll(text).mapNotNull { it.value.toDoubleOrNull() }.toList()
                        if (numbers.size >= 2) {
                            val sorted = numbers.sorted()
                            val litres = sorted.firstOrNull { it in 1.0..100.0 } ?: 5.0
                            val amount = sorted.firstOrNull { it > litres && it in 50.0..15000.0 } ?: (litres * fuelRate)
                            onReadingFound(litres, amount, 742.5)
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } else {
                imageProxy.close()
            }
        }
    }

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
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}
