package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlin.math.ln
import kotlin.math.pow

class LoanRepository(private val dao: WealthPulseDao) {

    // Loan CRUD
    fun getAllLoans(userId: String = "guest"): Flow<List<LoanEntity>> = dao.getAllLoans(userId)

    suspend fun getLoanById(id: Int): LoanEntity? = dao.getLoanById(id)

    suspend fun insertLoan(loan: LoanEntity): Long = dao.insertLoan(loan)

    suspend fun deleteLoanAndSchedules(loanId: Int) {
        dao.deleteLoan(loanId)
        dao.deleteSchedulesForLoan(loanId)
    }

    // Amortization Schedule Generation
    fun computeEmi(principal: Double, annualRate: Double, tenureMonths: Int): Double {
        if (tenureMonths <= 0) return 0.0
        if (annualRate <= 0.0) return principal / tenureMonths
        val r = annualRate / 100.0 / 12.0
        val base = 1.0 + r
        val power = base.pow(tenureMonths)
        return (principal * r * power) / (power - 1)
    }

    suspend fun generateAndSaveSchedule(loanId: Int, loan: LoanEntity) {
        dao.deleteSchedulesForLoan(loanId)
        val schedules = mutableListOf<LoanScheduleEntity>()
        var remainingPrincipal = loan.principalAmount
        val r = (loan.interestRate / 100.0) / 12.0
        val emi = loan.emiAmount
        
        val cal = Calendar.getInstance()
        cal.timeInMillis = loan.startDate

        for (i in 1..loan.tenureMonths) {
            val interestComp = if (r > 0.0) remainingPrincipal * r else 0.0
            val principalComp = if (r > 0.0) (emi - interestComp).coerceIn(0.0, remainingPrincipal) else emi.coerceIn(0.0, remainingPrincipal)
            
            cal.add(Calendar.MONTH, 1)
            val dueDate = cal.timeInMillis

            schedules.add(
                LoanScheduleEntity(
                    loanId = loanId,
                    installmentNumber = i,
                    dueDate = dueDate,
                    emiAmount = emi,
                    principalComponent = principalComp,
                    interestComponent = interestComp,
                    paymentStatus = "Pending",
                    userId = loan.userId
                )
            )
            remainingPrincipal = (remainingPrincipal - principalComp).coerceAtLeast(0.0)
        }
        dao.insertLoanSchedules(schedules)
    }

    // Schedules & Payments
    fun getSchedulesForLoan(loanId: Int): Flow<List<LoanScheduleEntity>> = dao.getSchedulesForLoan(loanId)

    fun getPaymentsForLoan(loanId: Int): Flow<List<LoanPaymentEntity>> = dao.getPaymentsForLoan(loanId)

    fun getAllSchedulesForUser(userId: String = "guest"): Flow<List<LoanScheduleEntity>> = dao.getAllSchedulesForUser(userId)

    fun getUpcomingPendingSchedules(userId: String = "guest"): Flow<List<LoanScheduleEntity>> = dao.getUpcomingPendingSchedules(userId)

    // Primary action: Complete Payment
    suspend fun recordLoanPayment(
        loanId: Int,
        amount: Double,
        paymentMethod: String,
        paymentType: String, // "Regular", "Prepayment", "Foreclosure"
        notes: String = "",
        paymentDate: Long = System.currentTimeMillis()
    ) {
        val loan = dao.getLoanById(loanId) ?: return
        val currentSchedules = dao.getSchedulesForLoanSync(loanId)

        if (paymentType == "Foreclosure") {
            // Full repayment
            val fullAmount = loan.outstandingBalance
            dao.insertLoanPayment(
                LoanPaymentEntity(
                    loanId = loanId,
                    installmentNumber = 0,
                    paymentDate = paymentDate,
                    amountPaid = fullAmount,
                    paymentMethod = paymentMethod,
                    notes = notes,
                    paymentType = "Foreclosure",
                    userId = loan.userId
                )
            )

            // Update Loan
            val updatedLoan = loan.copy(
                outstandingBalance = 0.0,
                status = "Closed",
                updatedAt = System.currentTimeMillis()
            )
            dao.insertLoan(updatedLoan)

            // Mark all pending as Paid
            val updatedSchedules = currentSchedules.map {
                if (it.paymentStatus == "Pending") it.copy(paymentStatus = "Paid") else it
            }
            dao.insertLoanSchedules(updatedSchedules)
            return
        }

        if (paymentType == "Prepayment") {
            // Prepayment directly reduces the outstanding principal balance
            val newOutstanding = (loan.outstandingBalance - amount).coerceAtLeast(0.0)
            val updatedLoan = loan.copy(
                outstandingBalance = newOutstanding,
                status = if (newOutstanding <= 0.1) "Closed" else loan.status,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertLoan(updatedLoan)

            // Insert dynamic payment record
            dao.insertLoanPayment(
                LoanPaymentEntity(
                    loanId = loanId,
                    installmentNumber = 0,
                    paymentDate = paymentDate,
                    amountPaid = amount,
                    paymentMethod = paymentMethod,
                    notes = notes,
                    paymentType = "Prepayment",
                    userId = loan.userId
                )
            )

            // Update Schedule: mark remaining schedules from the back as Prepaid
            var prepaidPool = amount
            val updatedSchedules = currentSchedules.reversed().map { schedule ->
                if (schedule.paymentStatus == "Pending" && prepaidPool > 0.0) {
                    val remainingEmiVal = schedule.emiAmount
                    if (prepaidPool >= remainingEmiVal) {
                        prepaidPool -= remainingEmiVal
                        schedule.copy(paymentStatus = "Prepaid")
                    } else {
                        prepaidPool = 0.0
                        schedule.copy(paymentStatus = "Pending")
                    }
                } else {
                    schedule
                }
            }.reversed()
            dao.insertLoanSchedules(updatedSchedules)
            return
        }

        // Regular Payment
        if (paymentType == "Regular") {
            // Find earliest pending installment
            val earliestPending = currentSchedules.firstOrNull { it.paymentStatus == "Pending" }
            val instNum = earliestPending?.installmentNumber ?: 1

            // Decrease outstanding balance
            val principalPart = earliestPending?.principalComponent ?: amount
            val newOutstanding = (loan.outstandingBalance - principalPart).coerceAtLeast(0.0)

            val updatedLoan = loan.copy(
                outstandingBalance = newOutstanding,
                status = if (newOutstanding <= 0.1) "Closed" else loan.status,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertLoan(updatedLoan)

            // Record payment
            dao.insertLoanPayment(
                LoanPaymentEntity(
                    loanId = loanId,
                    installmentNumber = instNum,
                    paymentDate = paymentDate,
                    amountPaid = amount,
                    paymentMethod = paymentMethod,
                    notes = notes,
                    paymentType = "Regular",
                    userId = loan.userId
                )
            )

            // Mark earliest pending as Paid
            if (earliestPending != null) {
                val updated = earliestPending.copy(paymentStatus = "Paid")
                dao.insertLoanSchedules(listOf(updated))
            }
        }
    }

    // Forecast remaining months
    fun calculateRemainingMonths(outstanding: Double, emi: Double, annualRate: Double): Int {
        if (outstanding <= 0.0) return 0
        if (emi <= 0.0) return 0
        if (annualRate <= 0.0) return (outstanding / emi).toInt().coerceAtLeast(1)
        
        val r = annualRate / 100.0 / 12.0
        val diff = emi - outstanding * r
        if (diff <= 0.0) return 999 // Infinite tenure because EMI doesn't even cover monthly interest!
        
        return try {
            val ratio = emi / diff
            val num = ln(ratio)
            val den = ln(1.0 + r)
            val months = (num / den).toInt()
            months.coerceAtLeast(1)
        } catch (e: Exception) {
            999
        }
    }

    // Alerts Preparation: repo methods for due dates
    fun getAlertsForLoans(sList: List<LoanScheduleEntity>): Map<String, List<LoanScheduleEntity>> {
        val now = System.currentTimeMillis()
        val dueIn7 = mutableListOf<LoanScheduleEntity>()
        val dueIn3 = mutableListOf<LoanScheduleEntity>()
        val dueIn1 = mutableListOf<LoanScheduleEntity>()
        val missed = mutableListOf<LoanScheduleEntity>()

        for (s in sList) {
            if (s.paymentStatus != "Paid" && s.paymentStatus != "Completed" && s.paymentStatus != "Prepaid") {
                val diffMills = s.dueDate - now
                val diffDays = (diffMills / (1000 * 60 * 60 * 24)).toInt()

                if (diffMills < 0) {
                    missed.add(s)
                } else if (diffDays <= 1) {
                    dueIn1.add(s)
                } else if (diffDays <= 3) {
                    dueIn3.add(s)
                } else if (diffDays <= 7) {
                    dueIn7.add(s)
                }
            }
        }

        return mapOf(
            "dueInTomorrow" to dueIn1,
            "dueIn3Days" to dueIn3,
            "dueIn7Days" to dueIn7,
            "missed" to missed
        )
    }
}
