package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SplitUiState {
    object Loading : SplitUiState
    data class Success(
        val groups: List<GroupEntity>,
        val currentGroup: GroupEntity?,
        val currentMembers: List<MemberEntity>,
        val currentExpenses: List<SplitExpenseEntity>,
        val currentSettlements: List<SettlementEntity>,
        val currentBalances: List<BalanceEntity>,
        val currentSimplifiedDebts: List<SimplifiedDebt>,
        
        // Mode 1: Personal totals aggregated across all groups for "You" or local user
        val totalLent: Double,
        val totalBorrowed: Double,
        val netBalance: Double,
        val youAreOwedList: List<MemberBalanceInfo>,
        val youOweList: List<MemberBalanceInfo>
    ) : SplitUiState
    data class Error(val message: String) : SplitUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class SplitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = SplitRepository(db.dao())

    private val _userId = MutableStateFlow("guest")
    val userId: StateFlow<String> = _userId.asStateFlow()

    // Currently selected Group ID (0 or null if none selected, showing group list)
    private val _selectedGroupId = MutableStateFlow<Int?>(null)
    val selectedGroupId: StateFlow<Int?> = _selectedGroupId.asStateFlow()

    // Expose all groups
    val allGroups: StateFlow<List<GroupEntity>> = _userId.flatMapLatest { uid ->
        repository.selectAllGroups(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Build overall combined Flow for UI State
    val uiState: StateFlow<SplitUiState> = combine(
        _userId,
        _selectedGroupId,
        allGroups
    ) { uid, selectedId, groups ->
        Triple(uid, selectedId, groups)
    }.flatMapLatest { (uid, selectedId, groups) ->
        if (selectedId != null && selectedId > 0) {
            val matchingGroup = groups.find { it.id == selectedId }
            combine(
                repository.selectMembers(selectedId),
                repository.selectExpenses(selectedId),
                repository.selectSettlements(selectedId),
                repository.selectBalances(selectedId)
            ) { members, expenses, settlements, balances ->
                // Calculate simplified debts for active group
                val netBalancesMap = balances.associate { it.memberName to it.netBalance }
                val simplified = repository.simplifyDebts(netBalancesMap)

                // Calculate overall user's aggregates across all groups for Mode 1 stats
                val (lent, borrowed, owedList, oweList) = calculateAggregatesAcrossAll(groups)

                SplitUiState.Success(
                    groups = groups,
                    currentGroup = matchingGroup,
                    currentMembers = members,
                    currentExpenses = expenses,
                    currentSettlements = settlements,
                    currentBalances = balances,
                    currentSimplifiedDebts = simplified,
                    totalLent = lent,
                    totalBorrowed = borrowed,
                    netBalance = lent - borrowed,
                    youAreOwedList = owedList,
                    youOweList = oweList
                )
            }
        } else {
            // No group selected: show aggregate values across all groups
            flow {
                val (lent, borrowed, owedList, oweList) = calculateAggregatesAcrossAll(groups)
                emit(
                    SplitUiState.Success(
                        groups = groups,
                        currentGroup = null,
                        currentMembers = emptyList(),
                        currentExpenses = emptyList(),
                        currentSettlements = emptyList(),
                        currentBalances = emptyList(),
                        currentSimplifiedDebts = emptyList(),
                        totalLent = lent,
                        totalBorrowed = borrowed,
                        netBalance = lent - borrowed,
                        youAreOwedList = owedList,
                        youOweList = oweList
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SplitUiState.Loading)

    // Selects active group context
    fun selectGroup(groupId: Int?) {
        _selectedGroupId.value = groupId
    }

    // Actions
    fun addGroup(name: String, description: String, type: String, members: List<String>) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        val newGroupId = repository.createGroup(name, description, type, _userId.value).toInt()
        
        // Add all members including "You" (always added as the primary, default local user)
        val allMemberNames = mutableListOf("You")
        members.forEach { m ->
            if (m.isNotBlank() && m != "You") {
                allMemberNames.add(m)
            }
        }
        repository.addMembers(newGroupId, allMemberNames, _userId.value)
        repository.recalculateAndSaveBalances(newGroupId, _userId.value)
    }

    fun deleteGroup(groupId: Int) = viewModelScope.launch {
        repository.removeGroup(groupId)
        if (_selectedGroupId.value == groupId) {
            _selectedGroupId.value = null
        }
    }

    fun addExpense(
        groupId: Int,
        title: String,
        amount: Double,
        paidBy: String,
        category: String,
        notes: String,
        splitType: String,
        shares: String,
        involved: String
    ) = viewModelScope.launch {
        repository.addExpense(
            groupId = groupId,
            title = title,
            amount = amount,
            paidBy = paidBy,
            category = category,
            notes = notes,
            splitType = splitType,
            participantShares = shares,
            involvedMembers = involved,
            userId = _userId.value
        )
    }

    fun deleteExpense(expenseId: Int, groupId: Int) = viewModelScope.launch {
        repository.removeExpense(expenseId, groupId, _userId.value)
    }

    fun addSettlement(
        groupId: Int,
        payer: String,
        receiver: String,
        amount: Double,
        notes: String
    ) = viewModelScope.launch {
        repository.addSettlement(
            groupId = groupId,
            payer = payer,
            receiver = receiver,
            amount = amount,
            notes = notes,
            userId = _userId.value
        )
    }

    fun deleteSettlement(settlementId: Int, groupId: Int) = viewModelScope.launch {
        repository.removeSettlement(settlementId, groupId, _userId.value)
    }

    // Calculates aggregates across all groups for simple lent/borrowed/net trackers (Mode 1)
    private suspend fun calculateAggregatesAcrossAll(groups: List<GroupEntity>): TupleAggregates {
        var totalLentSum = 0.0
        var totalBorrowedSum = 0.0
        
        // Cumulative mapping of peer transactions (who owes you, whom you owe)
        val peerBalanceMap = mutableMapOf<String, Double>()

        groups.forEach { group ->
            val balances = repository.getGroupSummary(group.id).outstandingBalances
            // Local user "You" net balance in this group
            val myBalance = balances["You"] ?: 0.0
            
            if (myBalance > 0.0) {
                totalLentSum += myBalance
            } else if (myBalance < 0.0) {
                totalBorrowedSum += -myBalance
            }

            balances.forEach { (member, balance) ->
                if (member != "You") {
                    // Splitwise perspective: If "You" have dynamic net balance:
                    // In a group, if X has net balance -₹100 (debtor) and "You" have net balance +₹100 (creditor):
                    // In simple terms, members with balance < 0 owe members with balance > 0.
                    // To aggregate individual peer balances, parse simplified debts!
                    // This is 100% correct, precise, and matches actual ledger settlement.
                }
            }

            val simplified = repository.simplifyDebts(balances)
            simplified.forEach { debt ->
                // debt: from pays to amount
                if (debt.toUser == "You") {
                    // 'from' owes us!
                    peerBalanceMap[debt.fromUser] = (peerBalanceMap[debt.fromUser] ?: 0.0) + debt.amount
                } else if (debt.fromUser == "You") {
                    // we owe 'to'!
                    peerBalanceMap[debt.toUser] = (peerBalanceMap[debt.toUser] ?: 0.0) - debt.amount
                }
            }
        }

        val owedList = mutableListOf<MemberBalanceInfo>()
        val oweList = mutableListOf<MemberBalanceInfo>()

        peerBalanceMap.forEach { (name, balance) ->
            if (balance > 0.01) {
                owedList.add(MemberBalanceInfo(name, balance))
            } else if (balance < -0.01) {
                oweList.add(MemberBalanceInfo(name, -balance))
            }
        }

        // Return calculated totals
        return TupleAggregates(
            totalLent = totalLentSum,
            totalBorrowed = totalBorrowedSum,
            youAreOwedList = owedList.sortedByDescending { it.netBalance },
            youOweList = oweList.sortedByDescending { it.netBalance }
        )
    }

    private data class TupleAggregates(
        val totalLent: Double,
        val totalBorrowed: Double,
        val youAreOwedList: List<MemberBalanceInfo>,
        val youOweList: List<MemberBalanceInfo>
    )
}
