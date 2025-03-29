package com.example.usageinsight.ui.fitness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usageinsight.model.FitnessData
import com.example.usageinsight.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class FitnessViewModel(
    private val fitnessRepository: FitnessRepository
) : ViewModel() {
    
    private val _fitnessData = MutableStateFlow<FitnessData?>(null)
    val fitnessData: StateFlow<FitnessData?> = _fitnessData
    
    init {
        loadLatestFitnessData()
    }
    
    private fun loadLatestFitnessData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            try {
                val data = fitnessRepository.getFitnessDataBetween(startOfDay, endOfDay).first()
                _fitnessData.value = data.firstOrNull()
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
    
    fun syncFitnessData() {
        viewModelScope.launch {
            try {
                fitnessRepository.syncFitnessData()
                loadLatestFitnessData() // 同步后重新加载数据
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
} 