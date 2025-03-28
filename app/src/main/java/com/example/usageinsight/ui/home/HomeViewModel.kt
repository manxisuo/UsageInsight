package com.example.usageinsight.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usageinsight.model.DailyUsageSummary
import com.example.usageinsight.repository.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(
    private val repository: UsageRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadTodayStats() {
        viewModelScope.launch {
            repository.getDailyUsageSummary(LocalDate.now())
                .catch { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "Unknown error")
                }
                .collect { summary ->
                    _uiState.value = HomeUiState.Success(summary)
                }
        }
    }

    fun refreshData() {
        _uiState.value = HomeUiState.Loading
        loadTodayStats()
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val summary: DailyUsageSummary) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
