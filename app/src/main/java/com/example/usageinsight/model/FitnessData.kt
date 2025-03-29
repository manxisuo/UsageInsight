package com.example.usageinsight.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_data")
data class FitnessData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val steps: Int,
    val distance: Float,
    val heartRate: Float? = null,
    val calories: Float? = null,
    val timestamp: Long
) 