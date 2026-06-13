package com.example.ui.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/**
 * Standard enterprise-grade page entry/exit animations representing the core of
 * MYFin's fluid single-screen and multi-tab system.
 */
object PageTransitions {

    fun enterpriseEnter(reducedMotion: Boolean = false): EnterTransition {
        if (reducedMotion) return fadeIn(animationSpec = tween(0))
        return fadeIn(
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseOut)
        ) + slideInHorizontally(
            initialOffsetX = { it / 8 },
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseOut)
        )
    }

    fun enterpriseExit(reducedMotion: Boolean = false): ExitTransition {
        if (reducedMotion) return fadeOut(animationSpec = tween(0))
        return fadeOut(
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseIn)
        ) + slideOutHorizontally(
            targetOffsetX = { -it / 8 },
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseIn)
        )
    }

    fun enterprisePopEnter(reducedMotion: Boolean = false): EnterTransition {
        if (reducedMotion) return fadeIn(animationSpec = tween(0))
        return fadeIn(
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseOut)
        ) + slideInHorizontally(
            initialOffsetX = { -it / 8 },
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseOut)
        )
    }

    fun enterprisePopExit(reducedMotion: Boolean = false): ExitTransition {
        if (reducedMotion) return fadeOut(animationSpec = tween(0))
        return fadeOut(
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseIn)
        ) + slideOutHorizontally(
            targetOffsetX = { it / 8 },
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseIn)
        )
    }

    // Gentle vertical sheet-like slide
    fun slideUpEnter(reducedMotion: Boolean = false): EnterTransition {
        if (reducedMotion) return fadeIn(animationSpec = tween(0))
        return slideInVertically(
            initialOffsetY = { it / 6 },
            animationSpec = tween(MotionTokens.DurationSlow, easing = MotionTokens.AppleEaseOut)
        ) + fadeIn(
            animationSpec = tween(MotionTokens.DurationSlow, easing = MotionTokens.AppleEaseOut)
        )
    }

    fun slideDownExit(reducedMotion: Boolean = false): ExitTransition {
        if (reducedMotion) return fadeOut(animationSpec = tween(0))
        return slideOutVertically(
            targetOffsetY = { it / 6 },
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseIn)
        ) + fadeOut(
            animationSpec = tween(MotionTokens.DurationNormal, easing = MotionTokens.AppleEaseIn)
        )
    }
}
