package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CreditCardRepository(private val dao: WealthPulseDao) {

    // Cards CRUD
    fun getAllCreditCards(userId: String): Flow<List<CreditCardEntity>> =
        dao.getAllCreditCardsForUser(userId)

    suspend fun getCreditCardById(id: Int): CreditCardEntity? =
        dao.getCreditCardById(id)

    suspend fun insertCreditCard(card: CreditCardEntity) =
        dao.insertCreditCard(card)

    suspend fun deleteCreditCard(id: Int) =
        dao.deleteCreditCard(id)

    // Statements
    fun getStatementsForCard(cardId: Int): Flow<List<CardStatementEntity>> =
        dao.getStatementsForCard(cardId)

    suspend fun getStatementById(id: Int): CardStatementEntity? =
        dao.getStatementById(id)

    suspend fun insertCardStatement(statement: CardStatementEntity) =
        dao.insertCardStatement(statement)

    suspend fun deleteCardStatement(id: Int) =
        dao.deleteCardStatement(id)

    // EMIs
    fun getEMIsForCard(cardId: Int): Flow<List<CardEMIEntity>> =
        dao.getEMIsForCard(cardId)

    fun getAllEMIs(userId: String): Flow<List<CardEMIEntity>> =
        dao.getAllEMIs(userId)

    suspend fun insertCardEMI(emi: CardEMIEntity) =
        dao.insertCardEMI(emi)

    suspend fun deleteCardEMI(id: Int) =
        dao.deleteCardEMI(id)

    // Payments
    fun getPaymentsForCard(cardId: Int): Flow<List<CardPaymentEntity>> =
        dao.getPaymentsForCard(cardId)

    fun getAllPayments(userId: String): Flow<List<CardPaymentEntity>> =
        dao.getAllPayments(userId)

    suspend fun insertCardPayment(payment: CardPaymentEntity) =
        dao.insertCardPayment(payment)

    suspend fun deleteCardPayment(id: Int) =
        dao.deleteCardPayment(id)

    // Custom flow that aggregates each credit card with related statistics, transactions, and EMIs
    fun getCreditCardsWithDetails(userId: String): Flow<List<CreditCardWithDetails>> {
        return combine(
            dao.getAllCreditCardsForUser(userId),
            dao.getAllDailyExpenses()
        ) { cards, allExpenses ->
            cards.map { card ->
                // Filter transactions linked to this cardId or matching the card name
                val mockNameRegex = card.cardName.trim()
                val cardExpenses = allExpenses.filter {
                    it.userId == userId && !it.isDeleted &&
                    (it.cardId == card.id || (it.paymentMode == "Credit Card" && it.notes.contains(mockNameRegex, ignoreCase = true)))
                }

                val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)

                val monthlyExpenses = cardExpenses.filter {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                }

                // Outstanding = current recorded outstanding or dynamically calculated if preferred,
                // but let's stick to using card's outstandingAmount as source of truth and update it on action.
                val outstanding = card.outstandingAmount
                val limit = card.creditLimit
                val utilPct = if (limit > 0) (outstanding / limit) * 100.0 else 0.0

                val utilLevel = when {
                    utilPct > 100.0 -> "Exceeded"
                    utilPct >= 76.0 -> "Critical"
                    utilPct >= 51.0 -> "High"
                    utilPct >= 31.0 -> "Moderate"
                    else -> "Healthy"
                }

                val available = (limit - outstanding).coerceAtLeast(0.0)

                // Days until due calculation based on billDate and dueDate
                val calendar = Calendar.getInstance()
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                
                // If bill date >= current day, current statement is still building. Due date is in next/current month.
                // Simplified manual day calculations:
                val daysUntilDue = if (card.dueDate >= currentDay) {
                    card.dueDate - currentDay
                } else {
                    // Due in next month
                    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    (maxDays - currentDay) + card.dueDate
                }

                CreditCardWithDetails(
                    card = card,
                    utilizationPercentage = utilPct,
                    utilizationLevel = utilLevel,
                    availableCredit = available,
                    daysUntilDue = daysUntilDue,
                    recentTransactions = cardExpenses.take(15),
                    monthlySpentAmount = monthlyExpenses.sumOf { it.amount }
                )
            }
        }
    }

    // High Fidelity transaction and state changes payment recording
    suspend fun recordManualCardPayment(
        cardId: Int,
        amount: Double,
        payDate: Long,
        source: String,
        notes: String,
        statementId: Int = 0
    ) {
        val payment = CardPaymentEntity(
            cardId = cardId,
            amount = amount,
            paymentDate = payDate,
            sourceAccount = source,
            notes = notes,
            statementId = statementId,
            userId = "guest"
        )
        dao.insertCardPayment(payment)

        // Reduce outstanding on Credit Card
        val card = dao.getCreditCardById(cardId)
        if (card != null) {
            val newOutstanding = (card.outstandingAmount - amount).coerceAtLeast(0.0)
            dao.insertCreditCard(
                card.copy(
                    outstandingAmount = newOutstanding,
                    billStatus = if (newOutstanding <= 0.1) "Paid" else "Pending",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // Satisfy associated past statement if any
        if (statementId > 0) {
            val statement = dao.getStatementById(statementId)
            if (statement != null) {
                dao.insertCardStatement(
                    statement.copy(
                        paymentStatus = "Paid",
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // Record an expense linked to card
    suspend fun recordCardExpense(expense: DailyExpenseEntity, cardId: Int) {
        // Assert cardId
        val updatedExpense = expense.copy(cardId = cardId)
        dao.insertDailyExpense(updatedExpense)

        // Increment Outstanding
        val card = dao.getCreditCardById(cardId)
        if (card != null) {
            val newOutstanding = card.outstandingAmount + expense.amount
            dao.insertCreditCard(
                card.copy(
                    outstandingAmount = newOutstanding,
                    billStatus = "Pending",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Convert an expense to EMI
    suspend fun convertExpenseToEMI(
        cardId: Int,
        expenseId: Int,
        tenure: Int,
        emiAmt: Double,
        description: String
    ) {
        // Get the expense details
        val expenses = dao.getAllDailyExpenses()
        // We'll insert an EMI entity
        val emi = CardEMIEntity(
            cardId = cardId,
            purchaseAmount = emiAmt * tenure,
            purchaseDate = System.currentTimeMillis(),
            tenureMonths = tenure,
            emiAmount = emiAmt,
            remainingInstallments = tenure,
            description = description,
            status = "Active",
            userId = "guest"
        )
        dao.insertCardEMI(emi)

        // Mark the original expense as deleted or as EMI conversion
        // In this app, we can also keep the expense record for tracker history
    }

    // Notification infrastructure queries
    fun getAlertsStatus(cardId: Int, daysToDue: Int): Boolean {
        // Mock checking alert criteria.
        // Stage 4 asks to prepare infrastructure but NEVER implement runtime alerts.
        // These can return boolean triggers.
        return daysToDue in listOf(7, 3, 1)
    }
}

data class CreditCardWithDetails(
    val card: CreditCardEntity,
    val utilizationPercentage: Double,
    val utilizationLevel: String, // Healthy, Moderate, High, Critical, Exceeded
    val availableCredit: Double,
    val daysUntilDue: Int,
    val recentTransactions: List<DailyExpenseEntity>,
    val monthlySpentAmount: Double
)

data class CreditCardDashboardSummary(
    val nearestDueCardName: String,
    val amountDue: Double,
    val daysRemaining: Int,
    val cardId: Int
)
