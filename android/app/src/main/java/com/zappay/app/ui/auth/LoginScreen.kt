package com.zappay.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.ZapPayButton
import com.zappay.app.ui.components.ZapPayInput
import com.zappay.app.ui.theme.*

@Composable
fun LoginScreen(
    role: String,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoginSuccess: (role: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var useOTP by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn, state.userRole) {
        if (state.isLoggedIn) onLoginSuccess(state.userRole ?: role)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onBack) { Text("Back", color = Purple500) }

        Spacer(Modifier.height(12.dp))
        Text(
            if (isRegistering) "Create Account" else "Welcome",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Gray900,
        )
        Text(
            "Sign ${if (isRegistering) "up" else "in"} as ${role.replace("_", " ")}",
            fontSize = 14.sp,
            color = Gray500,
        )
        Spacer(Modifier.height(24.dp))

        // Error
        if (state.error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Red100),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(state.error!!, color = Red500, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        // Debug OTP
        if (state.debugOtp != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Yellow100),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("DEBUG OTP: ${state.debugOtp}", color = Yellow500, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(12.dp))
        }

        // Phone
        ZapPayInput(
            value = phone,
            onValueChange = { phone = it },
            label = "Phone Number",
            placeholder = "9876543210",
            keyboardType = KeyboardType.Phone,
        )
        Spacer(Modifier.height(16.dp))

        if (isRegistering) {
            ZapPayInput(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                placeholder = "Your name",
                imeAction = ImeAction.Next,
            )
            Spacer(Modifier.height(16.dp))
        }

        if (useOTP) {
            ZapPayInput(
                value = otp,
                onValueChange = { otp = it },
                label = "OTP Code",
                placeholder = "6-digit OTP",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            )
            Spacer(Modifier.height(16.dp))

            ZapPayButton(
                text = state.isLoading.let { if (it) "Verifying..." else "Login with OTP" },
                onClick = { viewModel.loginWithOTP(phone, otp) },
                isLoading = state.isLoading,
                enabled = otp.length == 6 && phone.length >= 10,
            )
            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = { viewModel.sendOTP(phone) },
                modifier = Modifier.fillMaxWidth(),
                enabled = phone.length >= 10 && !state.isLoading,
            ) { Text("Resend OTP", color = Purple500) }
        } else {
            if (!isRegistering) {
                ZapPayInput(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Enter password",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                )
                Spacer(Modifier.height(24.dp))

                ZapPayButton(
                    text = "Login",
                    onClick = { viewModel.login(phone, password) },
                    isLoading = state.isLoading,
                    enabled = phone.length >= 10 && password.isNotEmpty(),
                )
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = { useOTP = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Login with OTP", color = Purple500) }
            } else {
                ZapPayInput(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Create a password",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                )
                Spacer(Modifier.height(24.dp))

                ZapPayButton(
                    text = "Register",
                    onClick = { viewModel.register(phone, name, password, role) },
                    isLoading = state.isLoading,
                    enabled = phone.length >= 10 && name.isNotEmpty() && password.length >= 6,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(if (isRegistering) "Already have an account? " else "New user? ", color = Gray500)
            TextButton(onClick = { isRegistering = !isRegistering; useOTP = false }) {
                Text(if (isRegistering) "Login" else "Register", color = Purple500)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
