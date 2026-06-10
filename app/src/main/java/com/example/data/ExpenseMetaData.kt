package com.example.data

import androidx.compose.ui.graphics.Color

data class ExpenseCategory(
    val name: String,
    val icon: String, // Emoji representation
    val colorHex: String, // Hex color code
    val isActive: Boolean = true
) {
    val composeColor: Color
        get() = Color(android.graphics.Color.parseColor(colorHex))
}

object ExpenseMetaData {
    val Categories = listOf(
        ExpenseCategory("Food", "🍔", "#FF5722"), // Deep Orange
        ExpenseCategory("Travel", "✈️", "#2196F3"), // Blue
        ExpenseCategory("Fuel", "⛽", "#FFEB3B"), // Yellow
        ExpenseCategory("Rent", "🏠", "#9C27B0"), // Purple
        ExpenseCategory("Utilities", "💡", "#00BCD4"), // Cyan
        ExpenseCategory("Entertainment", "🎬", "#E91E63"), // Pink
        ExpenseCategory("Shopping", "🛍️", "#4CAF50"), // Green
        ExpenseCategory("Medical", "💊", "#F44336"), // Red
        ExpenseCategory("Education", "📚", "#3F51B5"), // Indigo
        ExpenseCategory("Investment", "📈", "#009688"), // Teal
        ExpenseCategory("Insurance", "🛡️", "#607D8B"), // Blue Grey
        ExpenseCategory("Subscriptions", "🎟️", "#795548"), // Brown
        ExpenseCategory("Personal Care", "🧼", "#FFC107"), // Amber
        ExpenseCategory("Business", "💼", "#333333"), // Charcoal
        ExpenseCategory("Others", "💸", "#9E9E9E") // Grey
    )

    val PaymentMethods = listOf(
        "Cash",
        "UPI",
        "Credit Card",
        "Debit Card",
        "Net Banking",
        "Wallet",
        "Bank Transfer",
        "Cheque",
        "Other"
    )

    val Tags = listOf(
        "#office",
        "#vacation",
        "#trip",
        "#family",
        "#medical",
        "#bills",
        "#groceries",
        "#leisure"
    )
}
