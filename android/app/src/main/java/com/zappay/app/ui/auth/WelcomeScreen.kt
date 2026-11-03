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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.ZapPayCard
import com.zappay.app.ui.theme.*

@Composable
fun WelcomeScreen(
    onCustomerClick: () -> Unit,
    onPumpClick: () -> Unit,
    onDirectToCustomer: (() -> Unit)? = null,
    onDirectToPump: (() -> Unit)? = null,
    authViewModel: AuthViewModel? = null,
) {
    val authState by authViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var sessionChecked by remember { mutableStateOf(false) }

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
                    colors = listOf(Primary900, Primary800, Primary700),
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
            Spacer(Modifier.weight(0.15f))

            // ── Logo ──
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(logoGlow.value)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Primary500, Teal500),
                            start = Offset.Zero,
                            end = Offset(200f, 200f),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("Z", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = White)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "ZapPay",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                letterSpacing = (-1).sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "The smart way to pay for fuel",
                fontSize = 16.sp,
                color = Primary200,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(12.dp))

            // Trust badge
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Accent400, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Secure & Fast Payments", fontSize = 12.sp, color = Primary300)
            }

            Spacer(Modifier.weight(0.2f))

            // ── Role Selection ──
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
