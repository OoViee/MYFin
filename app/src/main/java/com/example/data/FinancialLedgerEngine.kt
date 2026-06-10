package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

object FinancialLedgerEngine {

    const val TYPE_INCOME = "Income"
    const val TYPE_EXPENSE = "Expense"
    const val TYPE_DEBT_GIVEN = "Debt Given"
    const val TYPE_DEBT_RECEIVED = "Debt Received"
    const val TYPE_DEBT_REPAID = "Debt Repaid"
    const val TYPE_TRANSFER = "Transfer"
    const val TYPE_CREDIT_CARD_EXPENSE = "Credit Card Expense"
    const val TYPE_CREDIT_CARD_PAYMENT = "Credit Card Payment"
    const val TYPE_EMI_PAYMENT = "EMI Payment"
    const val TYPE_SETTLEMENT = "Settlement"
    const val TYPE_LOAN_DISBURSEMENT = "Loan Disbursement"
    const val TYPE_LOAN_REPAYMENT = "Loan Repayment"
    const val TYPE_INVESTMENT = "Investment"
    const val TYPE_REDEMPTION = "Redemption"

    // Helper to map dynamic account modes to standard accounts
    fun getPaymentMethodCategory(paymentMode: String): String {
        val mode = paymentMode.lowercase()
        return when {
            mode == "cash" -> "Cash"
            mode == "wallet" -> "Wallet"
            mode.contains("card") || mode.contains("upi") || mode.contains("bank") || mode.contains("transfer") || mode == "debit card" || mode == "net banking" -> "Bank"
            else -> "Bank" // default fallback
        }
    }

    /**
     * Calculates the net Available Money from standard formula:
     * Net Available Money = Cash + Bank + Wallet + Recoverable Debt - Outstanding Debt - Card Outstanding - Loan Obligations
     */
    fun computeNetAvailableMoney(
        ledgerEntries: List<UnifiedLedgerEntry>,
        recoverableDebt: Double, // youAreOwed from splits
        outstandingDebt: Double, // youOwe from splits
        cardOutstanding: Double,
        loanObligations: Double
    ): Double {
        // Starting Base balances for high-fidelity interactive simulation
        var cash = 12000.0
        var bank = 48000.0
        var wallet = 45000.0

        for (entry in ledgerEntries) {
            val amt = entry.amount
            when (entry.type) {
                TYPE_INCOME -> {
                    when (getPaymentMethodCategory(entry.paymentMode)) {
                        "Cash" -> cash += amt
                        "Wallet" -> wallet += amt
                        "Bank" -> bank += amt
                    }
                }
                TYPE_EXPENSE, TYPE_CREDIT_CARD_EXPENSE -> {
                    when (getPaymentMethodCategory(entry.paymentMode)) {
                        "Cash" -> cash -= amt
                        "Wallet" -> wallet -= amt
                        "Bank" -> bank -= amt
                        // Credit Card does not deduct immediately from asset accounts
                    }
                }
                TYPE_TRANSFER -> {
                    // sourceAccount -> destAccount
                    when (entry.sourceAccount) {
                        "Cash" -> cash -= amt
                        "Wallet" -> wallet -= amt
                        "Bank" -> bank -= amt
                    }
                    when (entry.destAccount) {
                        "Cash" -> cash += amt
                        "Wallet" -> wallet += amt
                        "Bank" -> bank += amt
                    }
                }
                TYPE_CREDIT_CARD_PAYMENT -> {
                    // Payment of Card bill from asset accounts
                    when (getPaymentMethodCategory(entry.paymentMode)) {
                        "Cash" -> cash -= amt
                        "Wallet" -> wallet -= amt
                        "Bank" -> bank -= amt
                    }
                }
                TYPE_EMI_PAYMENT, TYPE_LOAN_REPAYMENT, TYPE_INVESTMENT -> {
                    when (getPaymentMethodCategory(entry.paymentMode)) {
                        "Cash" -> cash -= amt
                        "Wallet" -> wallet -= amt
                        "Bank" -> bank -= amt
                    }
                }
                TYPE_REDEMPTION -> {
                    when (getPaymentMethodCategory(entry.paymentMode)) {
                        "Cash" -> cash += amt
                        "Wallet" -> wallet += amt
                        "Bank" -> bank += amt
                    }
                }
                TYPE_SETTLEMENT -> {
                    // settlements received or paid
                    if (entry.category == "Paid") {
                        when (getPaymentMethodCategory(entry.paymentMode)) {
                            "Cash" -> cash -= amt
                            "Wallet" -> wallet -= amt
                            "Bank" -> bank -= amt
                        }
                    } else {
                        when (getPaymentMethodCategory(entry.paymentMode)) {
                            "Cash" -> cash += amt
                            "Wallet" -> wallet += amt
                            "Bank" -> bank += amt
                        }
                    }
                }
            }
        }

        return cash + bank + wallet + recoverableDebt - outstandingDebt - cardOutstanding - loanObligations
    }
}
