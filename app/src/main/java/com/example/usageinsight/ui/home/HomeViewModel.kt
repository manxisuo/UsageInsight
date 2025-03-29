package com.example.usageinsight.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usageinsight.model.DailyUsageSummary
import com.example.usageinsight.repository.AnalysisRepository
import com.example.usageinsight.repository.UsageRepository
import com.example.usageinsight.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first

class HomeViewModel(
    private val repository: UsageRepository,
    private val analysisRepository: AnalysisRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _analysis = MutableStateFlow<AnalysisState>(AnalysisState.Initial)
    val analysis: StateFlow<AnalysisState> = _analysis

    init {
        loadTodayStats()
    }

    fun loadTodayStats() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                repository.getDailyUsageSummary(LocalDate.now())
                    .catch { error ->
                        _uiState.value = HomeUiState.Error(error.message ?: "Unknown error")
                    }
                    .collect { summary ->
                        _uiState.value = HomeUiState.Success(summary)
                        Log.d("HomeViewModel", "总屏幕时间: ${summary.formattedTotalScreenTime}")
                    }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refreshData() {
        loadTodayStats()
    }

    fun generateAnalysis(summary: DailyUsageSummary) {
        viewModelScope.launch {
            _analysis.value = AnalysisState.Loading
            try {
                val result = analysisRepository.generateDailyAnalysis(summary)
                _analysis.value = AnalysisState.Success(result)
            } catch (e: Exception) {
                _analysis.value = AnalysisState.Error(e.message ?: "未知错误")
            }
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val summary: DailyUsageSummary) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class AnalysisState {
    data object Initial : AnalysisState()
    data object Loading : AnalysisState()
    data class Success(val content: String) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}
