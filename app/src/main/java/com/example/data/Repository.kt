package com.example.data

import kotlinx.coroutines.flow.Flow

class Repository(private val dao: WealthPulseDao) {

    // Daily Expenses
    val allDailyExpenses: Flow<List<DailyExpenseEntity>> = dao.getAllDailyExpenses()
    suspend fun insertDailyExpense(expense: DailyExpenseEntity) = dao.insertDailyExpense(expense)
    suspend fun deleteDailyExpense(id: Int) = dao.deleteDailyExpense(id)
    suspend fun clearDailyExpenses() = dao.clearDailyExpenses()

    // Credit Expenses
    val allCreditExpenses: Flow<List<CreditExpenseEntity>> = dao.getAllCreditExpenses()
    suspend fun insertCreditExpense(expense: CreditExpenseEntity) = dao.insertCreditExpense(expense)
    suspend fun deleteCreditExpense(id: Int) = dao.deleteCreditExpense(id)
    suspend fun clearCreditExpenses() = dao.clearCreditExpenses()

    // EMI Loans
    val allEmiLoans: Flow<List<EmiLoanEntity>> = dao.getAllEmiLoans()
    suspend fun insertEmiLoan(emi: EmiLoanEntity) = dao.insertEmiLoan(emi)
    suspend fun deleteEmiLoan(id: Int) = dao.deleteEmiLoan(id)
    suspend fun clearEmiLoans() = dao.clearEmiLoans()

    // Debt Splits
    val allDebtSplits: Flow<List<DebtSplitEntity>> = dao.getAllDebtSplits()
    suspend fun insertDebtSplit(debt: DebtSplitEntity) = dao.insertDebtSplit(debt)
    suspend fun deleteDebtSplit(id: Int) = dao.deleteDebtSplit(id)
    suspend fun clearDebtSplits() = dao.clearDebtSplits()

    // Income Paydays
    val allIncomePaydays: Flow<List<IncomePaydayEntity>> = dao.getAllIncomePaydays()
    suspend fun insertIncomePayday(income: IncomePaydayEntity) = dao.insertIncomePayday(income)
    suspend fun deleteIncomePayday(id: Int) = dao.deleteIncomePayday(id)
    suspend fun clearIncomePaydays() = dao.clearIncomePaydays()

    // SIP Records
    val allSipRecords: Flow<List<SipEntity>> = dao.getAllSipRecords()
    suspend fun insertSipRecord(sip: SipEntity) = dao.insertSipRecord(sip)
    suspend fun deleteSipRecord(id: Int) = dao.deleteSipRecord(id)
    suspend fun clearSipRecords() = dao.clearSipRecords()

    // Investment Records
    val allInvestmentRecords: Flow<List<InvestmentEntity>> = dao.getAllInvestmentRecords()
    suspend fun insertInvestmentRecord(investment: InvestmentEntity) = dao.insertInvestmentRecord(investment)
    suspend fun deleteInvestmentRecord(id: Int) = dao.deleteInvestmentRecord(id)
    suspend fun clearInvestmentRecords() = dao.clearInvestmentRecords()

    // Credit Cards
    val allCreditCards: Flow<List<CreditCardEntity>> = dao.getAllCreditCards()
    suspend fun insertCreditCard(card: CreditCardEntity) = dao.insertCreditCard(card)
    suspend fun deleteCreditCard(id: Int) = dao.deleteCreditCard(id)
    suspend fun clearCreditCards() = dao.clearCreditCards()

    // Trip Events
    val allTripEvents: Flow<List<TripEventEntity>> = dao.getAllTripEvents()
    suspend fun insertTripEvent(trip: TripEventEntity) = dao.insertTripEvent(trip)
    suspend fun deleteTripEvent(id: Int) = dao.deleteTripEvent(id)
    suspend fun clearTripEvents() = dao.clearTripEvents()

    // Trip Expenses
    val allTripExpenses: Flow<List<TripExpenseEntity>> = dao.getAllTripExpenses()
    suspend fun insertTripExpense(expense: TripExpenseEntity) = dao.insertTripExpense(expense)
    suspend fun deleteTripExpense(id: Int) = dao.deleteTripExpense(id)
    suspend fun clearTripExpenses() = dao.clearTripExpenses()

    // Participants
    val allParticipants: Flow<List<ParticipantEntity>> = dao.getAllParticipants()
    suspend fun insertParticipant(participant: ParticipantEntity) = dao.insertParticipant(participant)
    suspend fun deleteParticipant(id: Int) = dao.deleteParticipant(id)
    suspend fun deleteParticipantByName(name: String) = dao.deleteParticipantByName(name)
    suspend fun clearParticipants() = dao.clearParticipants()
}
