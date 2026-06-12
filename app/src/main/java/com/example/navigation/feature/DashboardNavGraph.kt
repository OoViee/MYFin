package com.example.navigation.feature

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.DashboardScreen
import com.example.navigation.NavigationDestinations
import java.text.NumberFormat

fun NavGraphBuilder.dashboardNavGraph(
    navController: NavController,
    currencyFormatter: NumberFormat,
    onTriggerQuickAction: (String) -> Unit
) {
    navigation(
        startDestination = NavigationDestinations.DASHBOARD,
        route = NavigationDestinations.HOME_ROUTE
    ) {
        composable(NavigationDestinations.DASHBOARD) {
            DashboardScreen(
                onNavigateToTab = { tab ->
                    // Convert old tab string to modern Nav routes if needed
                    val targetRoute = when (tab) {
                        "expenses" -> NavigationDestinations.TRANSACTIONS_ROUTE
                        "lent" -> NavigationDestinations.SPLIT_ROUTE
                        "reports" -> NavigationDestinations.INSIGHTS
                        "more" -> NavigationDestinations.PROFILE_ROUTE
                        else -> NavigationDestinations.HOME_ROUTE
                    }
                    navController.navigate(targetRoute) {
                        launchSingleTop = true
                    }
                },
                onTriggerQuickAction = onTriggerQuickAction,
                currencyFormatter = currencyFormatter
            )
        }
    }
}
