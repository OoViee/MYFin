package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
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
fun AppTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = false
) {
    if (isScrollable) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.Space8),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Space8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(tabs) { index, tabTitle ->
                AppTab(
                    title = tabTitle,
                    selected = index == selectedTabIndex,
                    onClick = { onTabSelected(index) }
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(AppShapes.TabShape)
                .background(BorderColor)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, tabTitle ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(AppShapes.TabShape)
                        .background(if (index == selectedTabIndex) SurfaceBlue else Color.Transparent)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = AppSpacing.Space12),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabTitle,
                        color = if (index == selectedTabIndex) NeonGreen else TextGray,
                        fontWeight = if (index == selectedTabIndex) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AppTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null
) {
    val bg = if (selected) NeonGreen.copy(alpha = 0.15f) else SurfaceBlue
    val textColor = if (selected) NeonGreen else TextGray
    val strokeColor = if (selected) NeonGreen else BorderColor
    
    Box(
        modifier = modifier
            .clip(AppShapes.TabShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.Space16, vertical = AppSpacing.Space8),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = textColor,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
            if (badgeCount != null && badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (selected) NeonGreen else TextGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = NavyBg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
