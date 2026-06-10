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

    @Query("SELECT * FROM daily_expenses ORDER BY timestamp DESC")
    suspend fun getAllDailyExpensesDirect(): List<DailyExpenseEntity>

    @Query("SELECT * FROM daily_expenses WHERE id = :id")
    suspend fun getDailyExpenseByIdDirect(id: Int): DailyExpenseEntity?

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

    @Query("SELECT * FROM income_paydays ORDER BY timestamp DESC")
    suspend fun getAllIncomePaydaysDirect(): List<IncomePaydayEntity>

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

    // Stage 5: Loan & EMI Queries
    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllLoans(userId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getLoanById(id: Int): LoanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteLoan(id: Int)

    @Query("SELECT * FROM loan_schedules WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    fun getSchedulesForLoan(loanId: Int): Flow<List<LoanScheduleEntity>>

    @Query("SELECT * FROM loan_schedules WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    suspend fun getSchedulesForLoanSync(loanId: Int): List<LoanScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanSchedules(schedules: List<LoanScheduleEntity>)

    @Query("DELETE FROM loan_schedules WHERE loanId = :loanId")
    suspend fun deleteSchedulesForLoan(loanId: Int)

    @Query("SELECT * FROM loan_payments WHERE loanId = :loanId ORDER BY paymentDate DESC")
    fun getPaymentsForLoan(loanId: Int): Flow<List<LoanPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanPayment(payment: LoanPaymentEntity): Long

    @Query("DELETE FROM loan_payments WHERE id = :id")
    suspend fun deleteLoanPayment(id: Int)

    // For Calendar: fetch all schedules for user in ascending order of due date
    @Query("SELECT * FROM loan_schedules WHERE userId = :userId ORDER BY dueDate ASC")
    fun getAllSchedulesForUser(userId: String): Flow<List<LoanScheduleEntity>>

    // For Alerts / Home: nearest upcoming pending EMI
    @Query("SELECT * FROM loan_schedules WHERE userId = :userId AND paymentStatus != 'Paid' AND paymentStatus != 'Completed' AND paymentStatus != 'Prepaid' ORDER BY dueDate ASC")
    fun getUpcomingPendingSchedules(userId: String): Flow<List<LoanScheduleEntity>>

    // Stage 6 Group Splitwise Expense & Debts
    @Query("SELECT * FROM `groups` WHERE userId = :userId ORDER BY updatedDate DESC")
    fun getAllGroups(userId: String = "guest"): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Query("DELETE FROM `groups` WHERE id = :id")
    suspend fun deleteGroup(id: Int)

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun getMembersForGroup(groupId: Int): Flow<List<MemberEntity>>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    suspend fun getMembersForGroupSync(groupId: Int): List<MemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteMembersForGroup(groupId: Int)

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY expenseDate DESC")
    fun getExpensesForGroup(groupId: Int): Flow<List<SplitExpenseEntity>>

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY expenseDate DESC")
    suspend fun getExpensesForGroupSync(groupId: Int): List<SplitExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitExpense(expense: SplitExpenseEntity): Long

    @Query("DELETE FROM split_expenses WHERE id = :id")
    suspend fun deleteSplitExpense(id: Int)

    @Query("SELECT * FROM group_settlements WHERE groupId = :groupId ORDER BY date DESC")
    fun getSettlementsForGroup(groupId: Int): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM group_settlements WHERE groupId = :groupId ORDER BY date DESC")
    suspend fun getSettlementsForGroupSync(groupId: Int): List<SettlementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity): Long

    @Query("DELETE FROM group_settlements WHERE id = :id")
    suspend fun deleteSettlement(id: Int)

    @Query("SELECT * FROM group_balances WHERE groupId = :groupId")
    fun getBalancesForGroup(groupId: Int): Flow<List<BalanceEntity>>

    @Query("SELECT * FROM group_balances")
    fun getAllGroupBalances(): Flow<List<BalanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalances(balances: List<BalanceEntity>)

    @Query("DELETE FROM group_balances WHERE groupId = :groupId")
    suspend fun deleteBalancesForGroup(groupId: Int)

    // Stage 7 Trip & Event Management
    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getAllTrips(userId: String = "guest"): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id")
    fun getTripById(id: Int): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripByIdSync(id: Int): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTrip(id: Int)

    @Query("SELECT * FROM trip_participants WHERE tripId = :tripId ORDER BY name ASC")
    fun getParticipantsForTrip(tripId: Int): Flow<List<TripParticipantEntity>>

    @Query("SELECT * FROM trip_participants WHERE tripId = :tripId ORDER BY name ASC")
    suspend fun getParticipantsForTripSync(tripId: Int): List<TripParticipantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripParticipants(participants: List<TripParticipantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripParticipant(participant: TripParticipantEntity): Long

    @Query("DELETE FROM trip_participants WHERE tripId = :tripId")
    suspend fun deleteParticipantsForTrip(tripId: Int)

    @Query("DELETE FROM trip_participants WHERE participantId = :participantId")
    suspend fun deleteTripParticipant(participantId: Int)

    // Unified Financial Ledger Queries
    @Query("SELECT * FROM unified_ledger_entries ORDER BY timestamp DESC")
    fun getAllUnifiedLedgerEntries(): Flow<List<UnifiedLedgerEntry>>

    @Query("SELECT * FROM unified_ledger_entries ORDER BY timestamp DESC")
    suspend fun getAllUnifiedLedgerEntriesSync(): List<UnifiedLedgerEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnifiedLedgerEntry(entry: UnifiedLedgerEntry): Long

    @Query("DELETE FROM unified_ledger_entries WHERE id = :id")
    suspend fun deleteUnifiedLedgerEntry(id: Int)

    @Query("DELETE FROM unified_ledger_entries WHERE referenceId = :referenceId")
    suspend fun deleteUnifiedLedgerEntryByReferenceId(referenceId: String)

    @Query("DELETE FROM unified_ledger_entries")
    suspend fun clearUnifiedLedgerEntries()
}

data class CategorySpending(
    val category: String,
    val totalSpent: Double
)
