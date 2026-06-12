package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.viewmodel.WealthPulseViewModel
import java.text.NumberFormat

@Composable
fun AppNavGraph(
    viewModel: WealthPulseViewModel,
    currencyFormatter: NumberFormat,
    onTriggerManualDialog: (String) -> Unit
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NavigationDestinations.BOTTOM_NAV_GRAPH,
        route = NavigationDestinations.ROOT_GRAPH
    ) {
        composable(NavigationDestinations.BOTTOM_NAV_GRAPH) {
            BottomNavContainer(
                viewModel = viewModel,
                currencyFormatter = currencyFormatter,
                onTriggerManualDialog = onTriggerManualDialog
            )
        }
    }
}
