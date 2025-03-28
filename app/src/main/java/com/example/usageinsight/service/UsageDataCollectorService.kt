package com.example.usageinsight.service

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import com.example.usageinsight.UsageInsightApp
import com.example.usageinsight.data.dao.AppUsageDao
import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.util.getTodayStartMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class UsageDataCollectorService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var appUsageDao: AppUsageDao
    private lateinit var usageStatsManager: UsageStatsManager

    override fun onCreate() {
        super.onCreate()
        appUsageDao = (application as UsageInsightApp).database.appUsageDao()
        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        
        startDataCollection()
    }

    private fun startDataCollection() {
        serviceScope.launch {
            while (true) {
                collectUsageData()
                delay(TimeUnit.MINUTES.toMillis(15)) // 每15分钟收集一次
            }
        }
    }

    private suspend fun collectUsageData() {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        stats.forEach { stat ->
            val packageName = stat.packageName
            // 跳过系统应用
            if (!packageName.startsWith("com.android.") && !packageName.startsWith("android")) {
                val usageEntity = AppUsageEntity(
                    packageName = packageName,
                    appName = getAppName(packageName),
                    usageTimeInMs = stat.totalTimeInForeground,
                    date = getTodayStartMillis(),
                    lastTimeUsed = stat.lastTimeUsed
                )
                appUsageDao.insertAppUsage(usageEntity)
            }
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = applicationContext.packageManager
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
