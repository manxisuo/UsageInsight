package com.example.usageinsight.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.usageinsight.model.DailyUsageSummary
import com.example.usageinsight.ui.components.ErrorMessage
import com.example.usageinsight.ui.components.LoadingIndicator
import com.example.usageinsight.ui.ViewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.example.usageinsight.ui.components.UsagePieChart
import com.example.usageinsight.ui.components.MarkdownText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import android.app.Application

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory.create(LocalContext.current.applicationContext as Application)
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val analysis by viewModel.analysis.collectAsState()

    Scaffold { padding ->
        when (val state = uiState) {
            is HomeUiState.Loading -> LoadingIndicator()
            is HomeUiState.Success -> UsageSummary(
                summary = state.summary,
                analysis = analysis,
                onAnalyzeClick = { viewModel.generateAnalysis(state.summary) },
                modifier = Modifier.padding(padding)
            )
            is HomeUiState.Error -> ErrorMessage(
                message = state.message,
                onRetry = viewModel::refreshData
            )
        }
    }
}

@Composable
private fun UsageSummary(
    summary: DailyUsageSummary,
    analysis: AnalysisState,
    onAnalyzeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "今日使用统计",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // 基础统计信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 总使用时长
                Text(
                    text = "总屏幕时间: ${summary.formattedTotalScreenTime}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 解锁次数
                Text(
                    text = "解锁次数: ${summary.unlockCount} 次",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 通知数量
                Text(
                    text = "通知数量: ${summary.notificationCount} 条",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 夜间使用占比
                Text(
                    text = "夜间使用占比: ${String.format("%.1f", summary.nightUsagePercentage)}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Top应用列表
        Text(
            text = "使用最多的应用",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 计算最长使用时间，用于进度条比例
        val maxUsageTime = summary.topApps.maxByOrNull { it.usageTimeInMs }?.usageTimeInMs ?: 1L

        summary.topApps.forEach { app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 应用名称和进度条背景
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                ) {
                    // 进度条背景
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(app.usageTimeInMs.toFloat() / maxUsageTime)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    // 应用名称
                    Text(
                        text = app.appName,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // 使用时间
                Text(
                    text = "${app.usageTimeInMs / 1000 / 60} 分钟",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .width(64.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End
                )
            }
        }

        // 分析按钮
        Button(
            onClick = onAnalyzeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("分析使用情况")
        }

        // 分析结果
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "使用分析",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                when (analysis) {
                    is AnalysisState.Initial -> {
                        Text(
                            text = "点击上方按钮生成分析",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is AnalysisState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is AnalysisState.Success -> {
                        MarkdownText(
                            markdown = analysis.content,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is AnalysisState.Error -> {
                        Text(
                            text = analysis.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
