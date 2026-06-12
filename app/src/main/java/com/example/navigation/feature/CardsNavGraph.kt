package com.example.navigation.feature

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.navigation.NavigationDestinations
import com.example.ui.CreditCardWorkspaceHub
import com.example.ui.LoanWorkspaceHub
import java.text.NumberFormat

fun NavGraphBuilder.cardsNavGraph(
    navController: NavController,
    currencyFormatter: NumberFormat
) {
    navigation(
        startDestination = NavigationDestinations.CREDIT_CARDS,
        route = NavigationDestinations.CARDS_ROUTE
    ) {
        composable(NavigationDestinations.CREDIT_CARDS) {
            CreditCardWorkspaceHub(
                currencyFormatter = currencyFormatter
            )
        }

        composable(NavigationDestinations.EMI_LOANS) {
            LoanWorkspaceHub(
                currencyFormatter = currencyFormatter
            )
        }
    }
}
