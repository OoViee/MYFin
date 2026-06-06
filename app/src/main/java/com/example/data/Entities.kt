package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_expenses")
data class DailyExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String,
    val description: String,
    val category: String,
    val paymentMode: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "credit_expenses")
data class CreditExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String,
    val description: String,
    val category: String,
    val cardName: String,
    val isEmiConversion: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emi_loans")
data class EmiLoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String,
    val description: String,
    val category: String,
    val totalTenureMonths: Int,
    val remainingMonths: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_splits")
data class DebtSplitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String,
    val description: String,
    val category: String,
    val paymentMode: String,
    val debtPersonInvolved: String,
    val isGroupSplit: Boolean,
    val groupName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "income_paydays")
data class IncomePaydayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String,
    val description: String,
    val category: String,
    val incomeFrequency: String, // Monthly, One-off, Freelance
    val paymentMode: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sip_records")
data class SipEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String,
    val description: String,
    val frequency: String, // Monthly, Weekly, etc.
    val investmentCategory: String, // Mutual Funds, Gold, etc.
    val dayOfMonth: Int, // Day on which monthly SIP auto-debits
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "investment_records")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double, // Invested Principal Amount
    val currency: String,
    val description: String, // e.g. "INFY Stock" or "Digital Gold"
    val category: String, // Equity, Mutual Funds, Fixed Deposits, Gold, Crypto
    val currentValue: Double, // Real-time or latest valuation
    val timestamp: Long = System.currentTimeMillis()
)

