package com.zappay.app.ui.pump

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.PumpRegisterRequest
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*
import com.zappay.app.util.LocationHelper

enum class StationType(val label: String, val icon: String) {
    PETROL("Petrol Pump", "⛽"),
    EV_SUPERCHARGER("EV Supercharger", "⚡"),
    HYBRID("Multi-Energy Hub", "🔋")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPumpScreen(
    viewModel: PumpViewModel,
    locationHelper: LocationHelper,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var stationType by remember { mutableStateOf(StationType.HYBRID) }
    var selectedBrand by remember { mutableStateOf("BPCL") }
    var pumpName by remember { mutableStateOf("BPCL & EV Hub Bilaspur Junction") }
    var ownerName by remember { mutableStateOf("Parth Dev (Dealer)") }
    var licenseNumber by remember { mutableStateOf("RO-BPCL-495001") }
    var address by remember { mutableStateOf("NH-49, Bilaspur Highway Junction") }
    var city by remember { mutableStateOf("Bilaspur") }
    var state_ by remember { mutableStateOf("Chhattisgarh") }
    var pincode by remember { mutableStateOf("495001") }
    var phoneNumber by remember { mutableStateOf("9876543210") }
    var email by remember { mutableStateOf("dealer.bilaspur@zappay.in") }

    // Rates & Bays
    var petrolRate by remember { mutableStateOf("100.50") }
    var dieselRate by remember { mutableStateOf("89.70") }
    var evTariffKwh by remember { mutableStateOf("18.50") }
    var evBaysCount by remember { mutableStateOf("4 Bays (CCS2 60/120kW)") }

    // Merchant Bank Settlement
    var settlementUpiId by remember { mutableStateOf("dealer.bilaspur@icici") }
    var bankAccountNumber by remember { mutableStateOf("918273645012") }
    var bankIfsc by remember { mutableStateOf("ICIC0001824") }
    var attendantPin by remember { mutableStateOf("1234") }

    var latitude by remember { mutableStateOf("22.0797") }
    var longitude by remember { mutableStateOf("82.1409") }
    var locationLoading by remember { mutableStateOf(false) }

    val brands = listOf("BPCL", "Jio-bp", "Tata Power", "IOCL", "HPCL", "Shell", "Statiq")

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            locationHelper.getFreshLocation { loc ->
                loc?.let {
                    latitude = "%.6f".format(it.latitude)
                    longitude = "%.6f".format(it.longitude)
                }
                locationLoading = false
            }
        } else {
            locationLoading = false
        }
    }

    LaunchedEffect(state.registrationSuccess) {
        if (state.registrationSuccess) {
            viewModel.clearRegistrationSuccess()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Station Onboarding & Setup", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Partner Forecourt & Supercharging Portal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // Quick demo auto-fill for instant onboarding
                        pumpName = "Jio-bp Pulse & Fuel Hub"
                        licenseNumber = "RO-JIO-2024"
                        address = "Ring Road 2, Bilaspur"
                        settlementUpiId = "jiobp.bilaspur@hdfcbank"
                    }) {
                        Text("Auto-Fill", color = Purple500, fontWeight = FontWeight.SemiBold)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // ── STEP 1: STATION CATEGORY PICKER ──────────────────────────────
            Text("STATION CATEGORY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary500, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StationType.values().forEach { type ->
                    val isSelected = stationType == type
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { stationType = type },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Primary500 else Neutral100
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(
                            Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(type.icon, fontSize = 24.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                type.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) White else Neutral700,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── NETWORK BRAND ────────────────────────────────────────────────
            Text("ENERGY BRAND / NETWORK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary500, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                brands.forEach { brand ->
                    val isSel = selectedBrand == brand
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedBrand = brand },
                        label = { Text(brand, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Purple500,
                            selectedLabelColor = White,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── BASIC DETAILS ────────────────────────────────────────────────
            SectionHeader("Station Identity & License")
            Spacer(Modifier.height(10.dp))
            ZapPayInput(value = pumpName, onValueChange = { pumpName = it }, label = "Station / RO Name", placeholder = "e.g. Bilaspur Energy Station")
            Spacer(Modifier.height(10.dp))
            ZapPayInput(value = licenseNumber, onValueChange = { licenseNumber = it }, label = "Dealer RO / License Code", placeholder = "e.g. RO-BPCL-495001")
            Spacer(Modifier.height(10.dp))
            ZapPayInput(value = ownerName, onValueChange = { ownerName = it }, label = "Dealer / Station Owner Name", placeholder = "Parth Dev")

            Spacer(Modifier.height(24.dp))

            // ── FORECOURT BAYS & TARIFFS ──────────────────────────────────────
            SectionHeader("Forecourt Dispenser & Bay Rates")
            Spacer(Modifier.height(10.dp))

            if (stationType == StationType.PETROL || stationType == StationType.HYBRID) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ZapPayInput(
                        value = petrolRate,
                        onValueChange = { petrolRate = it },
                        label = "Petrol Rate (₹/L)",
                        placeholder = "100.50",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal,
                    )
                    ZapPayInput(
                        value = dieselRate,
                        onValueChange = { dieselRate = it },
                        label = "Diesel Rate (₹/L)",
                        placeholder = "89.70",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal,
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            if (stationType == StationType.EV_SUPERCHARGER || stationType == StationType.HYBRID) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ZapPayInput(
                        value = evTariffKwh,
                        onValueChange = { evTariffKwh = it },
                        label = "EV Tariff (₹/kWh)",
                        placeholder = "18.50",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal,
                    )
                    ZapPayInput(
                        value = evBaysCount,
                        onValueChange = { evBaysCount = it },
                        label = "EV Charger Bays",
                        placeholder = "4 Bays (CCS2)",
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(16.dp))

            // ── MERCHANT SETTLEMENT & BANKING ────────────────────────────────
            SectionHeader("Instant Payouts & Payout Banking")
            Spacer(Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Green100),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Green500)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Zero Dealer Surcharge Guarantee", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Green500)
                        Text("Every vehicle auto-debit settles into your bank instantly T+0.", fontSize = 11.sp, color = Gray700)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            ZapPayInput(value = settlementUpiId, onValueChange = { settlementUpiId = it }, label = "Settlement UPI VPA", placeholder = "dealer@icici")
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ZapPayInput(value = bankAccountNumber, onValueChange = { bankAccountNumber = it }, label = "Bank Account No.", placeholder = "918273645012", modifier = Modifier.weight(1.2f), keyboardType = KeyboardType.Number)
                ZapPayInput(value = bankIfsc, onValueChange = { bankIfsc = it }, label = "IFSC Code", placeholder = "ICIC0001824", modifier = Modifier.weight(0.8f))
            }

            Spacer(Modifier.height(24.dp))

            // ── ATTENDANT SHIFT CREDENTIALS ──────────────────────────────────
            SectionHeader("Attendant Forecourt Access")
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ZapPayInput(value = phoneNumber, onValueChange = { phoneNumber = it }, label = "Attendant Phone", placeholder = "9876543210", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Phone)
                ZapPayInput(value = attendantPin, onValueChange = { attendantPin = it }, label = "Forecourt PIN", placeholder = "1234", modifier = Modifier.weight(0.7f), keyboardType = KeyboardType.NumberPassword)
            }

            Spacer(Modifier.height(24.dp))

            // ── LOCATION & GPS ───────────────────────────────────────────────
            SectionHeader("Station Location & GPS")
            Spacer(Modifier.height(10.dp))
            ZapPayInput(value = address, onValueChange = { address = it }, label = "Station Address", placeholder = "Street address")
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ZapPayInput(value = city, onValueChange = { city = it }, label = "City", placeholder = "Bilaspur", modifier = Modifier.weight(1f))
                ZapPayInput(value = state_, onValueChange = { state_ = it }, label = "State", placeholder = "Chhattisgarh", modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            ZapPayInput(value = pincode, onValueChange = { pincode = it }, label = "Pincode", placeholder = "495001", keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(10.dp))

            ZapPayButton(
                text = if (locationLoading) "Acquiring Station GPS..." else "📍 Auto-Detect GPS Coordinates",
                onClick = {
                    locationLoading = true
                    if (locationHelper.hasLocationPermission()) {
                        locationHelper.getFreshLocation { loc ->
                            loc?.let {
                                latitude = "%.6f".format(it.latitude)
                                longitude = "%.6f".format(it.longitude)
                            }
                            locationLoading = false
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                },
                isLoading = locationLoading,
                variant = com.zappay.app.ui.components.ButtonVariant.OUTLINE,
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ZapPayInput(value = latitude, onValueChange = { latitude = it }, label = "Latitude", placeholder = "22.0797", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                ZapPayInput(value = longitude, onValueChange = { longitude = it }, label = "Longitude", placeholder = "82.1409", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            }

            Spacer(Modifier.height(30.dp))

            if (state.error != null) {
                ErrorMessage(state.error!!)
                Spacer(Modifier.height(12.dp))
            }

            ZapPayButton(
                text = if (state.isLoading) "Registering Station..." else "🚀 Complete Onboarding & Open Forecourt",
                onClick = {
                    val fuels = when (stationType) {
                        StationType.PETROL -> listOf("PETROL", "DIESEL")
                        StationType.EV_SUPERCHARGER -> listOf("ELECTRIC (CCS2 / Type 2)")
                        StationType.HYBRID -> listOf("PETROL", "DIESEL", "ELECTRIC (CCS2 / Type 2)")
                    }
                    viewModel.registerPump(
                        PumpRegisterRequest(
                            pumpName = pumpName,
                            ownerName = ownerName,
                            licenseNumber = licenseNumber,
                            address = address,
                            city = city,
                            state = state_,
                            pincode = pincode,
                            phoneNumber = phoneNumber,
                            email = email.ifEmpty { null },
                            latitude = latitude.toDoubleOrNull(),
                            longitude = longitude.toDoubleOrNull(),
                            fuelTypes = fuels,
                            dailyFuelCapacity = 50000.0,
                        )
                    )
                },
                isLoading = state.isLoading,
                enabled = pumpName.isNotEmpty() && ownerName.isNotEmpty() && licenseNumber.isNotEmpty()
                    && address.isNotEmpty() && city.isNotEmpty() && state_.isNotEmpty()
                    && pincode.length >= 6 && phoneNumber.length >= 10,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
