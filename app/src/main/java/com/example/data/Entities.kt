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
    val syncStatus: String = "SYNCED",
    
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
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = timestamp,
    val updatedAt: Long = timestamp,
    val isDeleted: Boolean = false,
    val syncStatus: String = "SYNCED"
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

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val loanName: String,
    val loanType: String, // Home Loan, Personal Loan, Car Loan, Bike Loan, Education Loan, Gold Loan, Consumer Loan, Other
    val lenderName: String,
    val principalAmount: Double,
    val interestRate: Double,
    val tenureMonths: Int,
    val startDate: Long,
    val emiAmount: Double,
    val outstandingBalance: Double,
    val status: String = "Active", // "Active", "Closed"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "guest"
)

@Entity(tableName = "loan_schedules")
data class LoanScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val loanId: Int,
    val installmentNumber: Int,
    val dueDate: Long,
    val emiAmount: Double,
    val principalComponent: Double,
    val interestComponent: Double,
    val paymentStatus: String, // "Pending", "Paid", "Overdue", "Prepaid"
    val userId: String = "guest"
)

@Entity(tableName = "loan_payments")
data class LoanPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val loanId: Int,
    val installmentNumber: Int, // 0 if prepayment / foreclosure
    val paymentDate: Long,
    val amountPaid: Double,
    val paymentMethod: String,
    val notes: String = "",
    val paymentType: String, // "Regular", "Prepayment", "Foreclosure"
    val userId: String = "guest"
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val description: String,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis(),
    val groupType: String, // "Trip", "Home", "Sports", "Other"
    val status: String = "Active", // "Active", "Settled"
    val userId: String = "guest"
)

@Entity(tableName = "group_members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val memberName: String,
    val phoneOptional: String = "",
    val emailOptional: String = "",
    val colorIdentifier: Int = 0, // for avatar color styling
    val userId: String = "guest"
)

@Entity(tableName = "split_expenses")
data class SplitExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val title: String,
    val amount: Double,
    val paidBy: String, // Name of member who paid
    val expenseDate: Long = System.currentTimeMillis(),
    val category: String, // Food, Travel, Fuel, Hotel, etc.
    val notes: String = "",
    val splitType: String = "EQUAL", // "EQUAL", "EXACT", "PERCENT", "SHARE"
    val participantShares: String = "", // comma-separated values (e.g. "1,1,1" or "500,1000")
    val involvedMembers: String = "", // comma-separated name list (e.g. "Manu,Veena,Amit")
    val receiptUri: String = "", // for attachments local URI
    val userId: String = "guest"
)

@Entity(tableName = "group_settlements")
data class SettlementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val payer: String,
    val receiver: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val userId: String = "guest"
)

@Entity(tableName = "group_balances")
data class BalanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val memberName: String,
    val netBalance: Double,
    val userId: String = "guest"
)

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val location: String,
    val eventType: String, // Trip, Vacation, Tournament, Party, Office Event, Family Function, Wedding, Custom
    val status: String, // Active, Completed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val groupId: Int, // backing Split Group ID
    val userId: String = "guest"
)

@Entity(tableName = "trip_participants")
data class TripParticipantEntity(
    @PrimaryKey(autoGenerate = true) val participantId: Int = 0,
    val tripId: Int,
    val name: String,
    val role: String // Organizer, Participant, Viewer
)

@Entity(tableName = "unified_ledger_entries")
data class UnifiedLedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String = "INR",
    val description: String,
    val type: String, // "Income", "Expense", "Debt Given", "Debt Received", "Debt Repaid", "Transfer", "Credit Card Expense", "Credit Card Payment", "EMI Payment", "Settlement", "Loan Disbursement", "Loan Repayment", "Investment", "Redemption"
    val category: String, // e.g. "Food", "Salary", "Interest Component", "Principal Component"
    val paymentMode: String = "Other", // "Cash", "UPI", "Credit Card", "Debit Card", etc.
    val sourceAccount: String = "", // e.g. "Bank"
    val destAccount: String = "", // e.g. "Wallet" (used for Transfers)
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: String = "", // e.g., "daily_12", "cc_payment_5", "loan_payment_ui"
    val userId: String = "guest"
)





