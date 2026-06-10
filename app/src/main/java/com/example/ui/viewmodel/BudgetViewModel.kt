package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface BudgetUiState {
    object Loading : BudgetUiState
    data class Success(
        val budgets: List<BudgetProgress>,
        val summary: MonthlyBudgetSummary,
        val highestSpendingCategory: String?,
        val mostExceededBudget: Pair<String, Double>?,
        val remainingBudgetTotals: Double
    ) : BudgetUiState
    data class Error(val message: String) : BudgetUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = BudgetRepository(AppDatabase.getDatabase(application).dao())
    
    private val _userId = MutableStateFlow("guest")
    val userId: StateFlow<String> = _userId.asStateFlow()
    
    private val _selectedMonthRange = MutableStateFlow(getCurrentMonthRange())
    val selectedMonthRange: StateFlow<Pair<Long, Long>> = _selectedMonthRange.asStateFlow()
    
    // Reactive UI state emitting complete statistical aggregations
    val uiState: StateFlow<BudgetUiState> = combine(
        _userId,
        _selectedMonthRange
    ) { uid, range ->
        Pair(uid, range)
    }.flatMapLatest { (uid, range) ->
        val (start, end) = range
        combine(
            repository.getBudgetProgressList(uid, start, end),
            repository.getMonthlyBudgetSummary(uid, start, end),
            repository.getHighestSpendingCategory(uid, start, end),
            repository.getMostExceededBudget(uid, start, end),
            repository.getRemainingBudgetTotals(uid, start, end)
        ) { progressList, summary, highestCat, exceeded, remainingTotal ->
            BudgetUiState.Success(
                budgets = progressList,
                summary = summary,
                highestSpendingCategory = highestCat,
                mostExceededBudget = exceeded,
                remainingBudgetTotals = remainingTotal
            ) as BudgetUiState
        }
    }.catch { e ->
        emit(BudgetUiState.Error(e.localizedMessage ?: "Failed to load budget systems."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetUiState.Loading
    )
    
    fun setMonthRange(startDate: Long, endDate: Long) {
        _selectedMonthRange.value = Pair(startDate, endDate)
    }
    
    fun setUserId(uid: String) {
        _userId.value = uid
    }
    
    // Budget Creation validation
    fun createBudget(
        category: String,
        amount: Double,
        startDate: Long?,
        endDate: Long?,
        onResult: (Boolean, String) -> Unit
    ) {
        if (amount <= 0) {
            onResult(false, "Budget amount must be greater than 0")
            return
        }
        
        val (defaultStart, defaultEnd) = getCurrentMonthRange()
        val finalStart = startDate ?: defaultStart
        val finalEnd = endDate ?: defaultEnd
        
        viewModelScope.launch {
            try {
                val currentBudgets = repository.getAllBudgets(_userId.value).first()
                val hasDuplicate = currentBudgets.any {
                    it.category.equals(category, ignoreCase = true) &&
                    it.isActive &&
                    !it.isDeleted &&
                    it.startDate == finalStart &&
                    it.endDate == finalEnd
                }
                
                if (hasDuplicate) {
                    onResult(false, "An active budget already exists for '$category' in this period.")
                    return@launch
                }
                
                val newBudget = BudgetEntity(
                    category = category,
                    budgetAmount = amount,
                    startDate = finalStart,
                    endDate = finalEnd,
                    periodType = "Monthly",
                    userId = _userId.value
                )
                repository.insertBudget(newBudget)
                onResult(true, "Budget created successfully!")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to create budget.")
            }
        }
    }
    
    fun updateBudgetAmount(id: Int, newAmount: Double, onResult: (Boolean, String) -> Unit) {
        if (newAmount <= 0) {
            onResult(false, "Amount must be greater than 0")
            return
        }
        viewModelScope.launch {
            try {
                val budget = repository.getBudgetById(id)
                if (budget != null) {
                    val updated = budget.copy(
                        budgetAmount = newAmount,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.insertBudget(updated)
                    onResult(true, "Budget updated successfully")
                } else {
                    onResult(false, "Budget not found")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to update budget")
            }
        }
    }
    
    fun toggleBudgetActive(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleBudgetActiveState(id, isActive)
        }
    }
    
    fun deleteBudget(id: Int) {
        viewModelScope.launch {
            repository.softDeleteBudget(id)
        }
    }
    
    companion object {
        fun getCurrentMonthRange(): Pair<Long, Long> {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val end = calendar.timeInMillis
            return Pair(start, end)
        }
    }
}
