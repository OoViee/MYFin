package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.math.min

data class SimplifiedDebt(
    val fromUser: String,
    val toUser: String,
    val amount: Double
)

data class MemberBalanceInfo(
    val memberName: String,
    val netBalance: Double
)

class SplitRepository(private val dao: WealthPulseDao) {

    // Group Management
    fun selectAllGroups(userId: String = "guest"): Flow<List<GroupEntity>> = dao.getAllGroups(userId)
    
    suspend fun createGroup(groupName: String, description: String, groupType: String, userId: String = "guest"): Long {
        val group = GroupEntity(
            groupName = groupName,
            description = description,
            groupType = groupType,
            userId = userId
        )
        return dao.insertGroup(group)
    }

    suspend fun updateGroup(group: GroupEntity) {
        dao.insertGroup(group)
    }

    suspend fun removeGroup(groupId: Int) {
        dao.deleteGroup(groupId)
        dao.deleteMembersForGroup(groupId)
        dao.deleteBalancesForGroup(groupId)
        // Clean expenses and settlements under group
        val expenses = dao.getExpensesForGroupSync(groupId)
        expenses.forEach { dao.deleteSplitExpense(it.id) }
        val settlements = dao.getSettlementsForGroupSync(groupId)
        settlements.forEach { dao.deleteSettlement(it.id) }
    }

    // Member Management
    fun selectMembers(groupId: Int): Flow<List<MemberEntity>> = dao.getMembersForGroup(groupId)
    suspend fun selectMembersSync(groupId: Int): List<MemberEntity> = dao.getMembersForGroupSync(groupId)
    
    suspend fun addMembers(groupId: Int, names: List<String>, userId: String = "guest") {
        val members = names.mapIndexed { index, name ->
            MemberEntity(
                groupId = groupId,
                memberName = name,
                colorIdentifier = index % 8, // rotation of 8 colors
                userId = userId
            )
        }
        dao.insertMembers(members)
    }

    // Expense Tracking
    fun selectExpenses(groupId: Int): Flow<List<SplitExpenseEntity>> = dao.getExpensesForGroup(groupId)
    suspend fun selectExpensesSync(groupId: Int): List<SplitExpenseEntity> = dao.getExpensesForGroupSync(groupId)

    suspend fun addExpense(
        groupId: Int,
        title: String,
        amount: Double,
        paidBy: String,
        category: String,
        notes: String,
        splitType: String,
        participantShares: String,
        involvedMembers: String,
        userId: String = "guest"
    ): Long {
        val expense = SplitExpenseEntity(
            groupId = groupId,
            title = title,
            amount = amount,
            paidBy = paidBy,
            category = category,
            notes = notes,
            splitType = splitType,
            participantShares = participantShares,
            involvedMembers = involvedMembers,
            userId = userId
        )
        val id = dao.insertSplitExpense(expense)
        recalculateAndSaveBalances(groupId, userId)
        return id
    }

    suspend fun removeExpense(expenseId: Int, groupId: Int, userId: String = "guest") {
        dao.deleteSplitExpense(expenseId)
        recalculateAndSaveBalances(groupId, userId)
    }

    // Settlement Tracking
    fun selectSettlements(groupId: Int): Flow<List<SettlementEntity>> = dao.getSettlementsForGroup(groupId)
    suspend fun selectSettlementsSync(groupId: Int): List<SettlementEntity> = dao.getSettlementsForGroupSync(groupId)

    suspend fun addSettlement(
        groupId: Int,
        payer: String,
        receiver: String,
        amount: Double,
        notes: String,
        userId: String = "guest"
    ): Long {
        val settlement = SettlementEntity(
            groupId = groupId,
            payer = payer,
            receiver = receiver,
            amount = amount,
            notes = notes,
            userId = userId
        )
        val id = dao.insertSettlement(settlement)
        recalculateAndSaveBalances(groupId, userId)
        return id
    }

    suspend fun removeSettlement(settlementId: Int, groupId: Int, userId: String = "guest") {
        dao.deleteSettlement(settlementId)
        recalculateAndSaveBalances(groupId, userId)
    }

    // Live Balances (Mode 2)
    fun selectBalances(groupId: Int): Flow<List<BalanceEntity>> = dao.getBalancesForGroup(groupId)

    // Debt Simplification Algorithm (Minimizing Cash Flow / Min Trans)
    fun simplifyDebts(netBalances: Map<String, Double>): List<SimplifiedDebt> {
        val debtors = mutableListOf<Pair<String, Double>>()
        val creditors = mutableListOf<Pair<String, Double>>()

        netBalances.forEach { (name, balance) ->
            if (balance < -0.01) {
                debtors.add(Pair(name, balance))
            } else if (balance > 0.01) {
                creditors.add(Pair(name, balance))
            }
        }

        // Sort debtors asc (most negative first)
        debtors.sortBy { it.second }
        // Sort creditors desc (most positive first)
        creditors.sortByDescending { it.second }

        val transactions = mutableListOf<SimplifiedDebt>()
        var dIdx = 0
        var cIdx = 0

        val activeDebtors = debtors.map { it.first to it.second }.toMutableList()
        val activeCreditors = creditors.map { it.first to it.second }.toMutableList()

        while (dIdx < activeDebtors.size && cIdx < activeCreditors.size) {
            val debtorName = activeDebtors[dIdx].first
            val debtorOws = -activeDebtors[dIdx].second

            val creditorName = activeCreditors[cIdx].first
            val creditorOwed = activeCreditors[cIdx].second

            val settleAmount = min(debtorOws, creditorOwed)
            if (settleAmount > 0.01) {
                transactions.add(SimplifiedDebt(debtorName, creditorName, settleAmount))
            }

            // Update remaining
            val remainingDebtor = debtorOws - settleAmount
            val remainingCreditor = creditorOwed - settleAmount

            if (remainingDebtor < 0.01) {
                dIdx++
            } else {
                activeDebtors[dIdx] = Pair(debtorName, -remainingDebtor)
            }

            if (remainingCreditor < 0.01) {
                cIdx++
            } else {
                activeCreditors[cIdx] = Pair(creditorName, remainingCreditor)
            }
        }

        return transactions
    }

    // Force recalculating balances and commit state to BalanceEntity for indexed queries
    suspend fun recalculateAndSaveBalances(groupId: Int, userId: String = "guest") {
        val members = dao.getMembersForGroupSync(groupId).map { it.memberName }
        val expenses = dao.getExpensesForGroupSync(groupId)
        val settlements = dao.getSettlementsForGroupSync(groupId)

        val netBalances = mutableMapOf<String, Double>()
        members.forEach { netBalances[it] = 0.0 }

        // Process Expenses
        expenses.forEach { exp ->
            netBalances[exp.paidBy] = (netBalances[exp.paidBy] ?: 0.0) + exp.amount

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
            netBalances[set.payer] = (netBalances[set.payer] ?: 0.0) + set.amount
            netBalances[set.receiver] = (netBalances[set.receiver] ?: 0.0) - set.amount
        }

        // Clear existing balances, save calculated values
        dao.deleteBalancesForGroup(groupId)
        val balanceEntities = netBalances.map { (name, balance) ->
            BalanceEntity(
                groupId = groupId,
                memberName = name,
                netBalance = balance,
                userId = userId
            )
        }
        dao.insertBalances(balanceEntities)
    }

    // Reports Aggregator support
    suspend fun getGroupSummary(groupId: Int): GroupReportInfo {
        val members = dao.getMembersForGroupSync(groupId).map { it.memberName }
        val expenses = dao.getExpensesForGroupSync(groupId)
        val settlements = dao.getSettlementsForGroupSync(groupId)

        val totalExp = expenses.sumOf { it.amount }

        val contributions = mutableMapOf<String, Double>()
        members.forEach { contributions[it] = 0.0 }
        expenses.forEach { exp ->
            contributions[exp.paidBy] = (contributions[exp.paidBy] ?: 0.0) + exp.amount
        }

        val netBalances = mutableMapOf<String, Double>()
        members.forEach { netBalances[it] = 0.0 }

        expenses.forEach { exp ->
            netBalances[exp.paidBy] = (netBalances[exp.paidBy] ?: 0.0) + exp.amount

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

        settlements.forEach { set ->
            netBalances[set.payer] = (netBalances[set.payer] ?: 0.0) + set.amount
            netBalances[set.receiver] = (netBalances[set.receiver] ?: 0.0) - set.amount
        }

        val totalSettled = settlements.sumOf { it.amount }
        val settlementsCount = settlements.size

        return GroupReportInfo(
            totalExpense = totalExp,
            memberContributions = contributions,
            outstandingBalances = netBalances,
            totalSettled = totalSettled,
            settlementsCount = settlementsCount
        )
    }
}

data class GroupReportInfo(
    val totalExpense: Double,
    val memberContributions: Map<String, Double>,
    val outstandingBalances: Map<String, Double>,
    val totalSettled: Double,
    val settlementsCount: Int
)
