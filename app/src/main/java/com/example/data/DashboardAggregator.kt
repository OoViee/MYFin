package com.example.data

/**
 * DashboardAggregator is structured to compute metrics, cash flow trends,
 * and high-fidelity insights from multiple data repository models.
 * This ensures the view model receives a polished, sanitized view representation
 * of the financial states.
 */
class DashboardAggregator {
    companion object {
        fun computeDisposableCash(income: Double, expenses: Double, dueBills: Double): Double {
            return income - expenses - dueBills
        }

        fun selectCategoryIcon(categoryName: String): String {
            return when (categoryName.lowercase()) {
                "food", "food & dining", "dining" -> "🍔"
                "transport", "travel", "fuel" -> "🚗"
                "shopping" -> "🛍️"
                "utilities", "rent & utilities" -> "💡"
                "debts & splits", "splits" -> "👥"
                "sip", "sip mutual funds" -> "📈"
                "portfolio", "investments" -> "💼"
                "payday", "income" -> "💰"
                else -> "💸"
            }
        }
    }
}
