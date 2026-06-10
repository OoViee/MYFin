package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WealthPulseDao {

    // Daily Expenses
    @Query("SELECT * FROM daily_expenses ORDER BY timestamp DESC")
    fun getAllDailyExpenses(): Flow<List<DailyExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyExpense(expense: DailyExpenseEntity)

    @Query("DELETE FROM daily_expenses WHERE id = :id")
    suspend fun deleteDailyExpense(id: Int)

    @Query("DELETE FROM daily_expenses")
    suspend fun clearDailyExpenses()

    // Credit Expenses
    @Query("SELECT * FROM credit_expenses ORDER BY timestamp DESC")
    fun getAllCreditExpenses(): Flow<List<CreditExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditExpense(expense: CreditExpenseEntity)

    @Query("DELETE FROM credit_expenses WHERE id = :id")
    suspend fun deleteCreditExpense(id: Int)

    @Query("DELETE FROM credit_expenses")
    suspend fun clearCreditExpenses()

    // EMI Loans
    @Query("SELECT * FROM emi_loans ORDER BY timestamp DESC")
    fun getAllEmiLoans(): Flow<List<EmiLoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmiLoan(emi: EmiLoanEntity)

    @Query("DELETE FROM emi_loans WHERE id = :id")
    suspend fun deleteEmiLoan(id: Int)

    @Query("DELETE FROM emi_loans")
    suspend fun clearEmiLoans()

    // Debt Splits
    @Query("SELECT * FROM debt_splits ORDER BY timestamp DESC")
    fun getAllDebtSplits(): Flow<List<DebtSplitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtSplit(debt: DebtSplitEntity)

    @Query("DELETE FROM debt_splits WHERE id = :id")
    suspend fun deleteDebtSplit(id: Int)

    @Query("DELETE FROM debt_splits")
    suspend fun clearDebtSplits()

    // Income Paydays
    @Query("SELECT * FROM income_paydays ORDER BY timestamp DESC")
    fun getAllIncomePaydays(): Flow<List<IncomePaydayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomePayday(income: IncomePaydayEntity)

    @Query("DELETE FROM income_paydays WHERE id = :id")
    suspend fun deleteIncomePayday(id: Int)

    @Query("DELETE FROM income_paydays")
    suspend fun clearIncomePaydays()

    // SIP Records
    @Query("SELECT * FROM sip_records ORDER BY timestamp DESC")
    fun getAllSipRecords(): Flow<List<SipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSipRecord(sip: SipEntity)

    @Query("DELETE FROM sip_records WHERE id = :id")
    suspend fun deleteSipRecord(id: Int)

    @Query("DELETE FROM sip_records")
    suspend fun clearSipRecords()

    // Investment Records
    @Query("SELECT * FROM investment_records ORDER BY timestamp DESC")
    fun getAllInvestmentRecords(): Flow<List<InvestmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestmentRecord(investment: InvestmentEntity)

    @Query("DELETE FROM investment_records WHERE id = :id")
    suspend fun deleteInvestmentRecord(id: Int)

    @Query("DELETE FROM investment_records")
    suspend fun clearInvestmentRecords()

    // Credit Cards
    @Query("SELECT * FROM credit_cards ORDER BY id ASC")
    fun getAllCreditCards(): Flow<List<CreditCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteCreditCard(id: Int)

    @Query("DELETE FROM credit_cards")
    suspend fun clearCreditCards()

    // Trip/Events
    @Query("SELECT * FROM trip_events ORDER BY timestamp DESC")
    fun getAllTripEvents(): Flow<List<TripEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripEvent(trip: TripEventEntity)

    @Query("DELETE FROM trip_events WHERE id = :id")
    suspend fun deleteTripEvent(id: Int)

    @Query("DELETE FROM trip_events")
    suspend fun clearTripEvents()

    // Trip/Group Expenses
    @Query("SELECT * FROM trip_expenses ORDER BY timestamp DESC")
    fun getAllTripExpenses(): Flow<List<TripExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripExpense(expense: TripExpenseEntity)

    @Query("DELETE FROM trip_expenses WHERE id = :id")
    suspend fun deleteTripExpense(id: Int)

    @Query("DELETE FROM trip_expenses")
    suspend fun clearTripExpenses()

    // Dynamic Participants
    @Query("SELECT * FROM participants ORDER BY name ASC")
    fun getAllParticipants(): Flow<List<ParticipantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ParticipantEntity)

    @Query("DELETE FROM participants WHERE id = :id")
    suspend fun deleteParticipant(id: Int)

    @Query("DELETE FROM participants WHERE name = :name")
    suspend fun deleteParticipantByName(name: String)

    @Query("DELETE FROM participants")
    suspend fun clearParticipants()

    // --- Budget Management 3.0 ---
    @Query("SELECT * FROM budgets WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllBudgets(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    suspend fun getBudgetById(id: Int): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Query("UPDATE budgets SET isDeleted = 1, isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteBudget(id: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE budgets SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleBudgetActiveState(id: Int, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM budgets")
    suspend fun clearBudgets()

    @Query("""
        SELECT category, SUM(amount) as totalSpent 
        FROM daily_expenses 
        WHERE userId = :userId AND isDeleted = 0 AND timestamp >= :startDate AND timestamp <= :endDate 
        GROUP BY category
    """)
    fun getCategorySpendingForPeriod(userId: String, startDate: Long, endDate: Long): Flow<List<CategorySpending>>

    @Query("""
        SELECT SUM(amount) 
        FROM daily_expenses 
        WHERE userId = :userId AND isDeleted = 0 AND category = :category AND timestamp >= :startDate AND timestamp <= :endDate
    """)
    fun getSpentAmountForCategory(userId: String, category: String, startDate: Long, endDate: Long): Flow<Double?>

    // --- Credit Card Management Pro (Stage 4) ---
    @Query("SELECT * FROM credit_cards WHERE userId = :userId ORDER BY cardName ASC")
    fun getAllCreditCardsForUser(userId: String): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    suspend fun getCreditCardById(id: Int): CreditCardEntity?

    @Query("SELECT * FROM daily_expenses WHERE cardId = :cardId AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getExpensesForCard(cardId: Int): Flow<List<DailyExpenseEntity>>

    @Query("SELECT * FROM card_statements WHERE cardId = :cardId ORDER BY statementEndDate DESC")
    fun getStatementsForCard(cardId: Int): Flow<List<CardStatementEntity>>

    @Query("SELECT * FROM card_statements WHERE id = :id LIMIT 1")
    suspend fun getStatementById(id: Int): CardStatementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardStatement(statement: CardStatementEntity)

    @Query("DELETE FROM card_statements WHERE id = :id")
    suspend fun deleteCardStatement(id: Int)

    @Query("SELECT * FROM card_emis WHERE cardId = :cardId ORDER BY createdAt DESC")
    fun getEMIsForCard(cardId: Int): Flow<List<CardEMIEntity>>

    @Query("SELECT * FROM card_emis WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllEMIs(userId: String): Flow<List<CardEMIEntity>>

    @Query("SELECT * FROM card_emis WHERE id = :id LIMIT 1")
    suspend fun getEMIById(id: Int): CardEMIEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardEMI(emi: CardEMIEntity)

    @Query("DELETE FROM card_emis WHERE id = :id")
    suspend fun deleteCardEMI(id: Int)

    @Query("SELECT * FROM card_payments WHERE cardId = :cardId ORDER BY paymentDate DESC")
    fun getPaymentsForCard(cardId: Int): Flow<List<CardPaymentEntity>>

    @Query("SELECT * FROM card_payments WHERE userId = :userId ORDER BY paymentDate DESC")
    fun getAllPayments(userId: String): Flow<List<CardPaymentEntity>>

    @Query("SELECT * FROM card_payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: Int): CardPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardPayment(payment: CardPaymentEntity)

    @Query("DELETE FROM card_payments WHERE id = :id")
    suspend fun deleteCardPayment(id: Int)
}

data class CategorySpending(
    val category: String,
    val totalSpent: Double
)
