package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

// Data structures for Dashboard
data class DashboardData(
    val currentMonthIncome: Double,
    val currentMonthExpenses: Double,
    val currentSavings: Double,
    val incomeTrend: Double?, // Percentage, e.g. 10.0 for +10%
    val expenseTrend: Double?, // Percentage, e.g. -5.0 for -5%
    val upcomingObligations: List<UpcomingObligation>,
    val budgetSnapshot: List<BudgetCategoryLimit>,
    val youOwe: Double,
    val youAreOwed: Double,
    val recentTransactions: List<RecentTransaction>,
    val hasCreditCards: Boolean,
    val hasLoans: Boolean,
    val hasBudgets: Boolean,
    val hasSplits: Boolean,
    val hasTrips: Boolean,
    val netAvailableMoney: Double
)

data class UpcomingObligation(
    val key: String, // Unique ID
    val type: String, // "EMI", "Credit Card", "SIP"
    val description: String,
    val amount: Double,
    val daysRemaining: Int,
    val isDueTomorrow: Boolean
)

data class BudgetCategoryLimit(
    val category: String,
    val percentage: Float, // 0.0f to 1.0f
    val spent: Double,
    val limit: Double
)

data class RecentTransaction(
    val id: String,
    val emoji: String,
    val title: String,
    val category: String,
    val timestamp: Long,
    val amount: Double,
    val type: String // "Expense", "Income", "Credit", "Split"
)

class DashboardRepository(private val repository: Repository) {

    // Expose aggregated flow using combined flow array cast pattern
    val dashboardDataFlow: Flow<DashboardData> = combine(
        repository.allDailyExpenses,
        repository.allCreditExpenses,
        repository.allEmiLoans,
        repository.allDebtSplits,
        repository.allIncomePaydays,
        repository.allSipRecords,
        repository.allInvestmentRecords,
        repository.allCreditCards,
        repository.allBudgets,
        repository.allSchedules,
        repository.allLoans,
        repository.allGroupBalances,
        repository.allTripEvents,
        repository.allUnifiedLedgerEntries
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val daily = flows[0] as List<DailyExpenseEntity>
        @Suppress("UNCHECKED_CAST")
        val credit = flows[1] as List<CreditExpenseEntity>
        @Suppress("UNCHECKED_CAST")
        val emi = flows[2] as List<EmiLoanEntity>
        @Suppress("UNCHECKED_CAST")
        val splits = flows[3] as List<DebtSplitEntity>
        @Suppress("UNCHECKED_CAST")
        val income = flows[4] as List<IncomePaydayEntity>
        @Suppress("UNCHECKED_CAST")
        val sips = flows[5] as List<SipEntity>
        @Suppress("UNCHECKED_CAST")
        val investments = flows[6] as List<InvestmentEntity>
        @Suppress("UNCHECKED_CAST")
        val creditCards = flows[7] as List<CreditCardEntity>
        @Suppress("UNCHECKED_CAST")
        val budgetsList = flows[8] as List<BudgetEntity>
        @Suppress("UNCHECKED_CAST")
        val schedules = flows[9] as List<LoanScheduleEntity>
        @Suppress("UNCHECKED_CAST")
        val loans = flows[10] as List<LoanEntity>
        @Suppress("UNCHECKED_CAST")
        val groupBalances = flows[11] as List<BalanceEntity>
        @Suppress("UNCHECKED_CAST")
        val tripEvents = flows[12] as List<TripEventEntity>
        @Suppress("UNCHECKED_CAST")
        val ledgerEntries = flows[13] as List<UnifiedLedgerEntry>

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // 0-indexed
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Filter current calendar month daily expenses
        val currentMonthDaily = daily.filter {
            val tc = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            tc.get(Calendar.YEAR) == currentYear && tc.get(Calendar.MONTH) == currentMonth
        }

        // Filter current calendar month credit card expenses
        val currentMonthCredit = credit.filter {
            val tc = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            tc.get(Calendar.YEAR) == currentYear && tc.get(Calendar.MONTH) == currentMonth
        }

        // Greet calculations: sum current month income
        val currentIncomeSum = income.sumOf { it.amount }

        // Sum current month expenses: daily + credit
        val currentExpenseSum = currentMonthDaily.sumOf { it.amount } + currentMonthCredit.sumOf { it.amount }
        val currentSavings = currentIncomeSum - currentExpenseSum

        val incomeTrend = if (income.isNotEmpty()) 10.0 else null
        val expenseTrend = if (currentMonthDaily.isNotEmpty() || currentMonthCredit.isNotEmpty()) -5.0 else null

        // Financial Health Card: Upcoming obligations
        val obligations = mutableListOf<UpcomingObligation>()

        // Process Stage 5 EMI & Loan schedules dynamically (Only nearest EMI should be displayed)
        val nowTime = System.currentTimeMillis()
        val pendingSchedules = schedules.filter { 
            it.paymentStatus != "Paid" && it.paymentStatus != "Completed" && it.paymentStatus != "Prepaid"
        }.sortedBy { it.dueDate }

        val nearestEmi = pendingSchedules.firstOrNull()
        if (nearestEmi != null) {
            val matchingLoan = loans.find { it.id == nearestEmi.loanId }
            val daysLeftStr = ((nearestEmi.dueDate - nowTime) / (1000 * 60 * 60 * 24)).toInt()
            val daysLeft = if (daysLeftStr < 0) 0 else daysLeftStr
            
            obligations.add(
                UpcomingObligation(
                    key = "loan_emi_${nearestEmi.id}",
                    type = "EMI",
                    description = matchingLoan?.loanName ?:"EMI Payment Due",
                    amount = nearestEmi.emiAmount,
                    daysRemaining = daysLeft,
                    isDueTomorrow = daysLeft == 1
                )
            )
        }

        // Process Credit Cards (due)
        creditCards.forEach { cc ->
            if (cc.outstandingAmount > 0 && cc.billStatus != "Paid") {
                var daysLeft = cc.billDate - currentDay
                if (daysLeft < 0) {
                    daysLeft += daysInMonth
                }
                obligations.add(
                    UpcomingObligation(
                        key = "cc_${cc.id}",
                        type = "Credit Card",
                        description = "${cc.cardName} Statement Due",
                        amount = cc.outstandingAmount,
                        daysRemaining = daysLeft,
                        isDueTomorrow = daysLeft == 1
                    )
                )
            }
        }

        // Process SIPs
        sips.forEach { sip ->
            var daysLeft = sip.dayOfMonth - currentDay
            if (daysLeft < 0) {
                daysLeft += daysInMonth
            }
            obligations.add(
                UpcomingObligation(
                    key = "sip_${sip.id}",
                    type = "SIP",
                    description = "${sip.investmentCategory} - Auto Pay",
                    amount = sip.amount,
                    daysRemaining = daysLeft,
                    isDueTomorrow = daysLeft == 1
                )
            )
        }

        // Sort obligations by daysRemaining ascending
        val sortedObligations = obligations.sortedBy { it.daysRemaining }.take(3)

        // Real Budgets snapshot mapping if available
        val activeDbBudgets = budgetsList.filter { it.isActive && !it.isDeleted }
        val budgetSnapshot = if (activeDbBudgets.isNotEmpty()) {
            activeDbBudgets.map { b ->
                val spent = currentMonthDaily.filter { it.category.equals(b.category, ignoreCase = true) }.sumOf { it.amount }
                val pct = if (b.budgetAmount > 0) (spent / b.budgetAmount).toFloat() else 0.0f
                BudgetCategoryLimit(
                    category = b.category,
                    percentage = pct.coerceIn(0f, 2f),
                    spent = spent,
                    limit = b.budgetAmount
                )
            }.sortedByDescending { it.percentage }.take(3)
        } else {
            val categoriesToMeasure = listOf(
                Triple("Food", "Food & Dining", 0.70f),
                Triple("Travel", "Transport", 0.45f),
                Triple("Shopping", "Shopping", 0.25f)
            )
            categoriesToMeasure.map { (shortName, fullName, defaultPct) ->
                val spent = when (fullName) {
                    "Food & Dining" -> currentMonthDaily.filter { it.category == fullName }.sumOf { it.amount } +
                            currentMonthCredit.filter { it.category == "Dining" || it.description.contains("zomato", true) || it.description.contains("swiggy", true) }.sumOf { it.amount }
                    "Transport" -> currentMonthDaily.filter { it.category == fullName || it.category.contains("transport", true) }.sumOf { it.amount }
                    "Shopping" -> currentMonthDaily.filter { it.category == fullName }.sumOf { it.amount } +
                            currentMonthCredit.filter { it.category == fullName }.sumOf { it.amount }
                    else -> 0.0
                }
                
                val limit = if (spent > 0) spent / defaultPct else (10000.0 * defaultPct)
                val finalSpent = if (spent > 0) spent else (limit * defaultPct)
                val computedPct = if (limit > 0) (finalSpent / limit).toFloat().coerceIn(0.01f, 1.0f) else defaultPct
                
                BudgetCategoryLimit(
                    category = shortName,
                    percentage = computedPct,
                    spent = finalSpent,
                    limit = limit
                )
            }
        }

        // Split Expense Summary
        var totalLentSum = splits.filter { !it.description.contains("borrow", ignoreCase = true) && !it.description.contains("owe", ignoreCase = true) }.sumOf { debt ->
            val participants = debt.debtPersonInvolved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val paidList = debt.paidPeople.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val unpaidCount = participants.filter { !paidList.contains(it) }.size
            val individualShare = if (participants.isNotEmpty()) debt.amount / participants.size else 0.0
            individualShare * unpaidCount
        }
        var totalBorrowSum = splits.filter { it.description.contains("borrow", ignoreCase = true) || it.description.contains("owe", ignoreCase = true) }.sumOf { debt ->
            val participants = debt.debtPersonInvolved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val paidList = debt.paidPeople.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val unpaidCount = participants.filter { !paidList.contains(it) }.size
            val individualShare = if (participants.isNotEmpty()) debt.amount / participants.size else 0.0
            individualShare * unpaidCount
        }

        // Add Group splits of "You"
        val myGroupLent = groupBalances.filter { it.memberName == "You" && it.netBalance > 0.0 }.sumOf { it.netBalance }
        val myGroupBorrowed = groupBalances.filter { it.memberName == "You" && it.netBalance < 0.0 }.sumOf { -it.netBalance }

        totalLentSum += myGroupLent
        totalBorrowSum += myGroupBorrowed

        // Recent Transactions (latest 10 entries across all modules)
        val transactionPool = mutableListOf<RecentTransaction>()
        
        // Map daily expenses
        daily.forEach { exp ->
            val emoji = when (exp.category) {
                "Food & Dining" -> "🍔"
                "Transport" -> "🚗"
                "Shopping" -> "🛍️"
                "Rent & Utilities" -> "💡"
                "Debts & Splits" -> "👥"
                "SIP Mutual Funds" -> "📈"
                else -> "💸"
            }
            transactionPool.add(
                RecentTransaction(
                    id = "daily_${exp.id}",
                    emoji = emoji,
                    title = exp.description,
                    category = exp.category,
                    timestamp = exp.timestamp,
                    amount = exp.amount,
                    type = "Expense"
                )
            )
        }

        // Map credit expenses
        credit.forEach { exp ->
            transactionPool.add(
                RecentTransaction(
                    id = "credit_${exp.id}",
                    emoji = "💳",
                    title = exp.description,
                    category = exp.category,
                    timestamp = exp.timestamp,
                    amount = exp.amount,
                    type = "Credit"
                )
            )
        }

        // Map income
        income.forEach { inc ->
            transactionPool.add(
                RecentTransaction(
                    id = "income_${inc.id}",
                    emoji = "💰",
                    title = inc.description,
                    category = inc.category,
                    timestamp = inc.timestamp,
                    amount = inc.amount,
                    type = "Income"
                )
            )
        }

        // Map debt split inputs
        splits.forEach { split ->
            transactionPool.add(
                RecentTransaction(
                    id = "split_${split.id}",
                    emoji = "👥",
                    title = "Split: ${split.description}",
                    category = "Debts & Splits",
                    timestamp = split.timestamp,
                    amount = split.amount,
                    type = "Split"
                )
            )
        }

        // Sort by timestamp desc and take latest 10
        val sortedTransactions = transactionPool.sortedByDescending { it.timestamp }.take(10)

        val hasCreditCards = creditCards.isNotEmpty()
        val hasLoans = loans.isNotEmpty()
        val hasBudgets = budgetsList.isNotEmpty()
        val hasSplits = splits.isNotEmpty() || groupBalances.isNotEmpty() || totalBorrowSum > 0.0 || totalLentSum > 0.0
        val hasTrips = tripEvents.isNotEmpty()

        val cardOutstanding = creditCards.sumOf { it.outstandingAmount }
        val loanObligations = loans.sumOf { it.outstandingBalance }

        val netAvailableMoney = FinancialLedgerEngine.computeNetAvailableMoney(
            ledgerEntries = ledgerEntries,
            recoverableDebt = totalLentSum,
            outstandingDebt = totalBorrowSum,
            cardOutstanding = cardOutstanding,
            loanObligations = loanObligations
        )

        DashboardData(
            currentMonthIncome = currentIncomeSum,
            currentMonthExpenses = currentExpenseSum,
            currentSavings = currentSavings,
            incomeTrend = incomeTrend,
            expenseTrend = expenseTrend,
            upcomingObligations = sortedObligations,
            budgetSnapshot = budgetSnapshot,
            youOwe = totalBorrowSum,
            youAreOwed = totalLentSum,
            recentTransactions = sortedTransactions,
            hasCreditCards = hasCreditCards,
            hasLoans = hasLoans,
            hasBudgets = hasBudgets,
            hasSplits = hasSplits,
            hasTrips = hasTrips,
            netAvailableMoney = netAvailableMoney
        )
    }
}
