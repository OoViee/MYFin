package com.example.ui.viewmodel

import com.example.data.DashboardData

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(val data: DashboardData) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
