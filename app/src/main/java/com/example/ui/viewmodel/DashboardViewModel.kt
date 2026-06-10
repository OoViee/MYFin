package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DashboardRepository
import com.example.data.Repository
import kotlinx.coroutines.flow.*

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: Repository
    private val dashboardRepository: DashboardRepository

    val uiState: StateFlow<DashboardUiState>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = Repository(database.dao())
        dashboardRepository = DashboardRepository(repository)

        uiState = dashboardRepository.dashboardDataFlow
            .map { data ->
                DashboardUiState.Success(data) as DashboardUiState
            }
            .catch { e ->
                emit(DashboardUiState.Error(e.message ?: "An unknown error occurred"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DashboardUiState.Loading
            )
    }
}
