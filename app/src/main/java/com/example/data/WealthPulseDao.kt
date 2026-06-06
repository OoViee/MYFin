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
}
