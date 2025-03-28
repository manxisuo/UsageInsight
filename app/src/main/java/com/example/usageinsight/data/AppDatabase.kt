package com.example.usageinsight.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.usageinsight.data.dao.AppUsageDao
import com.example.usageinsight.data.dao.NotificationDao
import com.example.usageinsight.data.dao.UnlockDao
import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.data.entity.NotificationEntity
import com.example.usageinsight.data.entity.UnlockEntity

@Database(
    entities = [
        AppUsageEntity::class,
        UnlockEntity::class,
        NotificationEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun unlockDao(): UnlockDao
    abstract fun notificationDao(): NotificationDao
}
