package com.example.navigation.feature

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.navigation.NavigationDestinations
import com.example.ui.ExpensesWorkspaceHub
import com.example.ui.BudgetWorkspaceHub
import com.example.ui.viewmodel.WealthPulseViewModel
import java.text.NumberFormat

fun NavGraphBuilder.transactionNavGraph(
    navController: NavController,
    viewModel: WealthPulseViewModel,
    currencyFormatter: NumberFormat
) {
    navigation(
        startDestination = NavigationDestinations.EXPENSES,
        route = NavigationDestinations.TRANSACTIONS_ROUTE
    ) {
        composable(NavigationDestinations.EXPENSES) {
            // Expenses Ledger Screen
            val dailyExpenses = viewModel.dailyExpenses.value
            ExpensesWorkspaceHub(
                dailyExpenses = dailyExpenses,
                currencyFormatter = currencyFormatter,
                viewModel = viewModel
            )
        }
        
        composable(NavigationDestinations.BUDGET_GOALS) {
            // Budget Goal Setup Screen
            BudgetWorkspaceHub(
                currencyFormatter = currencyFormatter
            )
        }
    }
}
