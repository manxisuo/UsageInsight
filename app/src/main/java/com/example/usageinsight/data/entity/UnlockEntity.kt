package com.example.usageinsight.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlocks")
data class UnlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long
)
