package com.zappay.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════
// ZapPay Theme — Premium Fintech with Dark Mode
// ═══════════════════════════════════════════════════════════

private val LightColorScheme = lightColorScheme(
    primary = Primary500,
    onPrimary = White,
    primaryContainer = Primary50,
    onPrimaryContainer = Primary900,
    secondary = Teal500,
    onSecondary = White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal700,
    tertiary = Accent500,
    onTertiary = Primary900,
    tertiaryContainer = Accent100,
    onTertiaryContainer = Accent700,
    background = Neutral50,
    onBackground = Neutral900,
    surface = White,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral700,
    outline = Neutral200,
    outlineVariant = Neutral100,
    error = Danger500,
    onError = White,
    errorContainer = Danger100,
    onErrorContainer = Danger700,
    inverseSurface = Neutral900,
    inverseOnSurface = Neutral100,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary400,
    onPrimary = Primary900,
    primaryContainer = Primary800,
    onPrimaryContainer = Primary100,
    secondary = Teal300,
    onSecondary = Primary900,
    secondaryContainer = Teal700,
    onSecondaryContainer = Teal100,
    tertiary = Accent400,
    onTertiary = Primary900,
    tertiaryContainer = Accent700,
    onTertiaryContainer = Accent100,
    background = DarkSurface,
    onBackground = Neutral100,
    surface = DarkSurface1,
    onSurface = Neutral100,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = Neutral300,
    outline = DarkBorder,
    outlineVariant = DarkSurface3,
    error = Danger400,
    onError = Primary900,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Danger100,
    inverseSurface = Neutral100,
    inverseOnSurface = Neutral900,
)

// ── Custom Extended Colors (not in Material 3 scheme) ──
data class ZapPayExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val gold: Color,
    val goldContainer: Color,
    val cardSurface: Color,
    val subtleText: Color,
    val divider: Color,
)

val LightExtendedColors = ZapPayExtendedColors(
    success = Success500,
    onSuccess = White,
    successContainer = Success100,
    warning = Warning500,
    onWarning = White,
    warningContainer = Warning100,
    gold = Accent500,
    goldContainer = Accent100,
    cardSurface = White,
    subtleText = Neutral500,
    divider = Neutral100,
)

val DarkExtendedColors = ZapPayExtendedColors(
    success = Success400,
    onSuccess = Primary900,
    successContainer = Color(0xFF14532D),
    warning = Warning400,
    onWarning = Primary900,
    warningContainer = Color(0xFF78350F),
    gold = Accent400,
    goldContainer = Color(0xFF3D2E0A),
    cardSurface = DarkSurface2,
    subtleText = Neutral400,
    divider = DarkBorder,
)

val LocalExtendedColors = compositionLocalOf { LightExtendedColors }

@Composable
fun ZapPayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    // Set status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = if (darkTheme) DarkSurface.toArgb() else Primary900.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ZapPayTypography,
            shapes = Shapes(
                extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            ),
            content = content,
        )
    }
}

/** Convenience accessor for extended colors in composable scope */
object ZapPayThemeExtras {
    val colors: ZapPayExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
