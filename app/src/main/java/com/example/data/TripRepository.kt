package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

data class TripParticipantSummary(
    val name: String,
    val role: String,
    val totalPaid: Double,
    val individualShare: Double,
    val balance: Double // positive means they should receive, negative means they owe
)

data class TripCategoryExpense(
    val category: String,
    val amount: Double,
    val percentage: Double
)

data class TripAnalytics(
    val totalTripCost: Double,
    val costPerPerson: Double,
    val categoryDistribution: List<TripCategoryExpense>,
    val participantSummaries: List<TripParticipantSummary>,
    val recommendedSettlements: List<SimplifiedDebt>,
    val pendingSettlementsAmount: Double // outstanding settlements involving "You"
)

// Expose export-ready models as requested by Stage 7
data class TripExportData(
    val trip: TripEntity,
    val participants: List<TripParticipantEntity>,
    val totalCost: Double,
    val perPersonCost: Double,
    val expensesBreakdown: List<TripCategoryExpense>,
    val participantContributions: List<TripParticipantSummary>,
    val recommededSettleList: List<SimplifiedDebt>
)

class TripRepository(private val dao: WealthPulseDao) {

    private val splitRepository = SplitRepository(dao)

    // Trip CRUD
    fun selectAllTrips(userId: String = "guest"): Flow<List<TripEntity>> = dao.getAllTrips(userId)

    fun selectTripById(tripId: Int): Flow<TripEntity?> = dao.getTripById(tripId)

    suspend fun getTripByIdSync(tripId: Int): TripEntity? = dao.getTripByIdSync(tripId)

    suspend fun createTrip(
        name: String,
        description: String,
        startDate: String,
        endDate: String,
        location: String,
        eventType: String,
        userId: String = "guest"
    ): Long {
        // Step 1: Create a backing Splitwise Group
        val backingGroupId = splitRepository.createGroup(
            groupName = name,
            description = "Backing group for $eventType: $name",
            groupType = "Trip",
            userId = userId
        ).toInt()

        // Step 2: Create the TripEntity linking to this backing group
        val trip = TripEntity(
            name = name,
            description = description,
            startDate = startDate,
            endDate = endDate,
            location = location,
            eventType = eventType,
            status = "Active",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            groupId = backingGroupId,
            userId = userId
        )
        val tripId = dao.insertTrip(trip).toInt()

        // Step 3: Add "You" as default participant with Organizer role
        val orgParticipant = TripParticipantEntity(
            tripId = tripId,
            name = "You",
            role = "Organizer"
        )
        dao.insertTripParticipant(orgParticipant)

        // Step 4: Ensure backing split group has "You" as member
        splitRepository.addMembers(backingGroupId, listOf("You"), userId)
        splitRepository.recalculateAndSaveBalances(backingGroupId, userId)

        return tripId.toLong()
    }

    suspend fun updateTrip(trip: TripEntity) {
        val updatedTrip = trip.copy(updatedAt = System.currentTimeMillis())
        dao.insertTrip(updatedTrip)

        // Keep backing Splitwise group in sync
        val currentBackingGroup = dao.getAllGroups(trip.userId).first().find { it.id == trip.groupId }
        if (currentBackingGroup != null) {
            val updatedGroup = currentBackingGroup.copy(
                groupName = trip.name,
                description = "Backing group for ${trip.eventType}: ${trip.name}",
                updatedDate = System.currentTimeMillis()
            )
            splitRepository.updateGroup(updatedGroup)
        }
    }

    suspend fun removeTrip(tripId: Int) {
        val trip = getTripByIdSync(tripId) ?: return
        
        // Remove Trip entity and its specific participants
        dao.deleteTrip(tripId)
        dao.deleteParticipantsForTrip(tripId)

        // Remove Backing splitwise group and its expenses, members, and balances
        splitRepository.removeGroup(trip.groupId)
    }

    // Participant Management
    fun selectTripParticipants(tripId: Int): Flow<List<TripParticipantEntity>> = dao.getParticipantsForTrip(tripId)

    suspend fun addTripParticipant(tripId: Int, name: String, role: String, userId: String = "guest"): Boolean {
        if (name.isBlank() || name == "You") return false
        val trip = getTripByIdSync(tripId) ?: return false

        // Check if already participant
        val currentParticipants = dao.getParticipantsForTripSync(tripId)
        if (currentParticipants.any { it.name.equals(name, ignoreCase = true) }) return false

        // Add to trip participants
        val newPart = TripParticipantEntity(
            tripId = tripId,
            name = name,
            role = role
        )
        dao.insertTripParticipant(newPart)

        // Sync with Split module members
        val backingGroupId = trip.groupId
        val currentMembers = splitRepository.selectMembersSync(backingGroupId)
        if (currentMembers.none { it.memberName.equals(name, ignoreCase = true) }) {
            splitRepository.addMembers(backingGroupId, listOf(name), userId)
            splitRepository.recalculateAndSaveBalances(backingGroupId, userId)
        }
        return true
    }

    suspend fun removeTripParticipant(participantId: Int, tripId: Int, userId: String = "guest") {
        // Delete participant role record
        dao.deleteTripParticipant(participantId)
        
        val trip = getTripByIdSync(tripId) ?: return
        // Recalculate balances in backing group to reflect any changes
        splitRepository.recalculateAndSaveBalances(trip.groupId, userId)
    }

    // Expense & Settlement Integration Strategies
    fun selectTripExpenses(groupId: Int): Flow<List<SplitExpenseEntity>> = splitRepository.selectExpenses(groupId)

    suspend fun addTripExpense(
        groupId: Int,
        title: String,
        amount: Double,
        paidBy: String,
        category: String,
        notes: String,
        splitType: String,
        shares: String,
        involved: String,
        receiptUri: String = "",
        userId: String = "guest"
    ): Long {
        // Reuses Splitwise Engine directly, but adds attachment support
        val expense = SplitExpenseEntity(
            groupId = groupId,
            title = title,
            amount = amount,
            paidBy = paidBy,
            category = category,
            notes = notes,
            splitType = splitType,
            participantShares = shares,
            involvedMembers = involved,
            receiptUri = receiptUri,
            userId = userId
        )
        val id = dao.insertSplitExpense(expense)
        splitRepository.recalculateAndSaveBalances(groupId, userId)
        return id
    }

    suspend fun removeTripExpense(expenseId: Int, groupId: Int, userId: String = "guest") {
        splitRepository.removeExpense(expenseId, groupId, userId)
    }

    fun selectTripSettlements(groupId: Int): Flow<List<SettlementEntity>> = splitRepository.selectSettlements(groupId)

    suspend fun addTripSettlement(
        groupId: Int,
        payer: String,
        receiver: String,
        amount: Double,
        notes: String,
        userId: String = "guest"
    ): Long {
        return splitRepository.addSettlement(groupId, payer, receiver, amount, notes, userId)
    }

    suspend fun removeTripSettlement(settlementId: Int, groupId: Int, userId: String = "guest") {
        splitRepository.removeSettlement(settlementId, groupId, userId)
    }

    // Trip Real-time Analytics Calculations
    fun getTripAnalytics(tripId: Int, groupId: Int): Flow<TripAnalytics> {
        return combine(
            splitRepository.selectMembers(groupId),
            splitRepository.selectExpenses(groupId),
            splitRepository.selectBalances(groupId),
            selectTripParticipants(tripId)
        ) { members, expenses, balances, tripParticipants ->
            val totalTripCost = expenses.sumOf { it.amount }
            val memberCount = if (members.isNotEmpty()) members.size else 1
            val costPerPerson = totalTripCost / memberCount

            // 1. Category Distribution
            val categorySums = expenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            val categoriesList = listOf(
                "Transport", "Fuel", "Hotel", "Food", "Activities",
                "Shopping", "Tickets", "Sports", "Emergency", "Miscellaneous"
            )
            
            val categoryBreakdown = categoriesList.map { cat ->
                val amount = categorySums[cat] ?: 0.0
                val percentage = if (totalTripCost > 0.0) (amount / totalTripCost) * 100.0 else 0.0
                TripCategoryExpense(cat, amount, percentage)
            }.filter { it.amount > 0.0 }.sortedByDescending { it.amount }

            // 2. Participant Summary calculations
            val roleMap = tripParticipants.associate { it.name to it.role }
            
            // Calculate actual paid & individual share for each participant dynamically
            val summaryList = members.map { member ->
                val name = member.memberName
                val role = roleMap[name] ?: "Participant"
                
                // Real total paid by this member
                val paid = expenses.filter { it.paidBy == name }.sumOf { it.amount }
                
                // Real net balance generated by splits
                val netBal = balances.find { it.memberName == name }?.netBalance ?: 0.0
                
                // Share = what they were supposed to pay = paid - netBal
                val share = paid - netBal

                TripParticipantSummary(
                    name = name,
                    role = role,
                    totalPaid = paid,
                    individualShare = share,
                    balance = netBal
                )
            }

            // 3. Recommended settlements derived dynamically via SimplifyDebts
            val netBalancesMap = balances.associate { it.memberName to it.netBalance }
            val recommended = splitRepository.simplifyDebts(netBalancesMap)

            // 4. Pending settlements sum involving "You"
            val userPendingSum = recommended.filter { it.fromUser == "You" || it.toUser == "You" }
                .sumOf { it.amount }

            TripAnalytics(
                totalTripCost = totalTripCost,
                costPerPerson = costPerPerson,
                categoryDistribution = categoryBreakdown,
                participantSummaries = summaryList,
                recommendedSettlements = recommended,
                pendingSettlementsAmount = userPendingSum
            )
        }
    }

    // Dashboard Aggregate (Active Trips & Pending Settlements)
    fun getTripDashboardSummary(userId: String = "guest"): Flow<Pair<Int, Double>> {
        return combine(
            selectAllTrips(userId),
            dao.getAllGroupBalances()
        ) { trips, balances ->
            val activeTrips = trips.filter { it.status == "Active" }
            val activeTripGroupIds = activeTrips.map { it.groupId }.toSet()

            // Filter balances involving "You" that belong to these active trip groups
            val myPendingDebtSum = balances.filter {
                it.userId == userId && 
                it.memberName == "You" && 
                activeTripGroupIds.contains(it.groupId)
            }.sumOf { Math.abs(it.netBalance) }

            Pair(activeTrips.size, myPendingDebtSum)
        }
    }

    // Export Preparation support
    suspend fun prepareExportData(tripId: Int): TripExportData? {
        val trip = getTripByIdSync(tripId) ?: return null
        val participants = dao.getParticipantsForTripSync(tripId)
        
        // Single valuation of flow variables to prepare a snapshot
        val analytics = getTripAnalytics(tripId, trip.groupId).first()

        return TripExportData(
            trip = trip,
            participants = participants,
            totalCost = analytics.totalTripCost,
            perPersonCost = analytics.costPerPerson,
            expensesBreakdown = analytics.categoryDistribution,
            participantContributions = analytics.participantSummaries,
            recommededSettleList = analytics.recommendedSettlements
        )
    }
}
