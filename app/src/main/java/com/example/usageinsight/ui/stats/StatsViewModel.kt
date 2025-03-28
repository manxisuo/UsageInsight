package com.example.usageinsight.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.repository.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class StatsViewModel(
    private val repository: UsageRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState

    fun loadWeeklyStats() {
        viewModelScope.launch {
            val startDate = LocalDate.now().minus(7, ChronoUnit.DAYS)
            repository.getAppUsageStats(startDate)
                .collect { usageData ->
                    _uiState.value = StatsUiState.Success(
                        weeklyData = usageData,
                        totalScreenTime = calculateTotalScreenTime(usageData)
                    )
                }
        }
    }

    private fun calculateTotalScreenTime(data: List<AppUsageEntity>): Long {
        return data.sumOf { it.usageTimeInMs }
    }
}

sealed class StatsUiState {
    data object Loading : StatsUiState()
    data class Success(
        val weeklyData: List<AppUsageEntity>,
        val totalScreenTime: Long
    ) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}
