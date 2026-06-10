package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface CreditCardUiState {
    object Loading : CreditCardUiState
    data class Success(
        val cardsWithDetails: List<CreditCardWithDetails>,
        val totalOutstanding: Double,
        val totalLimit: Double,
        val overallUtilizationPercentage: Double,
        val activeEMIs: List<CardEMIEntity>,
        val recentPayments: List<CardPaymentEntity>,
        val dashboardSummary: CreditCardDashboardSummary?
    ) : CreditCardUiState
    data class Error(val message: String) : CreditCardUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class CreditCardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CreditCardRepository(db.dao())

    private val _userId = MutableStateFlow("guest")
    val userId: StateFlow<String> = _userId.asStateFlow()

    // Aggregate UI state Reactively matching overall outstanding, budgets, and EMIs
    val uiState: StateFlow<CreditCardUiState> = _userId.flatMapLatest { uid ->
        combine(
            repository.getCreditCardsWithDetails(uid),
            repository.getAllEMIs(uid),
            repository.getAllPayments(uid)
        ) { cardList, emiList, paymentList ->
            val totalOutstanding = cardList.sumOf { it.card.outstandingAmount }
            val totalLimit = cardList.sumOf { it.card.creditLimit }
            val overallUtil = if (totalLimit > 0) (totalOutstanding / totalLimit) * 100.0 else 0.0

            val activeEMIs = emiList.filter { it.status == "Active" }

            // Compute nearest due card for High Fidelity dashboard display
            val nearestDueCard = cardList.filter { it.card.isActive && it.card.outstandingAmount > 0 }
                .minByOrNull { it.daysUntilDue }

            val dashboardSummary = nearestDueCard?.let {
                CreditCardDashboardSummary(
                    nearestDueCardName = it.card.cardName,
                    amountDue = it.card.outstandingAmount,
                    daysRemaining = it.daysUntilDue,
                    cardId = it.card.id
                )
            }

            CreditCardUiState.Success(
                cardsWithDetails = cardList,
                totalOutstanding = totalOutstanding,
                totalLimit = totalLimit,
                overallUtilizationPercentage = overallUtil,
                activeEMIs = activeEMIs,
                recentPayments = paymentList.take(20),
                dashboardSummary = dashboardSummary
            ) as CreditCardUiState
        }
    }.catch { e ->
        emit(CreditCardUiState.Error(e.localizedMessage ?: "Failed loading credit card systems."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreditCardUiState.Loading
    )

    // Form: Create a Credit Card
    fun addCreditCard(
        cardName: String,
        bankName: String,
        cardType: String,
        cardNetwork: String,
        lastFourDigits: String,
        creditLimit: Double,
        billingDate: Int,
        dueDate: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        if (cardName.isBlank() || bankName.isBlank()) {
            onResult(false, "Card name and bank name cannot be blank.")
            return
        }
        if (creditLimit <= 0) {
            onResult(false, "Credit Limit must be greater than zero.")
            return
        }
        if (billingDate !in 1..31 || dueDate !in 1..31) {
            onResult(false, "Billing/Due day must be between 1 and 31.")
            return
        }

        viewModelScope.launch {
            try {
                val newCard = CreditCardEntity(
                    cardName = cardName,
                    bankName = bankName,
                    cardType = cardType,
                    cardNetwork = cardNetwork,
                    lastFourDigits = lastFourDigits,
                    creditLimit = creditLimit,
                    billDate = billingDate,
                    billingDate = billingDate,
                    dueDate = dueDate,
                    isActive = true,
                    userId = _userId.value
                )
                repository.insertCreditCard(newCard)
                onResult(true, "Credit card created successfully.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to save credit card.")
            }
        }
    }

    // Form: Delete Credit Card
    fun deleteCreditCard(cardId: Int) {
        viewModelScope.launch {
            repository.deleteCreditCard(cardId)
        }
    }

    // Toggle card Active state
    fun toggleCardActiveStatus(cardId: Int, isActive: Boolean) {
        viewModelScope.launch {
            val card = repository.getCreditCardById(cardId)
            if (card != null) {
                repository.insertCreditCard(card.copy(isActive = isActive, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    // Form: Record manual Payment
    fun recordPayment(
        cardId: Int,
        amount: Double,
        paymentDate: Long,
        source: String,
        notes: String,
        statementId: Int = 0,
        onResult: (Boolean, String) -> Unit
    ) {
        if (amount <= 0) {
            onResult(false, "Payment amount must be greater than zero.")
            return
        }
        viewModelScope.launch {
            try {
                repository.recordManualCardPayment(cardId, amount, paymentDate, source, notes, statementId)
                onResult(true, "Payment recorded and utilization updated successfully!")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to record card payment.")
            }
        }
    }

    // Get statements for specific card as Flow
    fun getStatementsFlow(cardId: Int): Flow<List<CardStatementEntity>> =
        repository.getStatementsForCard(cardId)

    // Form: Add manual periodic Statement
    fun addStatement(
        cardId: Int,
        amount: Double,
        minimumDue: Double,
        startDate: Long,
        endDate: Long,
        dueDate: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        if (amount < 0) {
            onResult(false, "Statement amount cannot be negative.")
            return
        }
        viewModelScope.launch {
            try {
                val statement = CardStatementEntity(
                    cardId = cardId,
                    statementStartDate = startDate,
                    statementEndDate = endDate,
                    statementAmount = amount,
                    minimumDue = minimumDue,
                    paymentDueDate = dueDate,
                    paymentStatus = "Unpaid",
                    userId = _userId.value
                )
                repository.insertCardStatement(statement)
                
                // Op: update outstanding if we want to synchronize statement
                val card = repository.getCreditCardById(cardId)
                if (card != null && card.outstandingAmount == 0.0) {
                    repository.insertCreditCard(card.copy(outstandingAmount = amount))
                }
                
                onResult(true, "Statement loaded successfully.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to record card statement.")
            }
        }
    }

    // Form: Convert / Track manual EMI conversions
    fun createCardEMI(
        cardId: Int,
        description: String,
        purchaseAmount: Double,
        purchaseDate: Long,
        tenure: Int,
        emiAmount: Double,
        onResult: (Boolean, String) -> Unit
    ) {
        if (purchaseAmount <= 0 || tenure <= 0 || emiAmount <= 0) {
            onResult(false, "Form inputs must contain positive numerical metrics.")
            return
        }
        viewModelScope.launch {
            try {
                val emi = CardEMIEntity(
                    cardId = cardId,
                    purchaseAmount = purchaseAmount,
                    purchaseDate = purchaseDate,
                    tenureMonths = tenure,
                    emiAmount = emiAmount,
                    remainingInstallments = tenure,
                    description = description,
                    status = "Active",
                    userId = _userId.value
                )
                repository.insertCardEMI(emi)

                // Optional: add to outstanding immediately of that card
                val card = repository.getCreditCardById(cardId)
                if (card != null) {
                    repository.insertCreditCard(
                        card.copy(
                            outstandingAmount = card.outstandingAmount + purchaseAmount,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }

                onResult(true, "Purchase EMI track established!")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed creating EMI tracker.")
            }
        }
    }

    // Complete / Cancel EMI installment or change billing status
    fun updateEmiStatus(emiId: Int, remaining: Int, status: String) {
        viewModelScope.launch {
            val dbEMI = db.dao().getEMIById(emiId)
            if (dbEMI != null) {
                if (remaining <= 0 || status == "Completed") {
                    db.dao().insertCardEMI(dbEMI.copy(remainingInstallments = 0, status = "Completed", updatedAt = System.currentTimeMillis()))
                } else {
                    db.dao().insertCardEMI(dbEMI.copy(remainingInstallments = remaining, status = status, updatedAt = System.currentTimeMillis()))
                }
            }
        }
    }
    
    // Direct card expense logging from Card details
    fun addExpenseOnCard(cardId: Int, amount: Double, desc: String, category: String) {
        viewModelScope.launch {
            val expense = DailyExpenseEntity(
                amount = amount,
                currency = "INR",
                description = desc,
                category = category,
                paymentMode = "Credit Card",
                cardId = cardId,
                notes = "Auto-added from Credit Card details",
                userId = _userId.value
            )
            repository.recordCardExpense(expense, cardId)
        }
    }
}
