package com.zappay.app.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.BuildConfig
import com.zappay.app.ui.components.ZapPayCard
import com.zappay.app.ui.theme.*

@Composable
fun WelcomeScreen(
    onCustomerClick: () -> Unit,
    onPumpClick: () -> Unit,
    onDirectToCustomer: (() -> Unit)? = null,
    onDirectToPump: (() -> Unit)? = null,
    onSetupStationClick: (() -> Unit)? = null,
    authViewModel: AuthViewModel? = null,
) {
    val authState by authViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var sessionChecked by remember { mutableStateOf(false) }

    val isPartnerApp = BuildConfig.APP_ROLE == "partner"

    LaunchedEffect(authState) {
        if (authState != null) {
            if (authState!!.isLoggedIn) {
                when (authState!!.userRole) {
                    "pump_operator", "pump_owner" -> onDirectToPump?.invoke() ?: onPumpClick()
                    else -> onDirectToCustomer?.invoke() ?: onCustomerClick()
                }
            } else {
                sessionChecked = true
            }
        }
    }

    if (!sessionChecked && authState == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Primary900, Primary800))),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Accent400, strokeWidth = 3.dp)
        }
        return
    }

    // ── Animated Logo ──
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val logoGlow = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logoGlow",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = if (isPartnerApp) listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F766E)) else listOf(Primary900, Primary800, Primary700),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 1600f),
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.12f))

            // ── Logo ──
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(logoGlow.value)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isPartnerApp) listOf(Teal500, Green500) else listOf(Primary500, Teal500),
                            start = Offset.Zero,
                            end = Offset(200f, 200f),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isPartnerApp) Icons.Filled.LocalGasStation else Icons.Filled.ElectricBolt,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(48.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                if (isPartnerApp) "ZapPay Partner" else "ZapPay",
                fontSize = if (isPartnerApp) 32.sp else 36.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                letterSpacing = (-1).sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (isPartnerApp) "Forecourt Manager for Petrol Pumps & EV Hubs" else "The smart way to pay for fuel",
                fontSize = 15.sp,
                color = Primary200,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(10.dp))

            // Trust badge
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Accent400, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    if (isPartnerApp) "Connected Bay Camera • Sub-15s Auto-Debit" else "Secure & Fast Payments",
                    fontSize = 12.sp,
                    color = Primary300,
                )
            }

            Spacer(Modifier.weight(0.15f))

            if (isPartnerApp) {
                // ── PARTNER PORTAL OPTIONS ──
                Text(
                    "RETAILER STATION ACCESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary300,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(14.dp))

                // Launch Command Center
                Card(
                    onClick = { onDirectToPump?.invoke() ?: onPumpClick() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Green500.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Green500),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Forecourt Command Center", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                            Text("Bay camera auto-scan & live shift", fontSize = 12.sp, color = Green100)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = White, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Onboard New Station
                Card(
                    onClick = { onSetupStationClick?.invoke() ?: onPumpClick() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Purple500.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Onboard New Pump / EV Hub", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                            Text("Setup bays, rates, nozzles & bank payout", fontSize = 12.sp, color = Primary300)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Primary300, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Operator PIN Login
                TextButton(onClick = onPumpClick) {
                    Text("Attendant Phone & PIN Sign In →", color = Accent400, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Spacer(Modifier.weight(0.1f))
            } else {
                // ── CUSTOMER APP ROLE SELECTION ──
                Text(
                    "GET STARTED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary300,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(16.dp))

                // Customer card
                Card(
                    onClick = onCustomerClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Primary500.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Primary300,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Customer", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = White)
                            Text(
                                "Pay for fuel & track expenses",
                                fontSize = 13.sp,
                                color = Primary300,
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Primary300,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Pump Owner card
                Card(
                    onClick = onPumpClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Teal500.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.LocalGasStation,
                                contentDescription = null,
                                tint = Teal300,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Pump Owner", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = White)
                            Text(
                                "Scan codes & accept payments",
                                fontSize = 13.sp,
                                color = Primary300,
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Primary300,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(Modifier.weight(0.15f))
            }

            // Footer
            Text(
                "By continuing, you agree to our Terms & Privacy Policy",
                fontSize = 11.sp,
                color = Primary400.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
