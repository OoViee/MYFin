package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface TripUiState {
    object Loading : TripUiState
    data class Success(
        val trips: List<TripEntity>,
        val currentTrip: TripEntity?,
        val currentParticipants: List<TripParticipantEntity>,
        val currentExpenses: List<SplitExpenseEntity>,
        val currentSettlements: List<SettlementEntity>,
        val currentBalances: List<BalanceEntity>,
        val analytics: TripAnalytics?,
        val activeTripsCount: Int,
        val pendingSettlementsAmount: Double
    ) : TripUiState
    data class Error(val message: String) : TripUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = TripRepository(db.dao())

    private val _userId = MutableStateFlow("guest")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _selectedTripId = MutableStateFlow<Int?>(null)
    val selectedTripId: StateFlow<Int?> = _selectedTripId.asStateFlow()

    // Read general flow for trips
    val allTrips: StateFlow<List<TripEntity>> = _userId.flatMapLatest { uid ->
        repository.selectAllTrips(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Read general flow for dashboard numbers (active trips count & total pending debt)
    val dashboardSummary: StateFlow<Pair<Int, Double>> = _userId.flatMapLatest { uid ->
        repository.getTripDashboardSummary(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0.0))

    // Combined UI state flow as requested by Stage 7
    val uiState: StateFlow<TripUiState> = combine(
        _userId,
        _selectedTripId,
        allTrips,
        dashboardSummary
    ) { uid, selectedId, trips, dashboard ->
        TupleParams(uid, selectedId, trips, dashboard)
    }.flatMapLatest<TupleParams, TripUiState> { params ->
        val selectedId = params.selectedId
        val trips = params.trips
        val (activeCount, pendingAmt) = params.dashboard

        if (selectedId != null && selectedId > 0) {
            val matchingTrip = trips.find { it.id == selectedId }
            if (matchingTrip != null) {
                combine(
                    repository.selectTripParticipants(selectedId),
                    repository.selectTripExpenses(matchingTrip.groupId),
                    repository.selectTripSettlements(matchingTrip.groupId),
                    db.dao().getBalancesForGroup(matchingTrip.groupId),
                    repository.getTripAnalytics(selectedId, matchingTrip.groupId)
                ) { participants, expenses, settlements, balances, analytics ->
                    TripUiState.Success(
                        trips = trips,
                        currentTrip = matchingTrip,
                        currentParticipants = participants,
                        currentExpenses = expenses,
                        currentSettlements = settlements,
                        currentBalances = balances,
                        analytics = analytics,
                        activeTripsCount = activeCount,
                        pendingSettlementsAmount = pendingAmt
                    )
                }
            } else {
                flowOf(
                    TripUiState.Success(
                        trips = trips,
                        currentTrip = null,
                        currentParticipants = emptyList(),
                        currentExpenses = emptyList(),
                        currentSettlements = emptyList(),
                        currentBalances = emptyList(),
                        analytics = null,
                        activeTripsCount = activeCount,
                        pendingSettlementsAmount = pendingAmt
                    )
                )
            }
        } else {
            flowOf(
                TripUiState.Success(
                    trips = trips,
                    currentTrip = null,
                    currentParticipants = emptyList(),
                    currentExpenses = emptyList(),
                    currentSettlements = emptyList(),
                    currentBalances = emptyList(),
                    analytics = null,
                    activeTripsCount = activeCount,
                    pendingSettlementsAmount = pendingAmt
                )
            )
        }
    }.catch { e ->
        emit(TripUiState.Error(e.message ?: "An unknown error occurred"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripUiState.Loading)

    // Actions
    fun createTrip(
        name: String,
        description: String,
        startDate: String,
        endDate: String,
        location: String,
        eventType: String
    ) = viewModelScope.launch {
        repository.createTrip(name, description, startDate, endDate, location, eventType, _userId.value)
    }

    fun selectTrip(tripId: Int?) {
        _selectedTripId.value = tripId
    }

    fun deleteTrip(tripId: Int) = viewModelScope.launch {
        repository.removeTrip(tripId)
        if (_selectedTripId.value == tripId) {
            _selectedTripId.value = null
        }
    }

    fun addParticipant(tripId: Int, name: String, role: String) = viewModelScope.launch {
        repository.addTripParticipant(tripId, name, role, _userId.value)
    }

    fun removeParticipant(participantId: Int, tripId: Int) = viewModelScope.launch {
        repository.removeTripParticipant(participantId, tripId, _userId.value)
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
        involved: String,
        receiptUri: String = ""
    ) = viewModelScope.launch {
        repository.addTripExpense(
            groupId = groupId,
            title = title,
            amount = amount,
            paidBy = paidBy,
            category = category,
            notes = notes,
            splitType = splitType,
            shares = shares,
            involved = involved,
            receiptUri = receiptUri,
            userId = _userId.value
        )
    }

    fun deleteExpense(expenseId: Int, groupId: Int) = viewModelScope.launch {
        repository.removeTripExpense(expenseId, groupId, _userId.value)
    }

    fun addSettlement(
        groupId: Int,
        payer: String,
        receiver: String,
        amount: Double,
        notes: String
    ) = viewModelScope.launch {
        repository.addTripSettlement(
            groupId = groupId,
            payer = payer,
            receiver = receiver,
            amount = amount,
            notes = notes,
            userId = _userId.value
        )
    }

    fun deleteSettlement(settlementId: Int, groupId: Int) = viewModelScope.launch {
        repository.removeTripSettlement(settlementId, groupId, _userId.value)
    }

    private data class TupleParams(
        val uid: String,
        val selectedId: Int?,
        val trips: List<TripEntity>,
        val dashboard: Pair<Int, Double>
    )
}
