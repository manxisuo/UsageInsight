package com.example.usageinsight.repository

import com.example.usageinsight.data.AppDatabase
import com.example.usageinsight.data.dao.UnlockDao
import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.data.entity.NotificationEntity
import com.example.usageinsight.data.entity.UnlockEntity
import com.example.usageinsight.model.AppUsageSummary
import com.example.usageinsight.model.DailyUsageSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.*
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class UsageRepositoryImpl(
    private val database: AppDatabase,
    private val unlockDao: UnlockDao
) : UsageRepository {
    
    private val commonAppNames = mapOf(
        "com.tencent.mm" to "微信",
        "com.zhihu.android" to "知乎",
        // ... 其他映射保持不变 ...
    )

    private fun getAppName(packageName: String, originalName: String): String {
        return commonAppNames[packageName] ?: originalName
    }

    private fun LocalDate.toEpochMilli(): Long {
        return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    override fun getTopAppsForDate(date: LocalDate): Flow<List<AppUsageEntity>> {
        return database.appUsageDao().getTopAppsForDate(date.toEpochMilli())
    }

    override fun getAppUsageStats(startDate: LocalDate): Flow<List<AppUsageEntity>> {
        return database.appUsageDao().getAppUsageStats(startDate.toEpochMilli())
    }

    override fun getUnlockCount(startTime: Long, endTime: Long): Flow<Int> {
        return unlockDao.getUnlockCountBetween(startTime, endTime)
    }

    override fun getUnlocks(startTime: Long, endTime: Long): Flow<List<UnlockEntity>> {
        return unlockDao.getUnlocksBetween(startTime, endTime)
    }

    override fun getNotificationCountForDate(date: LocalDate): Flow<Int> {
        return database.notificationDao().getNotificationCountForDate(date.toEpochMilli())
    }

    override fun getNotificationStats(startDate: LocalDate): Flow<List<NotificationEntity>> {
        return database.notificationDao().getNotificationStats(startDate.toEpochMilli())
    }

    override fun getDailyUsageSummary(date: LocalDate): Flow<DailyUsageSummary> = flow {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.d("UsageRepository", """
            查询时间范围：
            开始：${Instant.ofEpochMilli(startOfDay)}
            结束：${Instant.ofEpochMilli(endOfDay)}
            当前时间：${Instant.now()}
        """.trimIndent())

        val appUsages = database.appUsageDao().getAppUsagesForDay(startOfDay)

        Log.d("UsageRepository", """
            查询结果：
            记录数量：${appUsages.size}
            总使用时长：${appUsages.sumOf { it.usageTimeInMs } / 1000 / 60}分钟
            应用列表：${appUsages.joinToString("\n") { 
                "${it.appName}: ${it.usageTimeInMs / 1000 / 60}分钟" 
            }}
        """.trimIndent())

        val totalScreenTime = appUsages.sumOf { it.usageTimeInMs }
        val unlockCount = unlockDao.getUnlockCountBetween(startOfDay, endOfDay).first()
        val notificationCount = database.notificationDao().getNotificationCountForDate(startOfDay).first()

        emit(DailyUsageSummary(
            totalScreenTime = totalScreenTime,
            unlockCount = unlockCount,
            notificationCount = notificationCount,
            topApps = appUsages.sortedByDescending { it.usageTimeInMs }.take(10),
            nightUsagePercentage = 0f
        ))
    }.flowOn(Dispatchers.IO)

    override suspend fun cleanupOldData(timestamp: Long) {
        database.appUsageDao().deleteOldData(timestamp)
        database.notificationDao().deleteOldData(timestamp)
        // 如果需要清理解锁数据，可以在这里添加相应的方法
    }
}
