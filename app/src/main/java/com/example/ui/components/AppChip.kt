package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AppChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = NeonGreen,
    unselectedColor: Color = SurfaceBlue
) {
    val bg = if (selected) selectedColor.copy(alpha = 0.15f) else unselectedColor
    val borderStroke = if (selected) BorderStroke(AppBorder.Thin, selectedColor) else BorderStroke(AppBorder.Thin, BorderColor)
    val textColor = if (selected) selectedColor else TextGray

    Box(
        modifier = modifier
            .clip(AppShapes.ChipShape)
            .background(bg)
            .border(borderStroke, AppShapes.ChipShape)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.Space12, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        )
    }
}
