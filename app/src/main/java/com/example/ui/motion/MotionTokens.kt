package com.example.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.compositionLocalOf

/**
 * Centered motion tokens aligned with Apple Human Interface Guidelines and modern UI.
 * Core durations and easing curves optimized for fast, tactile, and natural feeling.
 */
object MotionTokens {
    // Timing Tokins (durations in Milliseconds)
    const val DurationFast = 100      // Quick actions, button press, checklist select
    const val DurationNormal = 250    // Navigation sliding, average fades, tab switches
    const val DurationSlow = 350      // Large modal views, screen transitions
    const val DurationMax = 400       // Complex expand/collapse, heavy illustrations

    // Curve Tokens
    // Apple-inspired Ease-In-Out: highly responsive breakout with slow settling
    val AppleEaseInOut: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f).safe()
    
    // Apple-inspired Ease-Out: standard decay curve for entering UI
    val AppleEaseOut: Easing = CubicBezierEasing(0.15f, 1.0f, 0.3f, 1.0f).safe()
    
    // Apple-inspired Ease-In: standard preparation curve for exit UI
    val AppleEaseIn: Easing = CubicBezierEasing(0.35f, 0.0f, 0.65f, 1.0f).safe()

    // Deceleration curve for slide-ins
    val Decelerate: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f).safe()

    // Acceleration curve for slide-outs
    val Accelerate: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f).safe()
}

/**
 * Robust helper function to wrap an Easing function, preventing CubicBezierEasing solver
 * precision crashes near the 1.0 boundaries on older runtime environments.
 */
private fun Easing.safe(): Easing {
    return Easing { fraction ->
        if (fraction <= 0.0f) return@Easing 0.0f
        if (fraction >= 0.9999f) return@Easing 1.0f
        try {
            this@safe.transform(fraction)
        } catch (e: IllegalArgumentException) {
            fraction
        }
    }
}

/**
 * Local setting to allow users toggle Reduced Motion across the app for accessibility support.
 */
val LocalReducedMotion = compositionLocalOf { false }
