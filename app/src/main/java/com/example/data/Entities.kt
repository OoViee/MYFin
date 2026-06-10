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
    val userId: String = "guest",
    val timestamp: Long = System.currentTimeMillis(),
    
    // Stage 2 fields supporting Expense 2.0
    val notes: String = "",
    val receiptImageUri: String = "",
    val tags: String = "", // Comma-separated list of tags (e.g. "#office,#trip")
    val dateString: String = "", // formatted "YYYY-MM-DD"
    val timeString: String = "", // formatted "HH:mm"
    val isRecurring: Boolean = false,
    val recurringPeriod: String = "None", // "None", "Daily", "Weekly", "Monthly", "Yearly"
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Stage 4 Credit Card field
    val cardId: Int = 0
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
    val userId: String = "guest",
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
    val userId: String = "guest",
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
    val paidPeople: String = "", // Comma-separated list of people who completed payment
    val userId: String = "guest",
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
    val userId: String = "guest",
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
    val userId: String = "guest",
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
    val userId: String = "guest",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardName: String,
    val creditLimit: Double,
    val billDate: Int, // e.g. 15 for 15th of month
    val billStatus: String = "Pending", // "Pending" or "Paid"
    val outstandingAmount: Double = 0.0,
    val userId: String = "guest",
    val bankName: String = "",
    val cardType: String = "Standard", // Standard, Premium, Cashback, Fuel, etc.
    val cardNetwork: String = "Visa", // Visa, Mastercard, RuPay, American Express, Diners Club, Other
    val lastFourDigits: String = "",
    val billingDate: Int = billDate,
    val dueDate: Int = 5, // e.g., 5th of month
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "trip_events")
data class TripEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val isPublic: Boolean = false,
    val participants: String, // Comma-separated list of participant names
    val userId: String = "guest",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "trip_expenses")
data class TripExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val title: String,
    val totalAmount: Double,
    val paidBy: String, // Spender name (can be multiple or just single name, e.g. "Amit")
    val splitMethod: String, // "EQUAL", "PERCENT", "EXACT", "SHARE"
    val participantWeights: String, // Comma-separated weights matching involvedParticipants
    val involvedParticipants: String, // Comma-separated list of names involved
    val category: String,
    val notes: String = "",
    val receiptUri: String = "", // Simulated receipt/invoice URI/filename
    val userId: String = "guest",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "participants")
data class ParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String = "",
    val isRegistered: Boolean = true,
    val userId: String = "guest"
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val budgetAmount: Double,
    val startDate: Long,
    val endDate: Long,
    val periodType: String = "Monthly", // "Monthly", "Weekly", "Yearly"
    val isActive: Boolean = true,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "guest"
)

@Entity(tableName = "card_statements")
data class CardStatementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardId: Int,
    val statementStartDate: Long,
    val statementEndDate: Long,
    val statementAmount: Double,
    val minimumDue: Double,
    val paymentDueDate: Long,
    val paymentStatus: String = "Unpaid", // Unpaid, Paid, Partially Paid
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "guest"
)

@Entity(tableName = "card_emis")
data class CardEMIEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardId: Int,
    val purchaseAmount: Double,
    val purchaseDate: Long,
    val tenureMonths: Int,
    val emiAmount: Double,
    val remainingInstallments: Int,
    val description: String,
    val status: String = "Active", // Active, Completed, Upcoming
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "guest"
)

@Entity(tableName = "card_payments")
data class CardPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardId: Int,
    val amount: Double,
    val paymentDate: Long,
    val sourceAccount: String,
    val notes: String = "",
    val statementId: Int = 0, // 0 if none
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = "guest"
)

