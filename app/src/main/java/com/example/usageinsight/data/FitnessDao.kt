package com.example.usageinsight.data

import androidx.room.*
import com.example.usageinsight.model.FitnessData
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fitnessData: FitnessData)

    @Query("SELECT * FROM fitness_data WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getFitnessDataBetween(startTime: Long, endTime: Long): Flow<List<FitnessData>>

    @Query("SELECT * FROM fitness_data ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestFitnessData(): FitnessData?
} 