package com.example.usageinsight.data.dao

import androidx.room.*
import com.example.usageinsight.data.entity.UnlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockDao {
    @Insert
    suspend fun insertUnlock(unlock: UnlockEntity)

    @Query("SELECT COUNT(*) FROM unlocks WHERE date = :date")
    fun getUnlockCountForDate(date: Long): Flow<Int>

    @Query("SELECT * FROM unlocks WHERE date >= :startDate")
    fun getUnlockStats(startDate: Long): Flow<List<UnlockEntity>>
    
    @Query("DELETE FROM unlocks WHERE date < :date")
    suspend fun deleteOldData(date: Long)
}
