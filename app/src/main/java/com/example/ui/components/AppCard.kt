package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.CardShape,
    containerColor: Color = SurfaceBlue,
    contentColor: Color = TextWhite,
    border: BorderStroke? = BorderStroke(AppBorder.Thin, BorderColor),
    elevation: Dp = AppElevation.None,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier.clickable(onClick = onClick),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(
                modifier = Modifier.padding(AppSpacing.Space16),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(
                modifier = Modifier.padding(AppSpacing.Space16),
                content = content
            )
        }
    }
}

@Composable
fun AppOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    AppCard(
        modifier = modifier,
        border = BorderStroke(AppBorder.Thin, BorderColor),
        containerColor = Color.Transparent,
        onClick = onClick,
        content = content
    )
}

@Composable
fun AppElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    AppCard(
        modifier = modifier,
        border = null,
        elevation = AppElevation.Card,
        onClick = onClick,
        content = content
    )
}
