package com.example.usageinsight.ui.settings

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.usageinsight.ui.ViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = ViewModelFactory.create(LocalContext.current.applicationContext as Application)
    )
) {
    val context = LocalContext.current
    val apiKey by viewModel.apiKey.collectAsState(initial = "")
    var editingApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showClearedMessage by remember { mutableStateOf(false) }
    var showUpdatingMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // API Key 输入框
        OutlinedTextField(
            value = editingApiKey,
            onValueChange = { editingApiKey = it },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) 
                VisualTransformation.None 
            else 
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) 
                            Icons.Default.VisibilityOff 
                        else 
                            Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) 
                            "隐藏 API Key" 
                        else 
                            "显示 API Key"
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 保存按钮
        Button(
            onClick = {
                viewModel.updateApiKey(editingApiKey)
                showSavedMessage = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存")
        }

        // 保存成功提示
        if (showSavedMessage) {
            LaunchedEffect(Unit) {
                delay(2000)
                showSavedMessage = false
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "保存成功",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(32.dp))

        // 立即更新数据按钮
        Button(
            onClick = { 
                viewModel.collectDataNow()
                // 显示更新提示
                showUpdatingMessage = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("立即更新使用数据")
        }

        // 更新提示
        if (showUpdatingMessage) {
            LaunchedEffect(Unit) {
                delay(2000)
                showUpdatingMessage = false
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在更新数据...",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 清空数据按钮
        Button(
            onClick = { showClearDataDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.38f)
            )
        ) {
            Text("清空使用数据")
        }

        // 清空成功提示
        if (showClearedMessage) {
            LaunchedEffect(Unit) {
                delay(2000)
                showClearedMessage = false
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "数据已清空",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // 确认对话框
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("确认清空数据") },
            text = { Text("确定要清空所有使用数据吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                        showClearedMessage = true
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
