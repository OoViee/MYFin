package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
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

class WealthPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: Repository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = Repository(database.dao())
    }

    // Room Streams
    val dailyExpenses = repository.allDailyExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val creditExpenses = repository.allCreditExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val emiLoans = repository.allEmiLoans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val debtSplits = repository.allDebtSplits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val incomePaydays = repository.allIncomePaydays.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sipRecords = repository.allSipRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val investmentRecords = repository.allInvestmentRecords.stateIn(
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
    fun addManualExpense(amount: Double, desc: String, cat: String, mode: String) {
        viewModelScope.launch {
            repository.insertDailyExpense(
                DailyExpenseEntity(amount = amount, currency = "INR", description = desc, category = cat, paymentMode = mode)
            )
        }
    }

    fun addManualCredit(amount: Double, desc: String, card: String, cat: String) {
        viewModelScope.launch {
            repository.insertCreditExpense(
                CreditExpenseEntity(amount = amount, currency = "INR", description = desc, category = cat, cardName = card, isEmiConversion = false)
            )
        }
    }

    fun addManualEmi(amount: Double, desc: String, tenure: Int, rem: Int) {
        viewModelScope.launch {
            repository.insertEmiLoan(
                EmiLoanEntity(amount = amount, currency = "INR", description = desc, category = "Loan", totalTenureMonths = tenure, remainingMonths = rem)
            )
        }
    }

    fun addManualDebt(amount: Double, desc: String, person: String, isGrp: Boolean, group: String) {
        viewModelScope.launch {
            repository.insertDebtSplit(
                DebtSplitEntity(amount = amount, currency = "INR", description = desc, category = "Peer Split", paymentMode = "UPI", debtPersonInvolved = person, isGroupSplit = isGrp, groupName = group)
            )
        }
    }

    fun addManualIncome(amount: Double, desc: String, frequency: String) {
        viewModelScope.launch {
            repository.insertIncomePayday(
                IncomePaydayEntity(amount = amount, currency = "INR", description = desc, category = "Salary", incomeFrequency = frequency, paymentMode = "Bank Transfer")
            )
        }
    }

    fun addManualSip(amount: Double, desc: String, category: String, dayOfMonth: Int, frequency: String = "Monthly") {
        viewModelScope.launch {
            repository.insertSipRecord(
                SipEntity(amount = amount, currency = "INR", description = desc, frequency = frequency, investmentCategory = category, dayOfMonth = dayOfMonth)
            )
        }
    }

    fun addManualInvestment(amount: Double, desc: String, category: String, currentValue: Double) {
        viewModelScope.launch {
            repository.insertInvestmentRecord(
                InvestmentEntity(amount = amount, currency = "INR", description = desc, category = category, currentValue = currentValue)
            )
        }
    }

    // Manual Deletions
    fun deleteExpense(id: Int) = viewModelScope.launch { repository.deleteDailyExpense(id) }
    fun deleteCredit(id: Int) = viewModelScope.launch { repository.deleteCreditExpense(id) }
    fun deleteEmi(id: Int) = viewModelScope.launch { repository.deleteEmiLoan(id) }
    fun deleteDebt(id: Int) = viewModelScope.launch { repository.deleteDebtSplit(id) }
    fun deleteIncome(id: Int) = viewModelScope.launch { repository.deleteIncomePayday(id) }
    fun deleteSip(id: Int) = viewModelScope.launch { repository.deleteSipRecord(id) }
    fun deleteInvestment(id: Int) = viewModelScope.launch { repository.deleteInvestmentRecord(id) }

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

            // 1. Daily Expenses
            repository.insertDailyExpense(DailyExpenseEntity(amount = 250.0, currency = "INR", description = "Zomato Gourmet Dinner", category = "Food & Dining", paymentMode = "UPI"))
            repository.insertDailyExpense(DailyExpenseEntity(amount = 120.0, currency = "INR", description = "Uber Taxi to Corporate Office", category = "Transport", paymentMode = "UPI"))
            repository.insertDailyExpense(DailyExpenseEntity(amount = 800.0, currency = "INR", description = "Monthly Electricity Bill", category = "Utilities", paymentMode = "Debit Card"))
            repository.insertDailyExpense(DailyExpenseEntity(amount = 1200.0, currency = "INR", description = "Sujata Organic Groceries", category = "Groceries", paymentMode = "Cash"))

            // 2. Credit Expenses
            repository.insertCreditExpense(CreditExpenseEntity(amount = 3500.0, currency = "INR", description = "Zara Designer Jacket", category = "Shopping", cardName = "HDFC Millennia", isEmiConversion = false))
            repository.insertCreditExpense(CreditExpenseEntity(amount = 1200.0, currency = "INR", description = "PVR IMAX Movie Ticket", category = "Entertainment", cardName = "ICICI Amazon", isEmiConversion = false))
            repository.insertCreditExpense(CreditExpenseEntity(amount = 25000.0, currency = "INR", description = "Apple iPad Mini", category = "Electronics", cardName = "Axis Magnus", isEmiConversion = true))

            // 3. EMIs
            repository.insertEmiLoan(EmiLoanEntity(amount = 8000.0, currency = "INR", description = "HDFC Car Loan Liability", category = "Loan", totalTenureMonths = 36, remainingMonths = 24))
            repository.insertEmiLoan(EmiLoanEntity(amount = 3200.0, currency = "INR", description = "Samsung TV No-Cost EMI", category = "Electronics", totalTenureMonths = 6, remainingMonths = 3))

            // 4. Splits
            repository.insertDebtSplit(DebtSplitEntity(amount = 450.0, currency = "INR", description = "Friday Office Meal Split", category = "Peer Split", paymentMode = "UPI", debtPersonInvolved = "Amit", isGroupSplit = true, groupName = "Office Lunch"))
            repository.insertDebtSplit(DebtSplitEntity(amount = 1500.0, currency = "INR", description = "Goa Villa booking share", category = "Peer Split", paymentMode = "UPI", debtPersonInvolved = "Neha", isGroupSplit = true, groupName = "Goa Trip 2026"))

            // 5. Income Paydays
            repository.insertIncomePayday(IncomePaydayEntity(amount = 75000.0, currency = "INR", description = "Intellect Corp Monthly Salary", category = "Salary", incomeFrequency = "Monthly", paymentMode = "Bank Transfer"))
            repository.insertIncomePayday(IncomePaydayEntity(amount = 12000.0, currency = "INR", description = "Web Design Consulting Bonus", category = "Freelance", incomeFrequency = "One-off", paymentMode = "UPI"))

            // 6. SIPs
            repository.insertSipRecord(SipEntity(amount = 5000.0, currency = "INR", description = "Nippon India Growth SIP", frequency = "Monthly", investmentCategory = "Mutual Funds", dayOfMonth = 5))
            repository.insertSipRecord(SipEntity(amount = 3000.0, currency = "INR", description = "Parag Parikh Flexi Cap SIP", frequency = "Monthly", investmentCategory = "Mutual Funds", dayOfMonth = 10))

            // 7. Investments
            repository.insertInvestmentRecord(InvestmentEntity(amount = 40000.0, currency = "INR", description = "INFY Stock Equity portfolio", category = "Equity", currentValue = 46500.0))
            repository.insertInvestmentRecord(InvestmentEntity(amount = 15000.0, currency = "INR", description = "SGB Sovereign Gold Bond", category = "Gold", currentValue = 18200.0))
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
    }
}
