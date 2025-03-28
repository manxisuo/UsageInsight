package com.example.usageinsight.repository

import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.data.entity.NotificationEntity
import com.example.usageinsight.data.entity.UnlockEntity
import com.example.usageinsight.model.DailyUsageSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface UsageRepository {
    // 应用使用数据
    fun getTopAppsForDate(date: LocalDate): Flow<List<AppUsageEntity>>
    fun getAppUsageStats(startDate: LocalDate): Flow<List<AppUsageEntity>>
    
    // 解锁数据
    fun getUnlockCountForDate(date: LocalDate): Flow<Int>
    fun getUnlockStats(startDate: LocalDate): Flow<List<UnlockEntity>>
    
    // 通知数据
    fun getNotificationCountForDate(date: LocalDate): Flow<Int>
    fun getNotificationStats(startDate: LocalDate): Flow<List<NotificationEntity>>
    
    // 数据清理
    suspend fun cleanOldData(beforeDate: LocalDate)
    
    // 数据聚合
    fun getDailyUsageSummary(date: LocalDate): Flow<DailyUsageSummary>
}
