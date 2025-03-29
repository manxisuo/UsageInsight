package com.example.usageinsight.repository

import android.util.Log
import com.example.usageinsight.data.UserPreferences
import com.example.usageinsight.model.DailyUsageSummary
import com.example.usageinsight.network.DeepSeekClient
import com.example.usageinsight.network.DeepSeekRequest
import com.example.usageinsight.network.Message
import kotlinx.coroutines.flow.first

class DeepSeekAnalysisRepository(
    private val userPreferences: UserPreferences
) : AnalysisRepository {
    private val TAG = "DeepSeekAnalysis"

    override suspend fun generateDailyAnalysis(summary: DailyUsageSummary): String {
        val apiKey = userPreferences.getApiKey() ?: throw IllegalStateException("API Key not set")
        
        val prompt = """
            作为一个手机使用习惯分析师，请分析以下用户的手机使用数据并给出建议：
            
            总屏幕时间：${summary.totalScreenTime / 1000 / 60} 分钟
            解锁次数：${summary.unlockCount} 次
            通知数量：${summary.notificationCount} 条
            夜间使用占比：${summary.nightUsagePercentage}%
            
            使用最多的应用（按时长排序）：
            ${summary.topApps.joinToString("\n") { 
                "${it.appName}: ${it.usageTimeInMs / 1000 / 60} 分钟" 
            }}
            
            请从以下几个方面进行分析：
            1. 总体使用情况评估
            2. 可能存在的问题
            3. 改善建议
            4. 健康使用指南
            
            请用通俗易懂的语言，给出具体可行的建议。
        """.trimIndent()

        Log.d(TAG, """
            ====== 发送请求到 DeepSeek ======
            API Key: ${apiKey.take(8)}...
            提示词：
            $prompt
            ==============================
        """.trimIndent())
        
        return try {
            val request = DeepSeekRequest(
                messages = listOf(Message("user", prompt))
            )

            Log.d(TAG, "正在调用 DeepSeek API...")
            
            val response = DeepSeekClient.api.generateAnalysis(
                apiKey = "Bearer $apiKey",
                request = request
            )

            val result = response.choices.firstOrNull()?.message?.content
                ?: throw IllegalStateException("空响应")
                
            Log.d(TAG, """
                ====== DeepSeek 返回结果 ======
                $result
                ==============================
            """.trimIndent())
            
            result

        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> 
                    "请求超时，请稍后重试"
                e.message?.contains("Unable to resolve host") == true -> 
                    "网络连接失败，请检查网络设置"
                else -> "分析生成失败：${e.message}"
            }
            Log.e(TAG, """
                ====== API 调用失败 ======
                错误类型: ${e.javaClass.simpleName}
                错误信息: $errorMessage
                详细堆栈:
                ${e.stackTraceToString()}
                ========================
            """.trimIndent())
            
            throw IllegalStateException(errorMessage)
        }
    }
}
