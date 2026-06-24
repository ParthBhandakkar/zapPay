package com.zappay.app.ui.pump

import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpSettingsScreen(
    viewModel: PumpViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    var fuelTypes by remember { mutableStateOf("Petrol,Diesel,CNG") }
    var fuelRates by remember { mutableStateOf("104.50,92.30,78.00") }
    var isOpen by remember { mutableStateOf(true) }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadSettings() }
    LaunchedEffect(state.settings) {
        state.settings?.let { settings ->
            fuelTypes = settings.fuelTypes
            fuelRates = settings.fuelRates
            isOpen = settings.isOpen
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            ZapPayTopBar(
                title = "Pump Settings",
                onBack = onBack,
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            // Gradient background header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Teal700, Teal500),
                            start = Offset.Zero,
                            end = Offset(1000f, 1000f),
                        )
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState),
            ) {
                Spacer(Modifier.height(40.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(24.dp),
                ) {
                    Spacer(Modifier.height(8.dp))
                    Text("Fuel Configuration", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(4.dp))
                    Text("Manage fuel types, rates & pump availability", fontSize = 14.sp, color = Neutral500)
                    Spacer(Modifier.height(24.dp))

                    // Fuel types
                    SettingsTextField(
                        icon = Icons.Outlined.LocalGasStation,
                        label = "Fuel Types (comma separated)",
                        value = fuelTypes,
                        onValueChange = { fuelTypes = it; localError = null; viewModel.clearSettingsSaved() },
                    )
                    Spacer(Modifier.height(20.dp))

                    // Fuel rates
                    SettingsTextField(
                        icon = Icons.Outlined.AttachMoney,
                        label = "Fuel Rates (comma separated)",
                        value = fuelRates,
                        onValueChange = { fuelRates = it; localError = null; viewModel.clearSettingsSaved() },
                        keyboardType = KeyboardType.Decimal,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Example: Petrol,Diesel,CNG and 104.50,92.30,78.00", fontSize = 12.sp, color = Neutral500)
                    Spacer(Modifier.height(24.dp))

                    // Pump status toggle
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isOpen) Success50 else Warning50),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isOpen) Success500.copy(alpha = 0.15f) else Warning500.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    if (isOpen) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isOpen) Success500 else Warning500,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Pump Status",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    if (isOpen) "Customers can scan & pay" else "No transactions accepted",
                                    fontSize = 12.sp,
                                    color = Neutral500,
                                )
                            }
                            Switch(
                                checked = isOpen,
                                onCheckedChange = { isOpen = it; viewModel.clearSettingsSaved() },
                                colors = SwitchDefaults.colors(checkedTrackColor = Teal500),
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Success/Error messages
                    if (state.settingsSaved) {
                        Card(colors = CardDefaults.cardColors(containerColor = Success50), shape = RoundedCornerShape(12.dp)) {
                            Row(
                                Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Success500, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Settings saved. Scanner rates will update automatically.", color = Success700, fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    if (localError != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = Danger50), shape = RoundedCornerShape(12.dp)) {
                            Text(localError!!, color = Danger500, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    if (state.error != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = Danger50), shape = RoundedCornerShape(12.dp)) {
                            Text(state.error!!, color = Danger500, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    ZapPayButton(
                        text = if (state.isLoading) "Saving..." else "Save Settings",
                        onClick = {
                            val types = splitSettingsCsv(fuelTypes)
                            val rates = splitSettingsCsv(fuelRates)
                            val invalidRate = rates.firstOrNull { (it.toDoubleOrNull() ?: 0.0) <= 0.0 }
                            localError = when {
                                types.isEmpty() -> "Add at least one fuel type."
                                rates.size != types.size -> "Add one rate for each fuel type."
                                invalidRate != null -> "Rates must be greater than zero."
                                else -> null
                            }
                            if (localError == null) {
                                viewModel.saveSettings(types.joinToString(","), rates.joinToString(","), isOpen)
                            }
                        },
                        isLoading = state.isLoading,
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsTextField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Neutral700)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(icon, contentDescription = null, tint = Primary500, modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary500,
                unfocusedBorderColor = Neutral200,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        )
    }
}

private fun splitSettingsCsv(value: String): List<String> {
    return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
