package com.example.usageinsight.data.dao

import androidx.room.*
import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.model.AppUsageSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Query("""
        SELECT packageName, appName, SUM(usageTimeInMs) as usageTimeInMs 
        FROM app_usage 
        WHERE date = :startOfDay  -- 精确匹配日期，而不是范围查询
        GROUP BY packageName, appName
    """)
    suspend fun getAppUsagesForDay(startOfDay: Long): List<AppUsageSummary>

    @Query("""
        SELECT * FROM app_usage 
        WHERE date = :date 
        GROUP BY packageName 
        ORDER BY usageTimeInMs DESC
    """)
    suspend fun getAppUsagesByDate(date: Long): List<AppUsageEntity>

    @Query("""
        SELECT * FROM app_usage 
        WHERE date = :date 
        GROUP BY packageName 
        ORDER BY usageTimeInMs DESC 
        LIMIT 10
    """)
    fun getTopAppsForDate(date: Long): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE date >= :startDate ORDER BY date ASC")
    fun getAppUsageStats(startDate: Long): Flow<List<AppUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsage(usage: AppUsageEntity)

    @Query("DELETE FROM app_usage WHERE date < :timestamp")
    suspend fun deleteOldData(timestamp: Long)

    @Query("DELETE FROM app_usage")
    suspend fun deleteAllData()

    @Query("SELECT COUNT(*) FROM app_usage")
    fun getTotalRecords(): Flow<Int>

    @Query("DELETE FROM app_usage WHERE date = :date")
    suspend fun deleteDataForDate(date: Long)
}
