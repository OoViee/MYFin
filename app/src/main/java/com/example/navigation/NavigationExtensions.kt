package com.example.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Extension functions to enforce consistent, safe, state-aware navigation behaviors.
 */
fun NavController.navigateToBottomBarRoute(route: String) {
    this.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(this@navigateToBottomBarRoute.graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

fun NavController.navigateAndClearBackStack(route: String) {
    this.navigate(route) {
        popUpTo(0) {
            inclusive = true
        }
    }
}
