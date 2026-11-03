package com.zappay.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.ZapPayButton
import com.zappay.app.ui.components.ZapPayInput
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatch by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn, state.userRole) {
        if (state.isLoggedIn) onLoginSuccess(state.userRole ?: role)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Primary900, Primary800),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 600f),
                    )
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                // ── Header Section (on gradient background) ──
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isRegistering) "Create Account" else "Welcome Back",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                    )
                    Text(
                        "Sign ${if (isRegistering) "up" else "in"} as ${role.replace("_", " ")}",
                        fontSize = 15.sp,
                        color = Primary300,
                    )
                    Spacer(Modifier.height(28.dp))
                }

                // ── Form Section (white card) ──
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(24.dp),
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Error
                    if (state.error != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Danger100),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                state.error!!,
                                color = Danger500,
                                modifier = Modifier.padding(14.dp),
                                fontSize = 13.sp,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Debug OTP
                    if (state.debugOtp != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Warning100),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "DEBUG OTP: ${state.debugOtp}",
                                color = Warning700,
                                modifier = Modifier.padding(14.dp),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
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
                        // ── OTP Input Boxes ──
                        Text("Enter OTP", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Neutral700)
                        Spacer(Modifier.height(10.dp))
                        OtpInputField(
                            otp = otp,
                            onOtpChange = { otp = it },
                        )
                        Spacer(Modifier.height(24.dp))

                        ZapPayButton(
                            text = state.isLoading.let { if (it) "Verifying..." else "Verify OTP" },
                            onClick = { viewModel.loginWithOTP(phone, otp, role) },
                            isLoading = state.isLoading,
                            enabled = otp.length == 6 && phone.length >= 10,
                        )
                        Spacer(Modifier.height(12.dp))

                        TextButton(
                            onClick = { viewModel.sendOTP(phone) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = phone.length >= 10 && !state.isLoading,
                        ) { Text("Resend OTP", color = Primary500, fontWeight = FontWeight.SemiBold) }
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
                                onClick = { viewModel.login(phone, password, role) },
                                isLoading = state.isLoading,
                                enabled = phone.length >= 10 && password.isNotEmpty(),
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(
                                onClick = { useOTP = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Login with OTP instead", color = Primary500, fontWeight = FontWeight.Medium) }
                        } else {
                            ZapPayInput(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordMismatch = confirmPassword.isNotEmpty() && it != confirmPassword
                                },
                                label = "Password",
                                placeholder = "Create a password",
                                isPassword = true,
                                imeAction = ImeAction.Next,
                            )
                            Spacer(Modifier.height(16.dp))
                            ZapPayInput(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    passwordMismatch = password != it
                                },
                                label = "Confirm Password",
                                placeholder = "Re-enter password",
                                isPassword = true,
                                imeAction = ImeAction.Done,
                                error = if (passwordMismatch) "Passwords do not match" else null,
                            )
                            Spacer(Modifier.height(24.dp))

                            ZapPayButton(
                                text = "Create Account",
                                onClick = { viewModel.register(phone, name, password, role) },
                                isLoading = state.isLoading,
                                enabled = phone.length >= 10 && name.isNotEmpty()
                                    && password.length >= 6 && !passwordMismatch && confirmPassword.isNotEmpty(),
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Toggle register/login
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text(
                            if (isRegistering) "Already have an account? " else "New user? ",
                            color = Neutral500,
                            fontSize = 14.sp,
                        )
                        TextButton(onClick = { isRegistering = !isRegistering; useOTP = false }) {
                            Text(
                                if (isRegistering) "Login" else "Register",
                                color = Primary500,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── OTP Input with Individual Boxes ──
@Composable
private fun OtpInputField(
    otp: String,
    onOtpChange: (String) -> Unit,
    digitCount: Int = 6,
) {
    BasicTextField(
        value = otp,
        onValueChange = {
            if (it.length <= digitCount && it.all { c -> c.isDigit() }) {
                onOtpChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(digitCount) { index ->
                    val char = otp.getOrNull(index)
                    val isFocused = otp.length == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (char != null) Primary50
                                else MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = when {
                                    isFocused -> Primary500
                                    char != null -> Primary300
                                    else -> Neutral200
                                },
                                shape = RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = char?.toString() ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary800,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        },
    )
}
