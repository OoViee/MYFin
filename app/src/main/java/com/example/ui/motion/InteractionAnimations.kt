package com.example.ui.motion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Tactile interaction modifiers that make items feel touchable.
 * Supports Apple inspired smooth 100ms scale-down press transitions.
 */

/**
 * Button Press Animation:
 * Scale: 1.0 -> 0.98
 * Duration: 100ms (fast)
 * Release: Smooth return
 * Avoids bouncy elastic overshoot
 */
fun Modifier.tactileButton(
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val pressScale = if (isPressed && enabled) 0.97f else if (isHovered && enabled) 1.01f else 1.0f
    val scale by animateFloatAsState(
        targetValue = pressScale,
        animationSpec = MotionSpecs.tweenFast(),
        label = "btn_tactile_scale"
    )

    val baseModifier = this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }

    if (onClick != null) {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            enabled = enabled,
            onClick = onClick
        )
    } else {
        baseModifier
    }
}

/**
 * Card Press Animation:
 * Scale: 1.0 -> 0.985 (Keep movement minimal)
 * Duration: 100ms (fast)
 * Release: Smooth decay
 */
fun Modifier.tactileCard(
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val pressScale = if (isPressed && enabled) 0.985f else if (isHovered && enabled) 1.01f else 1.0f
    val scale by animateFloatAsState(
        targetValue = pressScale,
        animationSpec = MotionSpecs.tweenFast(),
        label = "card_tactile_scale"
    )

    val baseModifier = this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }

    if (onClick != null) {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            enabled = enabled,
            onClick = onClick
        )
    } else {
        baseModifier
    }
}

/**
 * List Item / Grid Cell Touch:
 * Minimal scale, very quick return.
 */
fun Modifier.tactileListItem(
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val targetScale = if (isPressed && enabled) 0.99f else 1.0f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = MotionSpecs.tweenFast(),
        label = "list_item_tactile_scale"
    )
    
    val baseModifier = this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }

    if (onClick != null) {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            enabled = enabled,
            onClick = onClick
        )
    } else {
        baseModifier
    }
}
