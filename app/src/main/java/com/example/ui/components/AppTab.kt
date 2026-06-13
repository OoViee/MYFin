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
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.motion.tactileListItem

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
                val isSelected = index == selectedTabIndex
                val tabBg by animateColorAsState(
                    targetValue = if (isSelected) SurfaceBlue else Color.Transparent,
                    animationSpec = tween(200),
                    label = "row_tab_bg"
                )
                val tabTextColor by animateColorAsState(
                    targetValue = if (isSelected) NeonGreen else TextGray,
                    animationSpec = tween(200),
                    label = "row_tab_text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(AppShapes.TabShape)
                        .background(tabBg)
                        .tactileListItem { onTabSelected(index) }
                        .padding(vertical = AppSpacing.Space12),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabTitle,
                        color = tabTextColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
    val bgAnim by animateColorAsState(
        targetValue = if (selected) NeonGreen.copy(alpha = 0.15f) else SurfaceBlue,
        animationSpec = tween(200),
        label = "tab_bg"
    )
    val textColorAnim by animateColorAsState(
        targetValue = if (selected) NeonGreen else TextGray,
        animationSpec = tween(200),
        label = "tab_text"
    )
    
    Box(
        modifier = modifier
            .clip(AppShapes.TabShape)
            .background(bgAnim)
            .tactileListItem(onClick = onClick)
            .padding(horizontal = AppSpacing.Space16, vertical = AppSpacing.Space8),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = textColorAnim,
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
