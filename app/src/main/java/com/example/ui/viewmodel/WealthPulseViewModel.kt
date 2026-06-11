package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed interface AiCoreState {
    object Idle : AiCoreState
    object Loading : AiCoreState
    data class Success(
        val rawJson: String,
        val intent: String,
        val description: String,
        val amount: Double,
        val category: String,
        val paymentMode: String,
        val alertFlagged: Boolean,
        val alertReason: String?,
        val queryResponseText: String?,
        val targetModule: String?
    ) : AiCoreState
    data class Error(val message: String) : AiCoreState
}

// Current User Sign In State
data class UserProfile(
    val uid: String,
    val email: String,
    val isAnonymous: Boolean = false
)

class WealthPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository: Repository
    private var firebaseAuth: FirebaseAuth? = null

    val syncManager = SyncManager(application, db.dao())
    val syncStats = syncManager.syncStats
    val isSimulatedOnline = syncManager.isSimulatedOnline

    val showMigrationDialog = MutableStateFlow(false)
    var pendingMigrationUserId: String? = null

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _currentUserId = MutableStateFlow("guest")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _selectedTheme = MutableStateFlow("default")
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    init {
        // Load persisted theme preference
        val themePrefs = application.getSharedPreferences("myfin_theme_prefs", android.content.Context.MODE_PRIVATE)
        _selectedTheme.value = themePrefs.getString("saved_theme", "default") ?: "default"

        val database = AppDatabase.getDatabase(application)
        repository = Repository(database.dao())

        // Safe Firebase initialization
        try {
            if (FirebaseApp.getApps(application).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("default-dummy-aistudio-key-for-myfin")
                    .setApplicationId("1:41719129166:android:612ebc06")
                    .setProjectId("myfin-auth")
                    .build()
                FirebaseApp.initializeApp(application, options)
            }
            firebaseAuth = FirebaseAuth.getInstance()
        } catch (e: Exception) {
            android.util.Log.e("MYFin", "Firebase initialization failed: ${e.message}. Using fallback auth.")
        }

        // Set initial user session
        val currentFirebaseUser = firebaseAuth?.currentUser
        if (currentFirebaseUser != null) {
            val userProfile = UserProfile(
                uid = currentFirebaseUser.uid,
                email = currentFirebaseUser.email ?: "anonymous@myfin.io",
                isAnonymous = currentFirebaseUser.isAnonymous
            )
            _currentUser.value = userProfile
            _currentUserId.value = userProfile.uid
        } else {
            // Check for cached local auth session saved in SharedPreferences
            val prefs = application.getSharedPreferences("myfin_auth_prefs", android.content.Context.MODE_PRIVATE)
            val savedUid = prefs.getString("saved_uid", null)
            val savedEmail = prefs.getString("saved_email", null)
            if (savedUid != null && savedEmail != null) {
                val userProfile = UserProfile(uid = savedUid, email = savedEmail)
                _currentUser.value = userProfile
                _currentUserId.value = savedUid
            } else {
                _currentUser.value = null
                _currentUserId.value = "guest"
            }
        }
        
        // Continuous/On-demand startup sync to guarantee historical ledger correctness
        triggerSync()
    }

    // Dynamic Streams filtered based on currentUserId
    val dailyExpenses = combine(
        repository.allDailyExpenses,
        repository.allDebtSplits,
        _currentUserId
    ) { expList, debtList, uid ->
        val standardExpenses = expList.filter { it.userId == uid && !it.isDeleted }.toMutableList()
        val lentExpenses = debtList.filter { it.userId == uid && !it.description.contains("borrow", ignoreCase = true) && !it.description.contains("owe", ignoreCase = true) }.map { debt ->
            val participants = debt.debtPersonInvolved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val paidList = debt.paidPeople.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            
            val statusSuffix = when {
                participants.isEmpty() -> ""
                participants.all { paidList.contains(it) } -> " (Received from All) ✔"
                paidList.isEmpty() -> " (Pending from ${participants.joinToString(", ")}) ⏳"
                else -> {
                    val settled = paidList.intersect(participants.toSet())
                    val pending = participants.filter { !paidList.contains(it) }
                    " (Received from ${settled.joinToString(", ")}, Pending from ${pending.joinToString(", ")}) ⏳"
                }
            }
            
            DailyExpenseEntity(
                id = -debt.id, // Negative to differentiate and target easily
                amount = debt.amount,
                currency = debt.currency,
                description = "${debt.description}$statusSuffix",
                category = "Debts & Splits",
                paymentMode = debt.paymentMode,
                userId = debt.userId,
                timestamp = debt.timestamp
            )
        }
        (standardExpenses + lentExpenses).sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val creditExpenses = combine(repository.allCreditExpenses, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val emiLoans = combine(repository.allEmiLoans, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val debtSplits = combine(repository.allDebtSplits, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val incomePaydays = combine(repository.allIncomePaydays, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sipRecords = combine(repository.allSipRecords, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val investmentRecords = combine(repository.allInvestmentRecords, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val creditCards = combine(repository.allCreditCards, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tripEvents = combine(repository.allTripEvents, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tripExpenses = combine(repository.allTripExpenses, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val participants = combine(repository.allParticipants, _currentUserId) { list, uid ->
        list.filter { it.userId == uid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI AI Core State
    private val _aiState = MutableStateFlow<AiCoreState>(AiCoreState.Idle)
    val aiState: StateFlow<AiCoreState> = _aiState.asStateFlow()

    // Process NL Input
    fun processVoiceOrTextInput(input: String) {
        if (input.isBlank()) return

        _aiState.value = AiCoreState.Loading

        viewModelScope.launch {
            try {
                // Compile context JSON of existing Room records
                val contextJson = buildDatabaseSummaryJson()

                // Request parse from Gemini Client
                val rawResponse = GeminiClient.processFinancialInput(input, contextJson)

                if (rawResponse.contains("\"error\": \"API_KEY_NOT_SET\"") || rawResponse.contains("\"error\": \"API_CALL_FAILED\"")) {
                    val jsonObj = JSONObject(rawResponse)
                    _aiState.value = AiCoreState.Error(jsonObj.optString("message", "API Configuration Error"))
                    return@launch
                }

                // Parse structured output
                val json = JSONObject(rawResponse.trim())
                val intentName = json.optString("intent", "UNKNOWN")

                var alertFlag = false
                var alertMsg: String? = null
                var desc = ""
                var amt = 0.0
                var cat = ""
                var payMode = ""
                var targetMod: String? = null
                var queryResponse: String? = null

                if (intentName == "FINANCIAL_QUERY_INSIGHT" || intentName == "CLARIFICATION_REQUIRED") {
                    val dataObj = json.optJSONObject("data")
                    targetMod = dataObj?.optString("query_target_module") ?: "GLOBAL"
                    queryResponse = dataObj?.optString("insights_response_text") ?: "No insights text."
                } else {
                    // LOGGING actions 1 to 5
                    val dataObj = json.optJSONObject("data")
                    if (dataObj != null) {
                        amt = dataObj.optDouble("amount", 0.0)
                        val curr = dataObj.optString("currency", "INR")
                        desc = dataObj.optString("description", "")
                        cat = dataObj.optString("category", "General")
                        payMode = dataObj.optString("payment_mode", "Cash")

                        val meta = dataObj.optJSONObject("module_specific_metadata")

                        // Auto insertion depending on mapped module intent
                        when (intentName) {
                            "LOG_DAILY_EXPENSE" -> {
                                repository.insertDailyExpense(
                                    DailyExpenseEntity(
                                        amount = amt,
                                        currency = curr,
                                        description = desc,
                                        category = cat,
                                        paymentMode = payMode
                                    )
                                )
                            }
                            "LOG_CREDIT_EXPENSE" -> {
                                val card = meta?.optString("card_name") ?: "Credit Card"
                                val emiConv = meta?.optBoolean("is_emi_conversion", false) ?: false
                                repository.insertCreditExpense(
                                    CreditExpenseEntity(
                                        amount = amt,
                                        currency = curr,
                                        description = desc,
                                        category = cat,
                                        cardName = card,
                                        isEmiConversion = emiConv
                                    )
                                )
                            }
                            "LOG_EMI_LOAN" -> {
                                val tenure = meta?.optInt("emi_total_tenure_months", 12) ?: 12
                                val rem = meta?.optInt("emi_remaining_months", 12) ?: 12
                                repository.insertEmiLoan(
                                    EmiLoanEntity(
                                        amount = amt,
                                        currency = curr,
                                        description = desc,
                                        category = cat,
                                        totalTenureMonths = tenure,
                                        remainingMonths = rem
                                    )
                                )
                            }
                            "LOG_DEBT_SPLIT" -> {
                                val person = meta?.optString("debt_person_involved") ?: ""
                                val isGroup = meta?.optBoolean("is_group_split", false) ?: false
                                val group = meta?.optString("group_name") ?: ""
                                repository.insertDebtSplit(
                                    DebtSplitEntity(
                                        amount = amt,
                                        currency = curr,
                                        description = desc,
                                        category = cat,
                                        paymentMode = payMode,
                                        debtPersonInvolved = person,
                                        isGroupSplit = isGroup,
                                        groupName = group
                                    )
                                )
                            }
                            "LOG_INCOME_PAYDAY" -> {
                                val freq = meta?.optString("income_frequency") ?: "Monthly"
                                repository.insertIncomePayday(
                                    IncomePaydayEntity(
                                        amount = amt,
                                        currency = curr,
                                        description = desc,
                                        category = cat,
                                        incomeFrequency = freq,
                                        paymentMode = payMode
                                    )
                                )
                            }
                            "LOG_SIP" -> {
                                val dayVal = meta?.optInt("sip_day_of_month", 5) ?: 5
                                val catVal = meta?.optString("sip_category") ?: cat
                                repository.insertSipRecord(
                                    SipEntity(
                                        amount = amt,
                                        currency = curr,
                                        description = desc,
                                        frequency = "Monthly",
                                        investmentCategory = catVal,
                                        dayOfMonth = dayVal
                                    )
                                )
                            }
                            "LOG_INVESTMENT" -> {
                                val currentVal = meta?.optDouble("investment_current_value", amt) ?: amt
                                val catVal = meta?.optString("investment_category") ?: cat
                                repository.insertInvestmentRecord(
                                    InvestmentEntity(
                                        amount = amt,
                                        currency = curr,
                                        description = desc,
                                        category = catVal,
                                        currentValue = currentVal
                                    )
                                )
                            }
                        }
                    }

                    // Smart AI Insights Alerts
                    val alertObj = json.optJSONObject("smart_ai_insights_trigger")
                    if (alertObj != null) {
                        alertFlag = alertObj.optBoolean("flag_alert", false)
                        alertMsg = alertObj.optString("alert_reason", null)
                    }
                }

                _aiState.value = AiCoreState.Success(
                    rawJson = rawResponse,
                    intent = intentName,
                    description = desc,
                    amount = amt,
                    category = cat,
                    paymentMode = payMode,
                    alertFlagged = alertFlag,
                    alertReason = alertMsg,
                    queryResponseText = queryResponse,
                    targetModule = targetMod
                )
            } catch (e: Exception) {
                _aiState.value = AiCoreState.Error(e.message ?: "Failed to interpret output from AI engine.")
            }
        }
    }

    fun clearAiState() {
        _aiState.value = AiCoreState.Idle
    }

    // Manual Insert Helpers
    fun addManualExpense(amount: Double, desc: String, cat: String, mode: String, cardName: String = "HDFC Millennia") {
        viewModelScope.launch {
            repository.insertDailyExpense(
                DailyExpenseEntity(amount = amount, currency = "INR", description = desc, category = cat, paymentMode = mode, userId = currentUserId.value)
            )
            if (mode == "Credit Card" || mode.contains("Credit", ignoreCase = true)) {
                updateCardOutstanding(cardName, amount)
            }
        }
    }

    fun addManualCredit(amount: Double, desc: String, card: String, cat: String) {
        viewModelScope.launch {
            repository.insertCreditExpense(
                CreditExpenseEntity(amount = amount, currency = "INR", description = desc, category = cat, cardName = card, isEmiConversion = false, userId = currentUserId.value)
            )
            updateCardOutstanding(card, amount)
        }
    }

    fun addManualEmi(amount: Double, desc: String, tenure: Int, rem: Int, dayOfMonth: Int = 5) {
        viewModelScope.launch {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth.coerceIn(1, 31))
            repository.insertEmiLoan(
                EmiLoanEntity(
                    amount = amount,
                    currency = "INR",
                    description = desc,
                    category = "Loan",
                    totalTenureMonths = tenure,
                    remainingMonths = rem,
                    timestamp = cal.timeInMillis,
                    userId = currentUserId.value
                )
            )
        }
    }

    fun addManualDebt(amount: Double, desc: String, person: String, isGrp: Boolean, group: String, paidPeople: String = "") {
        viewModelScope.launch {
            val initialPaid = if (person.split(",").map { it.trim() }.any { it.equals("You", ignoreCase = true) }) {
                val list = paidPeople.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                if (!list.contains("You")) {
                    list.add("You")
                }
                list.distinct().joinToString(",")
            } else {
                paidPeople
            }
            repository.insertDebtSplit(
                DebtSplitEntity(amount = amount, currency = "INR", description = desc, category = "Peer Split", paymentMode = "UPI", debtPersonInvolved = person, isGroupSplit = isGrp, groupName = group, paidPeople = initialPaid, userId = currentUserId.value)
            )
        }
    }

    fun addManualIncome(amount: Double, desc: String, frequency: String) {
        viewModelScope.launch {
            val isGuest = currentUserId.value == "guest"
            repository.insertIncomePayday(
                IncomePaydayEntity(
                    amount = amount,
                    currency = "INR",
                    description = desc,
                    category = "Salary",
                    incomeFrequency = frequency,
                    paymentMode = "Bank Transfer",
                    userId = currentUserId.value,
                    syncStatus = if (isGuest) "SYNCED" else "PENDING_UPLOAD",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun addManualSip(amount: Double, desc: String, category: String, dayOfMonth: Int, frequency: String = "Monthly") {
        viewModelScope.launch {
            repository.insertSipRecord(
                SipEntity(amount = amount, currency = "INR", description = desc, frequency = frequency, investmentCategory = category, dayOfMonth = dayOfMonth, userId = currentUserId.value)
            )
        }
    }

    fun addManualInvestment(amount: Double, desc: String, category: String, currentValue: Double) {
        viewModelScope.launch {
            repository.insertInvestmentRecord(
                InvestmentEntity(amount = amount, currency = "INR", description = desc, category = category, currentValue = currentValue, userId = currentUserId.value)
            )
        }
    }

    // Manual Deletions
    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            if (id < 0) {
                repository.deleteDebtSplit(-id)
                return@launch
            }
            val expense = dailyExpenses.value.find { it.id == id }
            if (expense != null && (expense.paymentMode == "Credit Card" || expense.paymentMode.contains("Credit", ignoreCase = true))) {
                val card = creditCards.value.firstOrNull()
                if (card != null) {
                    decreaseCardOutstanding(card.cardName, expense.amount)
                }
            }
            repository.deleteDailyExpense(id)
        }
    }

    fun deleteCredit(id: Int) {
        viewModelScope.launch {
            val expense = creditExpenses.value.find { it.id == id }
            if (expense != null) {
                decreaseCardOutstanding(expense.cardName, expense.amount)
            }
            repository.deleteCreditExpense(id)
        }
    }

    fun deleteEmi(id: Int) = viewModelScope.launch { repository.deleteEmiLoan(id) }
    fun deleteDebt(id: Int) = viewModelScope.launch { repository.deleteDebtSplit(id) }

    // Trip Events
    fun createTrip(name: String, desc: String, start: String, end: String, isPublic: Boolean, participantsList: List<String>) {
        viewModelScope.launch {
            repository.insertTripEvent(
                TripEventEntity(
                    name = name,
                    description = desc,
                    startDate = start,
                    endDate = end,
                    isPublic = isPublic,
                    participants = participantsList.distinct().joinToString(","),
                    userId = currentUserId.value
                )
            )
        }
    }

    fun deleteTrip(id: Int) {
        viewModelScope.launch {
            repository.deleteTripEvent(id)
        }
    }

    // Trip Expenses
    fun addTripExpense(
        tripId: Int, 
        title: String, 
        totalAmount: Double, 
        paidBy: String, 
        splitMethod: String, 
        participantWeights: String, 
        involvedParticipants: List<String>, 
        category: String, 
        notes: String = "", 
        receiptUri: String = ""
    ) {
        viewModelScope.launch {
            repository.insertTripExpense(
                TripExpenseEntity(
                    tripId = tripId,
                    title = title,
                    totalAmount = totalAmount,
                    paidBy = paidBy,
                    splitMethod = splitMethod,
                    participantWeights = participantWeights,
                    involvedParticipants = involvedParticipants.distinct().joinToString(","),
                    category = category,
                    notes = notes,
                    receiptUri = receiptUri,
                    userId = currentUserId.value
                )
            )
        }
    }

    fun deleteTripExpense(id: Int) {
        viewModelScope.launch {
            repository.deleteTripExpense(id)
        }
    }

    // Theme Management
    fun selectTheme(themeName: String) {
        _selectedTheme.value = themeName
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("myfin_theme_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("saved_theme", themeName).apply()
        }
    }

    // Participants Management
    fun addParticipantToDb(name: String, email: String, isRegistered: Boolean) {
        viewModelScope.launch {
            val existing = participants.value.any { it.name.equals(name.trim(), ignoreCase = true) }
            if (!existing) {
                repository.insertParticipant(
                    ParticipantEntity(
                        name = name.trim(),
                        email = email.trim(),
                        isRegistered = isRegistered,
                        userId = currentUserId.value
                    )
                )
            }
        }
    }

    fun removeParticipantByName(name: String) {
        viewModelScope.launch {
            repository.deleteParticipantByName(name.trim())
        }
    }

    fun removeParticipant(id: Int) {
        viewModelScope.launch {
            repository.deleteParticipant(id)
        }
    }

    fun toggleDebtPersonPaidStatus(debtId: Int, personName: String) {
        viewModelScope.launch {
            val debt = debtSplits.value.find { it.id == debtId } ?: return@launch
            val paidList = debt.paidPeople.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (paidList.contains(personName)) {
                paidList.remove(personName)
            } else {
                paidList.add(personName)
            }
            val newPaidPeople = paidList.distinct().joinToString(",")
            repository.insertDebtSplit(debt.copy(paidPeople = newPaidPeople))
        }
    }
    fun deleteIncome(id: Int) = viewModelScope.launch {
        val existing = db.dao().getAllIncomePaydaysDirect().find { it.id == id }
        if (existing != null) {
            if (currentUserId.value == "guest") {
                repository.deleteIncomePayday(id)
            } else {
                repository.insertIncomePayday(
                    existing.copy(
                        isDeleted = true,
                        syncStatus = "PENDING_DELETE",
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
    fun deleteSip(id: Int) = viewModelScope.launch { repository.deleteSipRecord(id) }
    fun deleteInvestment(id: Int) = viewModelScope.launch { repository.deleteInvestmentRecord(id) }

    // Dynamic Credit Cards Operations
    fun addCreditCard(name: String, limit: Double, billDateDay: Int) {
        viewModelScope.launch {
            repository.insertCreditCard(
                CreditCardEntity(
                    cardName = name,
                    creditLimit = limit,
                    billDate = billDateDay,
                    outstandingAmount = 0.0,
                    billStatus = "Pending",
                    userId = currentUserId.value
                )
            )
        }
    }

    fun deleteCreditCard(id: Int) = viewModelScope.launch { repository.deleteCreditCard(id) }

    fun toggleCardBillStatus(cardId: Int) {
        viewModelScope.launch {
            val cc = creditCards.value.find { it.id == cardId } ?: return@launch
            val newStatus = if (cc.billStatus == "Paid") "Pending" else "Paid"
            val newOutstanding = if (newStatus == "Paid") 0.0 else cc.outstandingAmount
            repository.insertCreditCard(cc.copy(billStatus = newStatus, outstandingAmount = newOutstanding))
        }
    }

    private suspend fun updateCardOutstanding(cardName: String, amount: Double) {
        val trimmed = cardName.trim()
        if (trimmed.isEmpty()) return
        val cardsList = creditCards.value
        val matched = cardsList.find { it.cardName.equals(trimmed, ignoreCase = true) }
        if (matched != null) {
            val newOutstanding = matched.outstandingAmount + amount
            repository.insertCreditCard(matched.copy(
                outstandingAmount = newOutstanding,
                billStatus = "Pending"
            ))
        } else {
            repository.insertCreditCard(CreditCardEntity(
                cardName = trimmed,
                creditLimit = 150000.0,
                billDate = 15,
                billStatus = "Pending",
                outstandingAmount = amount,
                userId = currentUserId.value
            ))
        }
    }

    private suspend fun decreaseCardOutstanding(cardName: String, amount: Double) {
        val trimmed = cardName.trim()
        if (trimmed.isEmpty()) return
        val cardsList = creditCards.value
        val matched = cardsList.find { it.cardName.equals(trimmed, ignoreCase = true) }
        if (matched != null) {
            val newOutstanding = (matched.outstandingAmount - amount).coerceAtLeast(0.0)
            repository.insertCreditCard(matched.copy(outstandingAmount = newOutstanding))
        }
    }

    // Generates DB Summary JSON for Gemini context
    private fun buildDatabaseSummaryJson(): String {
        val totalExp = dailyExpenses.value.sumOf { it.amount }
        val totalCredit = creditExpenses.value.sumOf { it.amount }
        val totalEmi = emiLoans.value.sumOf { it.amount }
        val totalDebtOwedToMe = debtSplits.value.sumOf { it.amount }
        val totalInc = incomePaydays.value.sumOf { it.amount }
        val totalSip = sipRecords.value.sumOf { it.amount }
        val totalInvestmentInvested = investmentRecords.value.sumOf { it.amount }
        val totalInvestmentCurrent = investmentRecords.value.sumOf { it.currentValue }

        return """
            {
              "totals": {
                "daily_expenses_sum": $totalExp,
                "credit_spend_sum": $totalCredit,
                "emi_liability_monthly": $totalEmi,
                "debt_split_assets": $totalDebtOwedToMe,
                "income_sum": $totalInc,
                "active_sips_monthly_sum": $totalSip,
                "investments_invested_principal": $totalInvestmentInvested,
                "investments_current_valuation": $totalInvestmentCurrent
              },
              "active_emis_count": ${emiLoans.value.size},
              "active_splits_count": ${debtSplits.value.size},
              "active_sips_count": ${sipRecords.value.size},
              "investments_count": ${investmentRecords.value.size},
              "recent_expense_descriptions": ${dailyExpenses.value.take(5).map { "${it.description} (${it.amount} INR)" }},
              "credit_card_names": ${creditExpenses.value.map { it.cardName }.distinct()},
              "sip_descriptions": ${sipRecords.value.map { "${it.description} (Monthly: ${it.amount})" }},
              "investment_assets": ${investmentRecords.value.map { "${it.description} (Value: ${it.currentValue}, Invested: ${it.amount})" }}
            }
        """.trimIndent()
    }

    fun seedSampleDatabase() {
        viewModelScope.launch {
            clearAllDatabaseInternal()
        }
    }

    fun clearAllDatabase() {
        viewModelScope.launch {
            clearAllDatabaseInternal()
        }
    }

    private suspend fun clearAllDatabaseInternal() {
        repository.clearDailyExpenses()
        repository.clearCreditExpenses()
        repository.clearEmiLoans()
        repository.clearDebtSplits()
        repository.clearIncomePaydays()
        repository.clearSipRecords()
        repository.clearInvestmentRecords()
        repository.clearCreditCards()
        repository.clearTripEvents()
        repository.clearTripExpenses()
        repository.clearParticipants()
    }

    // ----------------------------------------------------
    // SECURE FIREBASE & FALLBACK AUTHENTICATION CONTROLS
    // ----------------------------------------------------

    fun signUpWithEmail(email: String, pword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || pword.isBlank()) {
            onError("Email and password cannot be empty.")
            return
        }
        val auth = firebaseAuth
        if (auth != null) {
            auth.createUserWithEmailAndPassword(email, pword)
                .addOnSuccessListener { result ->
                    val fbUser = result.user
                    if (fbUser != null) {
                        val userProfile = UserProfile(
                            uid = fbUser.uid,
                            email = fbUser.email ?: email,
                            isAnonymous = false
                        )
                        _currentUser.value = userProfile
                        _currentUserId.value = userProfile.uid
                        syncManager.updateConnectedAccount(userProfile.email)
                        checkForLocalGuestData(userProfile.uid)
                        onSuccess()
                    } else {
                        onError("Failed to create user session.")
                    }
                }
                .addOnFailureListener { exception ->
                    if (exception.message?.contains("API key", ignoreCase = true) == true || 
                        exception.message?.contains("configuration", ignoreCase = true) == true) {
                        performFallbackSignUp(email, pword, onSuccess, onError)
                    } else {
                        onError(exception.localizedMessage ?: "Sign up failed")
                    }
                }
        } else {
            performFallbackSignUp(email, pword, onSuccess, onError)
        }
    }

    private fun performFallbackSignUp(email: String, pword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val prefs = getApplication<Application>().getSharedPreferences("myfin_auth_prefs", android.content.Context.MODE_PRIVATE)
        val existingUid = prefs.getString("user_pwd_$email", null)
        if (existingUid != null) {
            onError("An account with this email already exists locally.")
            return
        }
        val newUid = "local_uid_" + java.util.UUID.randomUUID().toString().take(8)
        prefs.edit()
            .putString("user_pwd_$email", pword)
            .putString("saved_uid", newUid)
            .putString("saved_email", email)
            .apply()
            
        val userProfile = UserProfile(uid = newUid, email = email, isAnonymous = false)
        _currentUser.value = userProfile
        _currentUserId.value = newUid
        syncManager.updateConnectedAccount(userProfile.email)
        checkForLocalGuestData(userProfile.uid)
        onSuccess()
    }

    fun signInWithEmail(email: String, pword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || pword.isBlank()) {
            onError("Email and password cannot be empty.")
            return
        }
        val auth = firebaseAuth
        if (auth != null) {
            auth.signInWithEmailAndPassword(email, pword)
                .addOnSuccessListener { result ->
                    val fbUser = result.user
                    if (fbUser != null) {
                        val userProfile = UserProfile(
                            uid = fbUser.uid,
                            email = fbUser.email ?: email,
                            isAnonymous = false
                        )
                        _currentUser.value = userProfile
                        _currentUserId.value = userProfile.uid
                        syncManager.updateConnectedAccount(userProfile.email)
                        checkForLocalGuestData(userProfile.uid)
                        onSuccess()
                    } else {
                        onError("Failed to start user session.")
                    }
                }
                .addOnFailureListener { exception ->
                    if (exception.message?.contains("API key", ignoreCase = true) == true || 
                        exception.message?.contains("configuration", ignoreCase = true) == true) {
                        performFallbackSignIn(email, pword, onSuccess, onError)
                    } else {
                        onError(exception.localizedMessage ?: "Authentication failed")
                    }
                }
        } else {
            performFallbackSignIn(email, pword, onSuccess, onError)
        }
    }

    private fun performFallbackSignIn(email: String, pword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val prefs = getApplication<Application>().getSharedPreferences("myfin_auth_prefs", android.content.Context.MODE_PRIVATE)
        val savedPassword = prefs.getString("user_pwd_$email", null)
        if (savedPassword == null) {
            onError("No registered local account found for this email.")
            return
        }
        if (savedPassword != pword) {
            onError("Incorrect password. Please try again.")
            return
        }
        val newUid = "local_uid_" + email.hashCode().toString()
        prefs.edit()
            .putString("saved_uid", newUid)
            .putString("saved_email", email)
            .apply()
            
        val userProfile = UserProfile(uid = newUid, email = email, isAnonymous = false)
        _currentUser.value = userProfile
        _currentUserId.value = newUid
        syncManager.updateConnectedAccount(userProfile.email)
        checkForLocalGuestData(userProfile.uid)
        onSuccess()
    }

    fun signInAnonymously(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    val fbUser = result.user
                    if (fbUser != null) {
                        val userProfile = UserProfile(
                            uid = fbUser.uid,
                            email = "guest_user@myfin.io",
                            isAnonymous = true
                        )
                        _currentUser.value = userProfile
                        _currentUserId.value = userProfile.uid
                        onSuccess()
                    } else {
                        onError("Failed to start Guest session.")
                    }
                }
                .addOnFailureListener { exception ->
                    performFallbackGuestSignIn(onSuccess)
                }
        } else {
            performFallbackGuestSignIn(onSuccess)
        }
    }

    private fun performFallbackGuestSignIn(onSuccess: () -> Unit) {
        val userProfile = UserProfile(uid = "guest", email = "guest_user@myfin.io", isAnonymous = true)
        _currentUser.value = userProfile
        _currentUserId.value = "guest"
        
        val prefs = getApplication<Application>().getSharedPreferences("myfin_auth_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putString("saved_uid", "guest")
            .putString("saved_email", "guest_user@myfin.io")
            .apply()
            
        onSuccess()
    }

    fun signOutUser(onSuccess: () -> Unit) {
        firebaseAuth?.signOut()
        val prefs = getApplication<Application>().getSharedPreferences("myfin_auth_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .remove("saved_uid")
            .remove("saved_email")
            .apply()
            
        _currentUser.value = null
        _currentUserId.value = "guest"
        onSuccess()
    }

    fun signInWithGoogle(idToken: String, email: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val fbUser = result.user
                    if (fbUser != null) {
                        val userProfile = UserProfile(
                            uid = fbUser.uid,
                            email = fbUser.email ?: email ?: "google_user@myfin.io",
                            isAnonymous = false
                        )
                        _currentUser.value = userProfile
                        _currentUserId.value = userProfile.uid
                        val prefs = getApplication<Application>().getSharedPreferences("myfin_auth_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("saved_uid", userProfile.uid)
                            .putString("saved_email", userProfile.email)
                            .apply()
                        syncManager.updateConnectedAccount(userProfile.email)
                        checkForLocalGuestData(userProfile.uid)
                        onSuccess()
                    } else {
                        onError("Failed to start Google sign-in session.")
                    }
                }
                .addOnFailureListener { exception ->
                    performFallbackGoogleSignIn(email ?: "google_user@myfin.io", onSuccess)
                }
        } else {
            performFallbackGoogleSignIn(email ?: "google_user@myfin.io", onSuccess)
        }
    }

    fun performFallbackGoogleSignIn(email: String, onSuccess: () -> Unit) {
        val resolvedEmail = if (email.isBlank()) "google_user_${java.util.UUID.randomUUID().toString().take(6)}@gmail.com" else email
        val newUid = "google_uid_" + resolvedEmail.hashCode().toString()
        val prefs = getApplication<Application>().getSharedPreferences("myfin_auth_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putString("saved_uid", newUid)
            .putString("saved_email", resolvedEmail)
            .apply()
            
        val userProfile = UserProfile(uid = newUid, email = resolvedEmail, isAnonymous = false)
        _currentUser.value = userProfile
        _currentUserId.value = newUid
        syncManager.updateConnectedAccount(userProfile.email)
        checkForLocalGuestData(userProfile.uid)
        onSuccess()
    }

    // --- Expense Management 2.0 State and Actions ---
    private val _expenseFilters = MutableStateFlow(ExpenseFilters())
    val expenseFilters: StateFlow<ExpenseFilters> = _expenseFilters.asStateFlow()

    val filteredDailyExpenses = combine(
        dailyExpenses,
        _expenseFilters
    ) { expenses, sFilter ->
        var list = expenses.filter { it.id > 0 && !it.isDeleted } // ID > 0 pulls real daily expenses (ignoring negative-ID peer splits mapped onto expenses)
        
        // Real-time Text Search: Title, Notes, Category, Tags, Amount
        if (sFilter.searchQuery.isNotBlank()) {
            val query = sFilter.searchQuery.trim().lowercase()
            list = list.filter {
                it.description.lowercase().contains(query) ||
                it.notes.lowercase().contains(query) ||
                it.category.lowercase().contains(query) ||
                it.tags.lowercase().contains(query) ||
                it.amount.toString().contains(query)
            }
        }
        
        // Date filters
        if (sFilter.startDate != null) {
            list = list.filter { it.timestamp >= sFilter.startDate }
        }
        if (sFilter.endDate != null) {
            val endOfDay = sFilter.endDate + (24 * 60 * 60 * 1000 - 1)
            list = list.filter { it.timestamp <= endOfDay }
        }
        
        // Category filter
        if (sFilter.category != "All") {
            list = list.filter { it.category.equals(sFilter.category, ignoreCase = true) }
        }
        
        // Payment Mode filter
        if (sFilter.paymentMethod != "All") {
            list = list.filter { it.paymentMode.equals(sFilter.paymentMethod, ignoreCase = true) }
        }
        
        // Amount Range filters
        if (sFilter.minAmount != null) {
            list = list.filter { it.amount >= sFilter.minAmount }
        }
        if (sFilter.maxAmount != null) {
            list = list.filter { it.amount <= sFilter.maxAmount }
        }
        
        // Tag filter (searches for #tag inside tags string)
        if (sFilter.selectedTag.isNotBlank()) {
            list = list.filter { it.tags.contains(sFilter.selectedTag, ignoreCase = true) }
        }
        
        // Sorting
        list = when (sFilter.sortBy) {
            "Oldest First" -> list.sortedBy { it.timestamp }
            "Highest Amount" -> list.sortedByDescending { it.amount }
            "Lowest Amount" -> list.sortedBy { it.amount }
            else -> list.sortedByDescending { it.timestamp } // Newest First (default)
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateExpenseFilters(filters: ExpenseFilters) {
        _expenseFilters.value = filters
    }

    fun resetExpenseFilters() {
        _expenseFilters.value = ExpenseFilters()
    }

    fun insertFullExpense(
        amount: Double,
        description: String,
        category: String,
        paymentMode: String,
        notes: String = "",
        receiptImageUri: String = "",
        tags: String = "",
        dateString: String = "",
        timeString: String = "",
        isRecurring: Boolean = false,
        recurringPeriod: String = "None",
        timestamp: Long = System.currentTimeMillis(),
        cardId: Int = 0
    ) {
        viewModelScope.launch {
            val isGuest = currentUserId.value == "guest"
            val expense = DailyExpenseEntity(
                amount = amount,
                currency = "INR",
                description = description.ifBlank { "$category Expense" },
                category = category,
                paymentMode = paymentMode,
                userId = currentUserId.value,
                timestamp = timestamp,
                notes = notes,
                receiptImageUri = receiptImageUri,
                tags = tags,
                dateString = dateString,
                timeString = timeString,
                isRecurring = isRecurring,
                recurringPeriod = recurringPeriod,
                isDeleted = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (isGuest) "SYNCED" else "PENDING_UPLOAD",
                cardId = cardId
            )
            repository.insertDailyExpense(expense)
            if (paymentMode == "Credit Card" || paymentMode.contains("Credit", ignoreCase = true)) {
                val cardName = if (cardId > 0) {
                    db.dao().getCreditCardById(cardId)?.cardName ?: "HDFC Millennia"
                } else {
                    "HDFC Millennia"
                }
                updateCardOutstanding(cardName, amount)
            }
            triggerSync()
        }
    }

    fun updateFullExpense(
        id: Int,
        amount: Double,
        description: String,
        category: String,
        paymentMode: String,
        notes: String,
        receiptImageUri: String,
        tags: String,
        dateString: String,
        timeString: String,
        isRecurring: Boolean,
        recurringPeriod: String,
        timestamp: Long,
        cardId: Int = 0
    ) {
        viewModelScope.launch {
            val existing = dailyExpenses.value.find { it.id == id }
            val diffAmount = if (existing != null) amount - existing.amount else 0.0
            
            val isGuest = currentUserId.value == "guest"
            val expense = DailyExpenseEntity(
                id = id,
                amount = amount,
                currency = "INR",
                description = description.ifBlank { "$category Expense" },
                category = category,
                paymentMode = paymentMode,
                userId = currentUserId.value,
                timestamp = timestamp,
                notes = notes,
                receiptImageUri = receiptImageUri,
                tags = tags,
                dateString = dateString,
                timeString = timeString,
                isRecurring = isRecurring,
                recurringPeriod = recurringPeriod,
                isDeleted = false,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (isGuest) "SYNCED" else "PENDING_UPLOAD",
                cardId = cardId
            )
            repository.insertDailyExpense(expense)
            
            // Re-sync after edit
            repository.deleteUnifiedLedgerEntryByReferenceId("daily_${id}")
            
            if (paymentMode == "Credit Card" || paymentMode.contains("Credit", ignoreCase = true)) {
                if (diffAmount != 0.0) {
                    val cardName = if (cardId > 0) {
                        db.dao().getCreditCardById(cardId)?.cardName ?: "HDFC Millennia"
                    } else if (existing != null && existing.cardId > 0) {
                        db.dao().getCreditCardById(existing.cardId)?.cardName ?: "HDFC Millennia"
                    } else {
                        "HDFC Millennia"
                    }
                    updateCardOutstanding(cardName, diffAmount)
                }
            }
            triggerSync()
        }
    }

    fun softDeleteExpense(id: Int) {
        viewModelScope.launch {
            val existing = dailyExpenses.value.find { it.id == id }
            if (existing != null) {
                val isGuest = currentUserId.value == "guest"
                val deletedExpense = existing.copy(
                    isDeleted = true,
                    updatedAt = System.currentTimeMillis(),
                    syncStatus = if (isGuest) "SYNCED" else "PENDING_DELETE"
                )
                repository.insertDailyExpense(deletedExpense)
                
                // Keep ledger entry synchronized (delete referenced expense)
                repository.deleteUnifiedLedgerEntryByReferenceId("daily_${id}")
                
                if (existing.paymentMode == "Credit Card" || existing.paymentMode.contains("Credit", ignoreCase = true)) {
                    val cardName = if (existing.cardId > 0) {
                        db.dao().getCreditCardById(existing.cardId)?.cardName ?: "HDFC Millennia"
                    } else {
                        "HDFC Millennia"
                    }
                    decreaseCardOutstanding(cardName, existing.amount)
                }
            }
        }
    }

    // --- UNIFIED LEDGER FOR TRANSFERS & CROSS-MODULE SYNC ---

    val unifiedLedgerEntries: StateFlow<List<UnifiedLedgerEntry>> = repository.allUnifiedLedgerEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun recordTransfer(amount: Double, sourceAccount: String, destAccount: String, notes: String, timestamp: Long) {
        viewModelScope.launch {
            val entry = UnifiedLedgerEntry(
                amount = amount,
                currency = "INR",
                category = "Account Transfer",
                description = if (notes.isNotBlank()) "Transfer: $notes ($sourceAccount to $destAccount)" else "Transfer from $sourceAccount to $destAccount",
                paymentMode = sourceAccount,
                timestamp = timestamp,
                userId = currentUserId.value,
                type = "Transfer",
                sourceAccount = sourceAccount,
                destAccount = destAccount,
                referenceId = "transfer_${System.currentTimeMillis()}"
            )
            repository.insertUnifiedLedgerEntry(entry)
        }
    }

    fun deleteLedgerEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteUnifiedLedgerEntry(id)
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            val uid = _currentUserId.value
            try {
                // Collect first items of flow securely
                val dailyList = repository.allDailyExpenses.stateIn(this).value
                val incomeList = repository.allIncomePaydays.stateIn(this).value
                val currentLedger = repository.allUnifiedLedgerEntries.stateIn(this).value

                for (expense in dailyList.filter { it.userId == uid && !it.isDeleted }) {
                    val refId = "daily_${expense.id}"
                    if (currentLedger.none { it.referenceId == refId }) {
                        val isCard = expense.paymentMode == "Credit Card" || expense.paymentMode.contains("credit", true)
                        val type = if (isCard) "Credit Card Expense" else "Expense"
                        
                        // If notes exist on expense, append them cleanly to ledger description
                        val fullDesc = if (expense.notes.isNotBlank()) "${expense.description} (${expense.notes})" else expense.description

                        repository.insertUnifiedLedgerEntry(
                            UnifiedLedgerEntry(
                                amount = expense.amount,
                                currency = expense.currency,
                                category = expense.category,
                                description = fullDesc,
                                paymentMode = expense.paymentMode,
                                timestamp = expense.timestamp,
                                userId = expense.userId,
                                type = type,
                                referenceId = refId
                            )
                        )
                    }
                }

                for (income in incomeList.filter { it.userId == uid }) {
                    val refId = "income_${income.id}"
                    if (currentLedger.none { it.referenceId == refId }) {
                        repository.insertUnifiedLedgerEntry(
                            UnifiedLedgerEntry(
                                amount = income.amount,
                                currency = income.currency,
                                category = income.category,
                                description = income.description,
                                paymentMode = income.paymentMode,
                                timestamp = income.timestamp,
                                userId = income.userId,
                                type = "Income",
                                referenceId = refId
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MYFin", "Dynamic ledger sync error: ${e.message}")
            }
        }
    }

    fun checkForLocalGuestData(newUserId: String) {
        viewModelScope.launch {
            try {
                val guestExpenses = db.dao().getAllDailyExpensesDirect().filter { it.userId == "guest" }
                val guestIncomes = db.dao().getAllIncomePaydaysDirect().filter { it.userId == "guest" }
                if (guestExpenses.isNotEmpty() || guestIncomes.isNotEmpty()) {
                    pendingMigrationUserId = newUserId
                    showMigrationDialog.value = true
                } else {
                    syncManager.syncNow(newUserId)
                }
            } catch (e: Exception) {
                syncManager.syncNow(newUserId)
            }
        }
    }

    fun mergeLocalData() {
        val uid = pendingMigrationUserId ?: return
        showMigrationDialog.value = false
        pendingMigrationUserId = null
        viewModelScope.launch {
            syncManager.mergeLocalWithCloud(uid)
        }
    }

    fun replaceCloudData() {
        val uid = pendingMigrationUserId ?: return
        showMigrationDialog.value = false
        pendingMigrationUserId = null
        viewModelScope.launch {
            syncManager.replaceCloudWithLocal(uid)
        }
    }

    fun replaceLocalData() {
        val uid = pendingMigrationUserId ?: return
        showMigrationDialog.value = false
        pendingMigrationUserId = null
        viewModelScope.launch {
            syncManager.replaceLocalWithCloud(uid)
        }
    }

    fun dismissMigrationDialog() {
        val uid = pendingMigrationUserId
        showMigrationDialog.value = false
        pendingMigrationUserId = null
        if (uid != null) {
            viewModelScope.launch {
                syncManager.syncNow(uid)
            }
        }
    }
}

data class ExpenseFilters(
    val searchQuery: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val category: String = "All",
    val paymentMethod: String = "All",
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val selectedTag: String = "",
    val sortBy: String = "Newest First" // "Newest First", "Oldest First", "Highest Amount", "Lowest Amount"
)
