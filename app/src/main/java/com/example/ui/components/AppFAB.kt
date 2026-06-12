package com.example.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

@Composable
fun AppFAB(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = NeonGreen,
    contentColor: Color = NavyBg
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.ButtonShape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = AppElevation.Button)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun AppExtendedFAB(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = NeonGreen,
    contentColor: Color = NavyBg
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.ButtonShape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = AppElevation.Button),
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        },
        text = {
            Text(text = label)
        }
    )
}
