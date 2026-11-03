package com.zappay.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════
// ZapPay Animation System — Shared animation specs & utilities
// ═══════════════════════════════════════════════════════════

// ── Duration Constants ──
object ZapPayMotion {
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
    const val EXTRA_SLOW = 800
}

// ── Easing ──
val ZapPayEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
val ZapPayBounceEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)

// ── Enter / Exit Transitions for Navigation ──
val screenEnterTransition: EnterTransition =
    fadeIn(animationSpec = tween(ZapPayMotion.NORMAL, easing = ZapPayEasing)) +
    slideInHorizontally(
        initialOffsetX = { it / 4 },
        animationSpec = tween(ZapPayMotion.NORMAL, easing = ZapPayEasing),
    )

val screenExitTransition: ExitTransition =
    fadeOut(animationSpec = tween(ZapPayMotion.FAST, easing = ZapPayEasing)) +
    slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = tween(ZapPayMotion.NORMAL, easing = ZapPayEasing),
    )

val screenPopEnterTransition: EnterTransition =
    fadeIn(animationSpec = tween(ZapPayMotion.NORMAL, easing = ZapPayEasing)) +
    slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(ZapPayMotion.NORMAL, easing = ZapPayEasing),
    )

val screenPopExitTransition: ExitTransition =
    fadeOut(animationSpec = tween(ZapPayMotion.FAST, easing = ZapPayEasing)) +
    slideOutHorizontally(
        targetOffsetX = { it / 4 },
        animationSpec = tween(ZapPayMotion.NORMAL, easing = ZapPayEasing),
    )

// ── Staggered List Animation ──
/**
 * Modifier for staggered entrance animation on list items.
 * [index] is the item position (0-based).
 * [baseDelayMs] is the delay between each item.
 */
fun Modifier.staggeredEntrance(
    index: Int,
    baseDelayMs: Int = 50,
): Modifier = composed {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * baseDelayMs.toLong())
        animatable.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = ZapPayMotion.NORMAL,
                easing = ZapPayEasing,
            ),
        )
    }
    this.graphicsLayer {
        alpha = animatable.value
        translationY = (1f - animatable.value) * 40f
    }
}

// ── Pulse Animation (for live indicators) ──
@Composable
fun pulseScale(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    return scale.value
}

// ── Counting Number Animation ──
@Composable
fun animatedFloat(targetValue: Float, durationMs: Int = ZapPayMotion.SLOW): Float {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue,
            animationSpec = tween(durationMillis = durationMs, easing = ZapPayEasing),
        )
    }
    return animatable.value
}

// ── Shimmer translate animation ──
@Composable
fun shimmerTranslateAnim(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    return translateAnim.value
}
