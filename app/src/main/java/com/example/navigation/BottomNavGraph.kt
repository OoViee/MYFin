package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.navigation.feature.*
import com.example.ui.components.AppFAB
import com.example.ui.components.AppTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WealthPulseViewModel
import java.text.NumberFormat

@Composable
fun BottomNavContainer(
    viewModel: WealthPulseViewModel,
    currencyFormatter: NumberFormat,
    onTriggerManualDialog: (String) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Dynamic FAB parameters based on active route
    val fabConfig = remember(currentRoute) {
        when (currentRoute) {
            NavigationDestinations.EXPENSES -> FabConfiguration(
                icon = Icons.Default.Add,
                contentDescription = "Add Expense",
                onClick = { onTriggerManualDialog("EXPENSE") }
            )
            NavigationDestinations.CREDIT_CARDS -> FabConfiguration(
                icon = Icons.Default.Add,
                contentDescription = "Add Card",
                onClick = { onTriggerManualDialog("CREDIT") }
            )
            NavigationDestinations.SPLIT_DASHBOARD -> FabConfiguration(
                icon = Icons.Default.Add,
                contentDescription = "Add Split Expense",
                onClick = { onTriggerManualDialog("DEBT") }
            )
            else -> null // No FAB for Dashboard, Settings, etc., unless contextual
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (fabConfig != null) {
                AppFAB(
                    icon = fabConfig.icon,
                    contentDescription = fabConfig.contentDescription,
                    onClick = fabConfig.onClick,
                    modifier = Modifier.testTag("action_fab")
                )
            }
        },
        bottomBar = {
            // Elegant Premium Floating Island Bottom Navigation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomBarDestination.items.forEach { dest ->
                            // Check if current destination matches this destination's route or its nested items
                            val isSelected = currentRoute == dest.route || 
                                             (dest.route == NavigationDestinations.HOME_ROUTE && currentRoute == NavigationDestinations.DASHBOARD) ||
                                             (dest.route == NavigationDestinations.TRANSACTIONS_ROUTE && currentRoute == NavigationDestinations.EXPENSES) ||
                                             (dest.route == NavigationDestinations.CARDS_ROUTE && currentRoute == NavigationDestinations.CREDIT_CARDS) ||
                                             (dest.route == NavigationDestinations.SPLIT_ROUTE && currentRoute == NavigationDestinations.SPLIT_DASHBOARD) ||
                                             (dest.route == NavigationDestinations.PROFILE_ROUTE && currentRoute == NavigationDestinations.SETTINGS)
 
                            val tintColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            val bgAlpha = if (isSelected) 0.08f else 0.0f
 
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = bgAlpha) else Color.Transparent)
                                    .clickable {
                                        navController.navigateToBottomBarRoute(dest.route)
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag(dest.testTag),
                                contentAlignment = Alignment.Center
                             ) {
                                val sizeScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.05f else 1.0f,
                                    animationSpec = tween(durationMillis = 150),
                                    label = "nav_item_scale"
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = sizeScale
                                        scaleY = sizeScale
                                    }
                                ) {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.title,
                                        tint = tintColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dest.title,
                                        fontSize = 10.sp,
                                        color = tintColor,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationDestinations.HOME_ROUTE,
            enterTransition = { fadeIn(animationSpec = tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(250)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(250)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            route = NavigationDestinations.BOTTOM_NAV_GRAPH
        ) {
            dashboardNavGraph(
                navController = navController,
                currencyFormatter = currencyFormatter,
                onTriggerQuickAction = onTriggerManualDialog
            )

            transactionNavGraph(
                navController = navController,
                viewModel = viewModel,
                currencyFormatter = currencyFormatter
            )

            cardsNavGraph(
                navController = navController,
                currencyFormatter = currencyFormatter
            )

            splitNavGraph(
                navController = navController,
                currencyFormatter = currencyFormatter
            )

            profileNavGraph(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

data class FabConfiguration(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)
