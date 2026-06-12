package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
fun AppLoading(
    modifier: Modifier = Modifier,
    color: Color = NeonGreen,
    strokeWidth: Float = 4f
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth.dp
    )
}

@Composable
fun AppLoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "Loading records..."
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLoading(modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(AppSpacing.Space16))
            Text(
                text = message,
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun AppSkeletonCard(
    modifier: Modifier = Modifier,
    height: Float = 100f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(AppShapes.CardShape)
            .background(SurfaceBlue.copy(alpha = 0.5f))
    )
}
