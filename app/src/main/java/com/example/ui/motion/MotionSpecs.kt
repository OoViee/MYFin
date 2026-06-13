package com.example.ui.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Reusable animation specs constructed from centralized MotionTokens.
 * Automatically wraps animated durations to respect the "Reduced Motion" accessibility setting.
 */
object MotionSpecs {

    @Composable
    @ReadOnlyComposable
    fun <T> tweenFast(easing: Easing = MotionTokens.AppleEaseInOut): TwinSpec<T> {
        val duration = if (LocalReducedMotion.current) 0 else MotionTokens.DurationFast
        return tween(durationMillis = duration, easing = easing)
    }

    @Composable
    @ReadOnlyComposable
    fun <T> tweenNormal(easing: Easing = MotionTokens.AppleEaseInOut): TweenSpec<T> {
        val duration = if (LocalReducedMotion.current) 0 else MotionTokens.DurationNormal
        return tween(durationMillis = duration, easing = easing)
    }

    @Composable
    @ReadOnlyComposable
    fun <T> tweenSlow(easing: Easing = MotionTokens.AppleEaseInOut): TweenSpec<T> {
        val duration = if (LocalReducedMotion.current) 0 else MotionTokens.DurationSlow
        return tween(durationMillis = duration, easing = easing)
    }

    @Composable
    @ReadOnlyComposable
    fun <T> tweenMax(easing: Easing = MotionTokens.AppleEaseInOut): TweenSpec<T> {
        val duration = if (LocalReducedMotion.current) 0 else MotionTokens.DurationMax
        return tween(durationMillis = duration, easing = easing)
    }

    /**
     * Apple HIG style spring: highly damped, no visible wobble, feels organic and expensive.
     */
    @Composable
    @ReadOnlyComposable
    fun <T> springSmooth(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = Spring.StiffnessLow
    ): SpringSpec<T> {
        return spring(dampingRatio = dampingRatio, stiffness = stiffness)
    }

    /**
     * A slightly punchy, tactile spring spec for small interaction micro-responses (chips, selectors).
     */
    @Composable
    @ReadOnlyComposable
    fun <T> springMicro(): SpringSpec<T> {
        return spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    }
}

// Simple typealias to clean up reference syntax if needed
typealias TwinSpec<T> = TweenSpec<T>
