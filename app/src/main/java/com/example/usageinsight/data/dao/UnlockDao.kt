package com.example.usageinsight.data.dao

import androidx.room.*
import com.example.usageinsight.data.entity.UnlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockDao {
    @Insert
    suspend fun insert(unlock: UnlockEntity)

    @Query("SELECT COUNT(*) FROM unlocks WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getUnlockCountBetween(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT * FROM unlocks WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getUnlocksBetween(startTime: Long, endTime: Long): Flow<List<UnlockEntity>>
}
