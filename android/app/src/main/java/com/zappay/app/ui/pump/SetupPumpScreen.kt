package com.zappay.app.ui.pump

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.PumpRegisterRequest
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPumpScreen(
    viewModel: PumpViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    var pumpName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state_ by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    LaunchedEffect(state.registrationSuccess) {
        if (state.registrationSuccess) {
            viewModel.clearRegistrationSuccess()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Pump", fontWeight = FontWeight.SemiBold) },
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
            Text("Fill in your pump details", fontSize = 14.sp, color = Gray500)
            Spacer(Modifier.height(20.dp))

            ZapPayInput(value = pumpName, onValueChange = { pumpName = it }, label = "Pump Name", placeholder = "City Fuel Station")
            Spacer(Modifier.height(12.dp))
            ZapPayInput(value = ownerName, onValueChange = { ownerName = it }, label = "Owner Name", placeholder = "Your full name")
            Spacer(Modifier.height(12.dp))
            ZapPayInput(value = licenseNumber, onValueChange = { licenseNumber = it }, label = "License Number", placeholder = "DL/PC/2024/...")
            Spacer(Modifier.height(12.dp))
            ZapPayInput(value = address, onValueChange = { address = it }, label = "Address", placeholder = "Street address")
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ZapPayInput(value = city, onValueChange = { city = it }, label = "City", placeholder = "Mumbai", modifier = Modifier.weight(1f))
                ZapPayInput(value = state_, onValueChange = { state_ = it }, label = "State", placeholder = "MH", modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            ZapPayInput(value = pincode, onValueChange = { pincode = it }, label = "Pincode", placeholder = "400001", keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(12.dp))
            ZapPayInput(value = phoneNumber, onValueChange = { phoneNumber = it }, label = "Phone", placeholder = "9876543210", keyboardType = KeyboardType.Phone)
            Spacer(Modifier.height(12.dp))
            ZapPayInput(value = email, onValueChange = { email = it }, label = "Email (optional)", placeholder = "owner@example.com", keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)

            Spacer(Modifier.height(24.dp))

            if (state.error != null) {
                ErrorMessage(state.error!!)
                Spacer(Modifier.height(12.dp))
            }

            ZapPayButton(
                text = if (state.isLoading) "Registering..." else "Register Pump",
                onClick = {
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
