package com.example.navigation.feature

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.navigation.NavigationDestinations
import com.example.ui.SplitsWorkspaceHub
import java.text.NumberFormat

fun NavGraphBuilder.splitNavGraph(
    navController: NavController,
    currencyFormatter: NumberFormat
) {
    navigation(
        startDestination = NavigationDestinations.SPLIT_DASHBOARD,
        route = NavigationDestinations.SPLIT_ROUTE
    ) {
        composable(NavigationDestinations.SPLIT_DASHBOARD) {
            SplitsWorkspaceHub(
                currencyFormatter = currencyFormatter
            )
        }
    }
}
