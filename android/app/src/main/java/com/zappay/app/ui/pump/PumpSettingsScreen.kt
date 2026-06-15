package com.zappay.app.ui.pump

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    LaunchedEffect(Unit) { viewModel.loadSettings() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pump Settings", fontWeight = FontWeight.SemiBold) },
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
            Text("Configure your pump", fontSize = 14.sp, color = Gray500)
            Spacer(Modifier.height(20.dp))

            Text("Fuel Types (comma separated)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = fuelTypes,
                onValueChange = { fuelTypes = it },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            Text("Fuel Rates (comma separated)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = fuelRates,
                onValueChange = { fuelRates = it },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Pump Open", fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Switch(checked = isOpen, onCheckedChange = { isOpen = it }, colors = SwitchDefaults.colors(checkedTrackColor = Purple500))
            }

            Spacer(Modifier.height(24.dp))

            if (state.error != null) {
                ErrorMessage(state.error!!)
                Spacer(Modifier.height(12.dp))
            }

            ZapPayButton(
                text = if (state.isLoading) "Saving..." else "Save Settings",
                onClick = {
                    viewModel.saveSettings(
                        mapOf(
                            "fuel_types" to fuelTypes,
                            "fuel_rates" to fuelRates,
                            "is_open" to isOpen,
                        )
                    )
                },
                isLoading = state.isLoading,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
