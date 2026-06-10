package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface LoanUiState {
    object Loading : LoanUiState
    data class Success(
        val loans: List<LoanEntity>,
        val schedules: List<LoanScheduleEntity>,
        val totalOutstanding: Double,
        val totalPrincipal: Double,
        val overallProgressPercentage: Double,
        val upcomingEMIs: List<LoanScheduleEntity>,
        val dashboardSummary: LoanDashboardSummary?
    ) : LoanUiState
    data class Error(val message: String) : LoanUiState
}

data class LoanDashboardSummary(
    val nearestEmiLoanName: String,
    val amountDue: Double,
    val daysRemaining: Int,
    val dueDate: Long,
    val loanId: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
class LoanViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = LoanRepository(db.dao())

    private val _userId = MutableStateFlow("guest")
    val userId: StateFlow<String> = _userId.asStateFlow()

    val uiState: StateFlow<LoanUiState> = _userId.flatMapLatest { uid ->
        combine(
            repository.getAllLoans(uid),
            repository.getAllSchedulesForUser(uid)
        ) { loans, schedules ->
            val totalOutstanding = loans.sumOf { it.outstandingBalance }
            val totalPrincipal = loans.sumOf { it.principalAmount }
            val progress = if (totalPrincipal > 0.0) {
                (1.0 - (totalOutstanding / totalPrincipal)) * 100.0
            } else {
                0.0
            }

            // Upcoming EMIs are schedules that are Pending sorted by due date
            val upcomingSchedules = schedules.filter { 
                it.paymentStatus != "Paid" && it.paymentStatus != "Completed" && it.paymentStatus != "Prepaid"
            }

            // Find nearest EMI due
            val nowTime = System.currentTimeMillis()
            val nearestSchedule = upcomingSchedules.minByOrNull { it.dueDate }
            
            val dashboardSummary = nearestSchedule?.let { sch ->
                val matchingLoan = loans.find { it.id == sch.loanId }
                val daysRemaining = ((sch.dueDate - nowTime) / (1000 * 60 * 60 * 24)).toInt()
                LoanDashboardSummary(
                    nearestEmiLoanName = matchingLoan?.loanName ?: "Loan EMI",
                    amountDue = sch.emiAmount,
                    daysRemaining = daysRemaining.coerceAtLeast(0),
                    dueDate = sch.dueDate,
                    loanId = sch.loanId
                )
            }

            LoanUiState.Success(
                loans = loans,
                schedules = schedules,
                totalOutstanding = totalOutstanding,
                totalPrincipal = totalPrincipal,
                overallProgressPercentage = progress,
                upcomingEMIs = upcomingSchedules,
                dashboardSummary = dashboardSummary
            ) as LoanUiState
        }
    }.catch { e ->
        emit(LoanUiState.Error(e.localizedMessage ?: "Failed loading loan and EMI systems."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoanUiState.Loading
    )

    fun addLoan(
        loanName: String,
        loanType: String,
        lenderName: String,
        principalAmount: Double,
        interestRate: Double,
        tenureMonths: Int,
        startDate: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (loanName.isBlank() || lenderName.isBlank()) {
                    onResult(false, "Loan and Lender names are required.")
                    return@launch
                }
                if (principalAmount <= 0) {
                    onResult(false, "Principal amount must be greater than 0.")
                    return@launch
                }
                if (tenureMonths <= 0) {
                    onResult(false, "Tenure must be at least 1 month.")
                    return@launch
                }

                val computedEmi = repository.computeEmi(principalAmount, interestRate, tenureMonths)

                val loan = LoanEntity(
                    loanName = loanName,
                    loanType = loanType,
                    lenderName = lenderName,
                    principalAmount = principalAmount,
                    interestRate = interestRate,
                    tenureMonths = tenureMonths,
                    startDate = startDate,
                    emiAmount = computedEmi,
                    outstandingBalance = principalAmount,
                    status = "Active",
                    userId = _userId.value
                )

                val loanId = repository.insertLoan(loan)
                repository.generateAndSaveSchedule(loanId.toInt(), loan.copy(id = loanId.toInt()))
                onResult(true, "Loan created successfully.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to generate loan.")
            }
        }
    }

    fun recordPayment(
        loanId: Int,
        amount: Double,
        paymentMethod: String,
        paymentType: String,
        notes: String = "",
        paymentDate: Long = System.currentTimeMillis(),
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.recordLoanPayment(
                    loanId = loanId,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    paymentType = paymentType,
                    notes = notes,
                    paymentDate = paymentDate
                )
                onResult(true, "Payment recorded successfully.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to record payment.")
            }
        }
    }

    fun deleteLoan(loanId: Int) {
        viewModelScope.launch {
            repository.deleteLoanAndSchedules(loanId)
        }
    }
}
