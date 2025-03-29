package com.example.usageinsight.ui.fitness

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.usageinsight.ui.ViewModelFactory

@Composable
fun FitnessScreen(
    modifier: Modifier = Modifier,
    viewModel: FitnessViewModel = viewModel(
        factory = ViewModelFactory.create(LocalContext.current.applicationContext as Application)
    )
) {
    val fitnessData by viewModel.fitnessData.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "健康数据",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 显示步数、距离、心率等数据
        fitnessData?.let { data ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("步数: ${data.steps}")
                    Text("距离: ${data.distance}米")
                    data.heartRate?.let { 
                        Text("心率: $it BPM") 
                    }
                    data.calories?.let { 
                        Text("消耗卡路里: $it kcal") 
                    }
                }
            }
        } ?: run {
            Text("暂无健康数据，请确保已授予相关权限并同步数据")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.syncFitnessData() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("同步健康数据")
        }
    }
} 