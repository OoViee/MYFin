package com.example.ui.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

/**
 * Premium Motion Extensions for counting balances, skeleton displays, and feedback systems.
 */

/**
 * 1. Animated balance count-up text.
 * Animates a Double amount smoothly from the old value to the new value.
 */
@Composable
fun AnimatedAmountText(
    amount: Double,
    style: TextStyle = TextStyle.Default,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color.Unspecified,
    currencyCode: String = "INR"
) {
    val reducedMotion = LocalReducedMotion.current
    
    // Hold previous target to avoid restarting count-up from 0 on every recomposition unless it's initial
    var previousTarget by remember { mutableDoubleStateOf(0.0) }
    
    val animatedValue = if (reducedMotion) {
        amount.toFloat()
    } else {
        val anim = remember { Animatable(previousTarget.toFloat()) }
        LaunchedEffect(amount) {
            anim.animateTo(
                targetValue = amount.toFloat(),
                animationSpec = tween(
                    durationMillis = MotionTokens.DurationSlow + 100,
                    easing = MotionTokens.AppleEaseOut
                )
            )
            previousTarget = amount
        }
        anim.value
    }
    
    val currencyFormatter = remember(currencyCode) {
        val locale = if (currencyCode.equals("INR", ignoreCase = true)) {
            Locale("en", "IN")
        } else {
            Locale.US
        }
        NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 2
        }
    }

    Text(
        text = currencyFormatter.format(animatedValue.toDouble()),
        style = style,
        fontWeight = fontWeight,
        color = color
    )
}

/**
 * 2. Unified Hardware-Accelerated Shimmer Modifier.
 * Renders a premium skeletal loading state. It queries the active composition color scheme
 * and builds a light-cone brush path, sliding to provide a perfect responsive glow.
 */
fun Modifier.shimmer(
    visible: Boolean = true,
    shimmerWidth: Float = 600f
): Modifier = composed {
    if (!visible) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_inf")
    
    // Smooth translation of gradient line from left to right
    val xShimmer by infiniteTransition.animateFloat(
        initialValue = -shimmerWidth,
        targetValue = shimmerWidth * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1300,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate_x"
    )

    // Form modern contextual skeleton colors respecting dark or light surface setups
    val backgroundThemeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val shimmerHighlightColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)

    this
        .background(color = backgroundThemeColor)
        .drawWithContent {
            // Draw original content behind
            drawContent()
            // Layer the sliding gradient on top
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        shimmerHighlightColor,
                        Color.Transparent
                    ),
                    start = Offset(xShimmer, 0f),
                    end = Offset(xShimmer + shimmerWidth, size.height)
                )
            )
        }
}

/**
 * 3. Haptic Feedback Architect.
 * Wraps Android Native haptic calls in structured semantic events to simulate Apple Taptic Engine feedback.
 */
class HapticFeedbackHelper(private val playHaptic: (HapticFeedbackType) -> Unit) {
    fun success() {
        playHaptic(HapticFeedbackType.LongPress)
    }
    fun warning() {
        playHaptic(HapticFeedbackType.LongPress)
    }
    fun error() {
        playHaptic(HapticFeedbackType.LongPress)
    }
    fun selection() {
        playHaptic(HapticFeedbackType.LongPress)
    }
    fun click() {
        playHaptic(HapticFeedbackType.LongPress)
    }
}

@Composable
fun rememberHapticFeedbackHelper(): HapticFeedbackHelper {
    val hapticFeedback = LocalHapticFeedback.current
    return remember(hapticFeedback) {
        HapticFeedbackHelper { type ->
            try {
                hapticFeedback.performHapticFeedback(type)
            } catch (e: Exception) {
                // Ignore silent haptics if unsupported or permission constrained
            }
        }
    }
}
