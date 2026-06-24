package com.zappay.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

// ═══════════════════════════════════════════════════════════
// ZapPay Gradient System — Reusable gradient brushes
// ═══════════════════════════════════════════════════════════

/** Main brand gradient — deep navy to teal */
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Primary900, Primary700, Primary500),
    start = Offset(0f, 0f),
    end = Offset(1000f, 800f),
)

/** Header gradient — used in dashboard and section headers */
val HeaderGradient = Brush.linearGradient(
    colors = listOf(Primary800, Primary600),
    start = Offset(0f, 0f),
    end = Offset(1200f, 600f),
)

/** Gold accent gradient — used for premium elements */
val GoldGradient = Brush.linearGradient(
    colors = listOf(Accent700, Accent500, Accent400),
    start = Offset(0f, 0f),
    end = Offset(800f, 400f),
)

/** Wallet card gradient — premium feel for balance cards */
val WalletGradient = Brush.linearGradient(
    colors = listOf(Primary900, Primary700, Teal700),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1000f),
)

/** Success gradient — payments, confirmations */
val SuccessGradient = Brush.linearGradient(
    colors = listOf(Success700, Success500),
    start = Offset(0f, 0f),
    end = Offset(800f, 400f),
)

/** Dark mode card gradient */
val DarkCardGradient = Brush.linearGradient(
    colors = listOf(DarkSurface2, DarkSurface3),
    start = Offset(0f, 0f),
    end = Offset(600f, 600f),
)

/** Subtle shimmer gradient for loading states */
fun shimmerBrush(translateX: Float) = Brush.linearGradient(
    colors = listOf(
        Neutral100,
        Neutral100.copy(alpha = 0.3f),
        Neutral100,
    ),
    start = Offset.Zero,
    end = Offset(x = translateX, y = translateX),
)

/** Dark mode shimmer */
fun darkShimmerBrush(translateX: Float) = Brush.linearGradient(
    colors = listOf(
        DarkSurface2,
        DarkSurface3.copy(alpha = 0.5f),
        DarkSurface2,
    ),
    start = Offset.Zero,
    end = Offset(x = translateX, y = translateX),
)

/** Splash screen gradient */
val SplashGradient = Brush.verticalGradient(
    colors = listOf(Primary900, Primary800, Primary700),
)

/** Banner gradient overlay — for text readability on images */
val BannerOverlayGradient = Brush.verticalGradient(
    colors = listOf(
        Black.copy(alpha = 0f),
        Black.copy(alpha = 0.6f),
    ),
)
