package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Repository(private val dao: WealthPulseDao) {

    // Daily Expenses
    val allDailyExpenses: Flow<List<DailyExpenseEntity>> = dao.getAllDailyExpenses()
    suspend fun insertDailyExpense(expense: DailyExpenseEntity) = dao.insertDailyExpense(expense)
    suspend fun deleteDailyExpense(id: Int) = dao.deleteDailyExpense(id)
    suspend fun clearDailyExpenses() = dao.clearDailyExpenses()
    
    // Expense Analytics Helpers (Stage 2)
    fun getMonthlyTotal(userId: String): Flow<Double> {
        return dao.getAllDailyExpenses().map { list ->
            val cal = java.util.Calendar.getInstance()
            val currentYear = cal.get(java.util.Calendar.YEAR)
            val currentMonth = cal.get(java.util.Calendar.MONTH)
            list.filter { 
                it.userId == userId && !it.isDeleted && 
                java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }.run {
                    get(java.util.Calendar.YEAR) == currentYear && get(java.util.Calendar.MONTH) == currentMonth
                }
            }.sumOf { it.amount }
        }
    }

    fun getCategoryTotal(userId: String, category: String): Flow<Double> {
        return dao.getAllDailyExpenses().map { list ->
            list.filter { it.userId == userId && !it.isDeleted && it.category.equals(category, ignoreCase = true) }
                .sumOf { it.amount }
        }
    }

    fun getWeeklyTotal(userId: String): Flow<Double> {
        return dao.getAllDailyExpenses().map { list ->
            val cal = java.util.Calendar.getInstance()
            val currentYear = cal.get(java.util.Calendar.YEAR)
            val currentWeek = cal.get(java.util.Calendar.WEEK_OF_YEAR)
            list.filter { 
                it.userId == userId && !it.isDeleted && 
                java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }.run {
                    get(java.util.Calendar.YEAR) == currentYear && get(java.util.Calendar.WEEK_OF_YEAR) == currentWeek
                }
            }.sumOf { it.amount }
        }
    }

    fun getYearlyTotal(userId: String): Flow<Double> {
        return dao.getAllDailyExpenses().map { list ->
            val cal = java.util.Calendar.getInstance()
            val currentYear = cal.get(java.util.Calendar.YEAR)
            list.filter { 
                it.userId == userId && !it.isDeleted && 
                java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(java.util.Calendar.YEAR) == currentYear
            }.sumOf { it.amount }
        }
    }

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

    // Budgets
    val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets("guest")

    // Stage 5 Loans
    val allLoans: Flow<List<LoanEntity>> = dao.getAllLoans("guest")
    val allSchedules: Flow<List<LoanScheduleEntity>> = dao.getAllSchedulesForUser("guest")

    // Stage 6 Group Splitwise & Debt Management
    val allGroups: Flow<List<GroupEntity>> = dao.getAllGroups("guest")
    val allGroupBalances: Flow<List<BalanceEntity>> = dao.getAllGroupBalances()

    suspend fun insertGroup(group: GroupEntity): Long = dao.insertGroup(group)
    suspend fun deleteGroup(id: Int) = dao.deleteGroup(id)

    fun getMembersForGroup(groupId: Int): Flow<List<MemberEntity>> = dao.getMembersForGroup(groupId)
    suspend fun getMembersForGroupSync(groupId: Int): List<MemberEntity> = dao.getMembersForGroupSync(groupId)
    suspend fun insertMembers(members: List<MemberEntity>) = dao.insertMembers(members)
    suspend fun deleteMembersForGroup(groupId: Int) = dao.deleteMembersForGroup(groupId)

    fun getExpensesForGroup(groupId: Int): Flow<List<SplitExpenseEntity>> = dao.getExpensesForGroup(groupId)
    suspend fun getExpensesForGroupSync(groupId: Int): List<SplitExpenseEntity> = dao.getExpensesForGroupSync(groupId)
    suspend fun insertSplitExpense(expense: SplitExpenseEntity): Long = dao.insertSplitExpense(expense)
    suspend fun deleteSplitExpense(id: Int) = dao.deleteSplitExpense(id)

    fun getSettlementsForGroup(groupId: Int): Flow<List<SettlementEntity>> = dao.getSettlementsForGroup(groupId)
    suspend fun getSettlementsForGroupSync(groupId: Int): List<SettlementEntity> = dao.getSettlementsForGroupSync(groupId)
    suspend fun insertSettlement(settlement: SettlementEntity): Long = dao.insertSettlement(settlement)
    suspend fun deleteSettlement(id: Int) = dao.deleteSettlement(id)

    fun getBalancesForGroup(groupId: Int): Flow<List<BalanceEntity>> = dao.getBalancesForGroup(groupId)
    suspend fun insertBalances(balances: List<BalanceEntity>) = dao.insertBalances(balances)
    suspend fun deleteBalancesForGroup(groupId: Int) = dao.deleteBalancesForGroup(groupId)

    // Reports Preparation (Stage 6)
    suspend fun getGroupExpenseTotal(groupId: Int): Double {
        return dao.getExpensesForGroupSync(groupId).sumOf { it.amount }
    }

    suspend fun getMemberContributions(groupId: Int): Map<String, Double> {
        val expenses = dao.getExpensesForGroupSync(groupId)
        return expenses.groupBy { it.paidBy }.mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    suspend fun getOutstandingBalances(groupId: Int): Map<String, Double> {
        val members = dao.getMembersForGroupSync(groupId).map { it.memberName }
        val expenses = dao.getExpensesForGroupSync(groupId)
        val settlements = dao.getSettlementsForGroupSync(groupId)

        val netBalances = mutableMapOf<String, Double>()
        members.forEach { netBalances[it] = 0.0 }

        // Process Expenses
        expenses.forEach { exp ->
            // Credit the payer
            netBalances[exp.paidBy] = (netBalances[exp.paidBy] ?: 0.0) + exp.amount

            // Debit split shares
            val participants = exp.involvedMembers.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (participants.isNotEmpty()) {
                val shares = exp.participantShares.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                when (exp.splitType) {
                    "EQUAL" -> {
                        val share = exp.amount / participants.size
                        participants.forEach { p ->
                            netBalances[p] = (netBalances[p] ?: 0.0) - share
                        }
                    }
                    "EXACT" -> {
                        participants.forEachIndexed { idx, p ->
                            val sVal = shares.getOrNull(idx)?.toDoubleOrNull() ?: 0.0
                            netBalances[p] = (netBalances[p] ?: 0.0) - sVal
                        }
                    }
                    "PERCENT" -> {
                        participants.forEachIndexed { idx, p ->
                            val pct = shares.getOrNull(idx)?.toDoubleOrNull() ?: 0.0
                            val share = exp.amount * (pct / 100.0)
                            netBalances[p] = (netBalances[p] ?: 0.0) - share
                        }
                    }
                    "SHARE" -> {
                        val weights = shares.map { it.toDoubleOrNull() ?: 1.0 }
                        val totalWeights = weights.sum()
                        if (totalWeights > 0.0) {
                            participants.forEachIndexed { idx, p ->
                                val w = weights.getOrNull(idx) ?: 1.0
                                val share = exp.amount * (w / totalWeights)
                                netBalances[p] = (netBalances[p] ?: 0.0) - share
                            }
                        }
                    }
                }
            }
        }

        // Process Settlements
        settlements.forEach { set ->
            // Payer gets money back (outstanding increases/improves, so credits outstanding balance)
            netBalances[set.payer] = (netBalances[set.payer] ?: 0.0) + set.amount
            // Receiver got money, so outstanding becomes negative (decreases)
            netBalances[set.receiver] = (netBalances[set.receiver] ?: 0.0) - set.amount
        }

        return netBalances
    }

    suspend fun getSettlementStats(groupId: Int): Map<String, Double> {
        val settlements = dao.getSettlementsForGroupSync(groupId)
        val totalSettled = settlements.sumOf { it.amount }
        val count = settlements.size.toDouble()
        return mapOf(
            "totalSettled" to totalSettled,
            "settlementsCount" to count
        )
    }

    // Unified Ledger Entries
    val allUnifiedLedgerEntries: Flow<List<UnifiedLedgerEntry>> = dao.getAllUnifiedLedgerEntries()
    suspend fun insertUnifiedLedgerEntry(entry: UnifiedLedgerEntry) = dao.insertUnifiedLedgerEntry(entry)
    suspend fun deleteUnifiedLedgerEntry(id: Int) = dao.deleteUnifiedLedgerEntry(id)
    suspend fun deleteUnifiedLedgerEntryByReferenceId(referenceId: String) = dao.deleteUnifiedLedgerEntryByReferenceId(referenceId)
    suspend fun clearUnifiedLedgerEntries() = dao.clearUnifiedLedgerEntries()
}
