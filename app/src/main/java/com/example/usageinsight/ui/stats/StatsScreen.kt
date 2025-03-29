package com.example.usageinsight.ui.stats

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.ui.ViewModelFactory
import com.example.usageinsight.ui.components.ErrorMessage
import com.example.usageinsight.ui.components.LoadingIndicator

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(
        factory = ViewModelFactory.create(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        when (val state = uiState) {
            is StatsUiState.Loading -> LoadingIndicator()
            is StatsUiState.Success -> WeeklyStats(
                weeklyData = state.weeklyData,
                totalScreenTime = state.totalScreenTime,
                modifier = Modifier.padding(padding)
            )
            is StatsUiState.Error -> ErrorMessage(
                message = state.message,
                onRetry = viewModel::loadWeeklyStats
            )
        }
    }
}

@Composable
private fun WeeklyStats(
    weeklyData: List<AppUsageEntity>,
    totalScreenTime: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "本周使用统计",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("总使用时长：${totalScreenTime / 1000 / 60} 分钟")
        
        // TODO: 添加图表展示
    }
}
