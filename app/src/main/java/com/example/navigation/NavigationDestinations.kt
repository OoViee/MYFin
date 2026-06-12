package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

object NavigationDestinations {
    // Top-Level Graphs
    const val ROOT_GRAPH = "root_graph"
    const val BOTTOM_NAV_GRAPH = "bottom_nav_graph"

    // Primary Endpoints
    const val HOME_ROUTE = "home_route"
    const val TRANSACTIONS_ROUTE = "transactions_route"
    const val CARDS_ROUTE = "cards_route"
    const val SPLIT_ROUTE = "split_route"
    const val PROFILE_ROUTE = "profile_route"

    // Secondary/Sub-Endpoints
    // Home
    const val DASHBOARD = "dashboard"
    const val FINANCIAL_HEALTH = "financial_health"
    const val RECENT_ACTIVITY = "recent_activity"
    
    // Transactions
    const val EXPENSES = "expenses"
    const val BUDGET_GOALS = "budgets"
    const val CATEGORIES = "categories"

    // Cards
    const val CREDIT_CARDS = "credit_cards"
    const val EMI_LOANS = "emi_loans"
    const val LOANS = "loans"
    const val STATEMENTS = "statements"

    // Split
    const val SPLIT_DASHBOARD = "split_dashboard"
    const val TRIPS = "trips"
    const val SETTLEMENTS = "settlements"
    
    // Insights (Accessible from Home or Top-Level)
    const val INSIGHTS = "insights"
    
    // Profile / Settings
    const val SETTINGS = "settings"
}

sealed class BottomBarDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    object Home : BottomBarDestination(
        route = NavigationDestinations.HOME_ROUTE,
        title = "Home",
        icon = Icons.Default.Home,
        testTag = "nav_item_home"
    )

    object Transactions : BottomBarDestination(
        route = NavigationDestinations.TRANSACTIONS_ROUTE,
        title = "Transactions",
        icon = Icons.Default.DateRange,
        testTag = "nav_item_expenses"
    )

    object Cards : BottomBarDestination(
        route = NavigationDestinations.CARDS_ROUTE,
        title = "Cards",
        icon = Icons.Default.Warning, // Premium look (or choose generic safe list)
        testTag = "nav_item_credit"
    )

    object Split : BottomBarDestination(
        route = NavigationDestinations.SPLIT_ROUTE,
        title = "Split",
        icon = Icons.Default.Share,
        testTag = "nav_item_lent"
    )

    object Profile : BottomBarDestination(
        route = NavigationDestinations.PROFILE_ROUTE,
        title = "Profile",
        icon = Icons.Default.Settings,
        testTag = "nav_item_more"
    )

    companion object {
        val items = listOf(Home, Transactions, Cards, Split, Profile)
    }
}
