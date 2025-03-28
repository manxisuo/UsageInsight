package com.example.usageinsight.repository

import com.example.usageinsight.data.AppDatabase
import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.data.entity.NotificationEntity
import com.example.usageinsight.data.entity.UnlockEntity
import com.example.usageinsight.model.AppUsageSummary
import com.example.usageinsight.model.DailyUsageSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import java.time.LocalTime
import java.util.Calendar

class UsageRepositoryImpl(
    private val database: AppDatabase
) : UsageRepository {
    
    private fun LocalDate.toEpochMilli(): Long {
        return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    override fun getTopAppsForDate(date: LocalDate): Flow<List<AppUsageEntity>> {
        return database.appUsageDao().getTopAppsForDate(date.toEpochMilli())
    }

    override fun getAppUsageStats(startDate: LocalDate): Flow<List<AppUsageEntity>> {
        return database.appUsageDao().getAppUsageStats(startDate.toEpochMilli())
    }

    override fun getUnlockCountForDate(date: LocalDate): Flow<Int> {
        return database.unlockDao().getUnlockCountForDate(date.toEpochMilli())
    }

    override fun getUnlockStats(startDate: LocalDate): Flow<List<UnlockEntity>> {
        return database.unlockDao().getUnlockStats(startDate.toEpochMilli())
    }

    override fun getNotificationCountForDate(date: LocalDate): Flow<Int> {
        return database.notificationDao().getNotificationCountForDate(date.toEpochMilli())
    }

    override fun getNotificationStats(startDate: LocalDate): Flow<List<NotificationEntity>> {
        return database.notificationDao().getNotificationStats(startDate.toEpochMilli())
    }

    override fun getDailyUsageSummary(date: LocalDate): Flow<DailyUsageSummary> {
        return combine(
            getTopAppsForDate(date),
            getUnlockCountForDate(date),
            getNotificationCountForDate(date)
        ) { apps, unlocks, notifications ->
            DailyUsageSummary(
                totalScreenTime = apps.sumOf { it.usageTimeInMs },
                unlockCount = unlocks,
                notificationCount = notifications,
                topApps = apps.map { 
                    AppUsageSummary(it.packageName, it.appName, it.usageTimeInMs) 
                },
                nightUsagePercentage = calculateNightUsagePercentage(apps)
            )
        }
    }

    override suspend fun cleanOldData(beforeDate: LocalDate) {
        val timestamp = beforeDate.toEpochMilli()
        database.appUsageDao().deleteOldData(timestamp)
        database.unlockDao().deleteOldData(timestamp)
        database.notificationDao().deleteOldData(timestamp)
    }

    private fun calculateNightUsagePercentage(apps: List<AppUsageEntity>): Float {
        val totalUsage = apps.sumOf { it.usageTimeInMs }
        if (totalUsage == 0L) return 0f

        val nightUsage = apps.sumOf { app ->
            val instant = Instant.ofEpochMilli(app.lastTimeUsed)
            val time = LocalTime.ofInstant(instant, ZoneId.systemDefault())
            
            if (time.isAfter(LocalTime.of(21, 0)) || time.isBefore(LocalTime.of(6, 0))) {
                app.usageTimeInMs
            } else 0L
        }

        return (nightUsage.toFloat() / totalUsage) * 100
    }
}
