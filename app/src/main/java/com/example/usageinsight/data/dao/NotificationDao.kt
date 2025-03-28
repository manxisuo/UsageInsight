package com.example.usageinsight.data.dao

import androidx.room.*
import com.example.usageinsight.data.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT COUNT(*) FROM notifications WHERE date = :date")
    fun getNotificationCountForDate(date: Long): Flow<Int>

    @Query("SELECT * FROM notifications WHERE date >= :startDate")
    fun getNotificationStats(startDate: Long): Flow<List<NotificationEntity>>
    
    @Query("DELETE FROM notifications WHERE date < :date")
    suspend fun deleteOldData(date: Long)
}
