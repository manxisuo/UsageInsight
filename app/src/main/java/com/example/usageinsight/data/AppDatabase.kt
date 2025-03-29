package com.example.usageinsight.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.usageinsight.data.dao.AppUsageDao
import com.example.usageinsight.data.dao.NotificationDao
import com.example.usageinsight.data.dao.UnlockDao
import com.example.usageinsight.data.dao.FitnessDao
import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.data.entity.NotificationEntity
import com.example.usageinsight.data.entity.UnlockEntity
import com.example.usageinsight.model.FitnessData

@Database(
    entities = [
        AppUsageEntity::class,
        UnlockEntity::class,
        NotificationEntity::class,
        FitnessData::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun unlockDao(): UnlockDao
    abstract fun notificationDao(): NotificationDao
    abstract fun fitnessDao(): FitnessDao

    companion object {
        const val DATABASE_NAME = "app_database"
        private const val TAG = "AppDatabase"
        
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d(TAG, "数据库创建成功")
                }
                
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d(TAG, "数据库打开成功")
                }
            })
            .build()
        }
    }
}
