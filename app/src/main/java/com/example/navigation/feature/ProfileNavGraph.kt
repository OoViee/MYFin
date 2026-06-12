package com.example.navigation.feature

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.navigation.NavigationDestinations
import com.example.ui.viewmodel.WealthPulseViewModel

// We can import SettingsWorkspacePage if we move it or if it is accessible. 
// Since it's currently defined package-less or in MainActivity package com.example,
// we reference it using com.example.SettingsWorkspacePage!
import com.example.SettingsWorkspacePage

fun NavGraphBuilder.profileNavGraph(
    navController: NavController,
    viewModel: WealthPulseViewModel
) {
    navigation(
        startDestination = NavigationDestinations.SETTINGS,
        route = NavigationDestinations.PROFILE_ROUTE
    ) {
        composable(NavigationDestinations.SETTINGS) {
            SettingsWorkspacePage(
                viewModel = viewModel
            )
        }
    }
}
