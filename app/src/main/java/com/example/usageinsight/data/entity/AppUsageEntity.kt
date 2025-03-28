package com.example.usageinsight.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val usageTimeInMs: Long,
    val date: Long,  // 使用时间戳存储日期
    val lastTimeUsed: Long
)
