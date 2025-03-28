package com.example.usageinsight.data.dao

import androidx.room.*
import com.example.usageinsight.data.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsage(usage: AppUsageEntity)

    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY usageTimeInMs DESC LIMIT 10")
    fun getTopAppsForDate(date: Long): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE date >= :startDate")
    fun getAppUsageStats(startDate: Long): Flow<List<AppUsageEntity>>
    
    @Query("DELETE FROM app_usage WHERE date < :date")
    suspend fun deleteOldData(date: Long)
}
