package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.theme.*

@Composable
fun AppDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        var isAnimated by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isAnimated = true
        }
        val scale by animateFloatAsState(
            targetValue = if (isAnimated) 1.0f else 0.94f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "dialog_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isAnimated) 1.0f else 0.0f,
            animationSpec = tween(150),
            label = "dialog_alpha"
        )

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .clip(AppShapes.DialogShape)
                .background(SurfaceBlue)
                .border(AppBorder.Thin, BorderColor, AppShapes.DialogShape),
            color = SurfaceBlue
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.Space24)
            ) {
                Text(
                    text = title,
                    color = TextWhite,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(AppSpacing.Space16))
                
                Column {
                    content()
                }
            }
        }
    }
}

@Composable
fun AppConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDanger: Boolean = false
) {
    AppDialog(
        title = title,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Text(
            text = message,
            color = TextGray,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
        )
        Spacer(modifier = Modifier.height(AppSpacing.Space24))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            AppTextButton(
                text = dismissText,
                onClick = onDismiss
            )
            Spacer(modifier = Modifier.width(AppSpacing.Space12))
            if (isDanger) {
                AppDangerButton(
                    text = confirmText,
                    onClick = onConfirm
                )
            } else {
                AppButton(
                    text = confirmText,
                    onClick = onConfirm
                )
            }
        }
    }
}

// Convenient typealias mapping for dialogues
typealias ColumnScope = androidx.compose.foundation.layout.ColumnScope
