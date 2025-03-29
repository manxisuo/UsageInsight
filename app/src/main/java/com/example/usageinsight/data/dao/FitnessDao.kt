package com.example.usageinsight.data.dao

import androidx.room.*
import com.example.usageinsight.model.FitnessData
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {
    @Query("SELECT * FROM fitness_data WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getFitnessDataBetween(startTime: Long, endTime: Long): Flow<List<FitnessData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fitnessData: FitnessData)

    @Query("DELETE FROM fitness_data WHERE timestamp < :timestamp")
    fun deleteOldData(timestamp: Long)

    @Query("SELECT * FROM fitness_data ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestFitnessData(): FitnessData?
} 