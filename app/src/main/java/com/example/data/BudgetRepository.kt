package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class BudgetRepository(private val dao: WealthPulseDao) {

    // Budget CRUD
    fun getAllBudgets(userId: String): Flow<List<BudgetEntity>> = dao.getAllBudgets(userId)

    suspend fun getBudgetById(id: Int): BudgetEntity? = dao.getBudgetById(id)

    suspend fun insertBudget(budget: BudgetEntity) = dao.insertBudget(budget)

    suspend fun softDeleteBudget(id: Int) = dao.softDeleteBudget(id)

    suspend fun toggleBudgetActiveState(id: Int, isActive: Boolean) = dao.toggleBudgetActiveState(id, isActive)

    suspend fun clearBudgets() = dao.clearBudgets()

    // Retrieve category-wise spending for specified interval
    fun getCategorySpendingForPeriod(userId: String, startDate: Long, endDate: Long): Flow<List<CategorySpending>> {
        return dao.getCategorySpendingForPeriod(userId, startDate, endDate)
    }

    // Budget Progress aggregations
    fun getBudgetProgressList(userId: String, startDate: Long, endDate: Long): Flow<List<BudgetProgress>> {
        return combine(
            dao.getAllBudgets(userId),
            dao.getCategorySpendingForPeriod(userId, startDate, endDate),
            dao.getAllDailyExpenses()
        ) { budgets, spentList, allExpenses ->
            val spentMap = spentList.associate { it.category to it.totalSpent }
            
            budgets.filter { it.isActive && !it.isDeleted }.map { budget ->
                val spent = spentMap[budget.category] ?: 0.0
                val remaining = budget.budgetAmount - spent
                val ratio = if (budget.budgetAmount > 0) (spent / budget.budgetAmount) * 100.0 else 0.0
                
                // Fetch limit 10 contributing recent entries for details
                val recent = allExpenses.filter {
                    it.userId == userId && !it.isDeleted && 
                    it.category.equals(budget.category, ignoreCase = true) &&
                    it.timestamp >= startDate && it.timestamp <= endDate
                }.sortedByDescending { it.timestamp }.take(10)

                BudgetProgress(
                    budget = budget,
                    spentAmount = spent,
                    remainingAmount = remaining,
                    usagePercentage = ratio,
                    recentExpenses = recent
                )
            }
        }
    }

    // --- Insights & Aggregations Preparation ---
    fun getHighestSpendingCategory(userId: String, startDate: Long, endDate: Long): Flow<String?> {
        return dao.getCategorySpendingForPeriod(userId, startDate, endDate).map { spentList ->
            spentList.maxByOrNull { it.totalSpent }?.category
        }
    }

    fun getMostExceededBudget(userId: String, startDate: Long, endDate: Long): Flow<Pair<String, Double>?> {
        return combine(
            dao.getAllBudgets(userId),
            dao.getCategorySpendingForPeriod(userId, startDate, endDate)
        ) { budgets, spentList ->
            val spentMap = spentList.associate { it.category to it.totalSpent }
            budgets.filter { it.isActive && !it.isDeleted }
                .mapNotNull { budget ->
                    val spent = spentMap[budget.category] ?: 0.0
                    if (spent > budget.budgetAmount) {
                        Pair(budget.category, spent - budget.budgetAmount)
                    } else null
                }
                .maxByOrNull { it.second }
        }
    }

    fun getRemainingBudgetTotals(userId: String, startDate: Long, endDate: Long): Flow<Double> {
        return combine(
            dao.getAllBudgets(userId),
            dao.getCategorySpendingForPeriod(userId, startDate, endDate)
        ) { budgets, spentList ->
            val spentMap = spentList.associate { it.category to it.totalSpent }
            budgets.filter { it.isActive && !it.isDeleted }
                .sumOf { budget ->
                    val spent = spentMap[budget.category] ?: 0.0
                    maxOf(0.0, budget.budgetAmount - spent)
                }
        }
    }

    fun getMonthlyBudgetSummary(userId: String, startDate: Long, endDate: Long): Flow<MonthlyBudgetSummary> {
        return combine(
            dao.getAllBudgets(userId),
            dao.getCategorySpendingForPeriod(userId, startDate, endDate)
        ) { budgets, spentList ->
            val spentMap = spentList.associate { it.category to it.totalSpent }
            val activeBudgets = budgets.filter { it.isActive && !it.isDeleted }
            
            var totalBudget = 0.0
            var totalSpent = 0.0
            var totalRemaining = 0.0
            var totalExceeded = 0.0
            
            activeBudgets.forEach { budget ->
                val spent = spentMap[budget.category] ?: 0.0
                totalBudget += budget.budgetAmount
                totalSpent += spent
                
                if (spent > budget.budgetAmount) {
                    totalExceeded += (spent - budget.budgetAmount)
                } else {
                    totalRemaining += (budget.budgetAmount - spent)
                }
            }

            MonthlyBudgetSummary(
                totalBudgetLimit = totalBudget,
                totalSpent = totalSpent,
                remainingAmount = totalRemaining,
                exceededAmount = totalExceeded,
                activeBudgetsCount = activeBudgets.size
            )
        }
    }
}

data class BudgetProgress(
    val budget: BudgetEntity,
    val spentAmount: Double,
    val remainingAmount: Double,
    val usagePercentage: Double,
    val recentExpenses: List<DailyExpenseEntity>
)

data class MonthlyBudgetSummary(
    val totalBudgetLimit: Double,
    val totalSpent: Double,
    val remainingAmount: Double,
    val exceededAmount: Double,
    val activeBudgetsCount: Int
)
