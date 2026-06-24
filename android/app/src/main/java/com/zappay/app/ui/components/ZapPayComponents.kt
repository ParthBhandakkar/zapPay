package com.zappay.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.theme.*

// ═══════════════════════════════════════════════════════════
// ZapPay Component Library — Premium UI Components
// ═══════════════════════════════════════════════════════════

// ── Primary Button ──
@Composable
fun ZapPayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
) {
    val containerColor = when (variant) {
        ButtonVariant.PRIMARY -> Primary500
        ButtonVariant.SECONDARY -> Success500
        ButtonVariant.OUTLINE -> Color.Transparent
        ButtonVariant.GOLD -> Accent500
    }
    val contentColor = when (variant) {
        ButtonVariant.PRIMARY, ButtonVariant.SECONDARY -> White
        ButtonVariant.OUTLINE -> Primary500
        ButtonVariant.GOLD -> Primary900
    }

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.6f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (variant == ButtonVariant.OUTLINE) 0.dp else 2.dp,
            pressedElevation = 0.dp,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

enum class ButtonVariant { PRIMARY, SECONDARY, OUTLINE, GOLD }

// ── Text Input ──
@Composable
fun ZapPayInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier) {
        if (label.isNotEmpty()) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Neutral700)
            Spacer(Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder, color = Neutral400) }) else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) ({
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Neutral400,
                    )
                }
            }) else null,
            leadingIcon = leadingIcon?.let({ ({ Icon(it, contentDescription = null, tint = Neutral400) }) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary500,
                unfocusedBorderColor = Neutral200,
                errorBorderColor = Danger500,
                cursorColor = Primary500,
                focusedLabelColor = Primary500,
            ),
        )
        if (error != null) {
            Text(error, color = Danger500, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }
    }
}

// ── Card ──
@Composable
fun ZapPayCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) { content() }
}

// ── Gradient Card ──
@Composable
fun ZapPayGradientCard(
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient),
        ) { content() }
    }
}

// ── Balance Card (Premium) ──
@Composable
fun BalanceCard(
    balance: Double,
    onAddMoney: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ZapPayGradientCard(gradient = WalletGradient, modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Wallet,
                    contentDescription = null,
                    tint = Accent400,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Wallet Balance", color = Primary200, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "₹ ${"%.2f".format(balance)}",
                color = White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
            )
            if (onAddMoney != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAddMoney,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White.copy(alpha = 0.15f),
                        contentColor = White,
                    ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Money", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Stat Item ──
@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = Neutral500)
    }
}

// ── Transaction Item ──
@Composable
fun TransactionItem(
    title: String,
    subtitle: String,
    amount: Double,
    isCredit: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (isCredit) Success500.copy(alpha = 0.12f) else Danger500.copy(alpha = 0.08f),
                    RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (isCredit) Icons.Outlined.ArrowDownward else Icons.Outlined.LocalGasStation,
                contentDescription = null,
                tint = if (isCredit) Success500 else Primary500,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = Neutral500)
        }
        Text(
            "${if (isCredit) "+" else "-"}₹ ${"%.2f".format(amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isCredit) Success500 else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Top App Bar ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZapPayTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
    )
}

// ── Empty State ──
@Composable
fun ZapPayEmptyState(
    icon: ImageVector = Icons.Outlined.Inbox,
    title: String,
    subtitle: String = "",
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Primary500.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Primary500.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Neutral500,
                textAlign = TextAlign.Center,
            )
        }
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            ZapPayButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.width(200.dp),
            )
        }
    }
}

// ── Loading Screen ──
@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Primary500, strokeWidth = 3.dp)
    }
}

// ── Error Message ──
@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = Neutral500, textAlign = TextAlign.Center, fontSize = 14.sp)
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRetry) { Text("Retry", color = Primary500) }
        }
    }
}

// ── Banner Card (for promotional banners) ──
@Composable
fun ZapPayBanner(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Outlined.LocalOffer,
    gradient: Brush = HeaderGradient,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().background(gradient).padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, fontSize = 13.sp, color = White.copy(alpha = 0.8f))
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

// ── Status Badge ──
@Composable
fun ZapPayBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

// ── Section Header ──
@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, fontSize = 13.sp, color = Primary500, fontWeight = FontWeight.Medium)
            }
        }
    }
}
