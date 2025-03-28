package com.example.usageinsight

import android.app.Application
import androidx.room.Room
import com.example.usageinsight.data.AppDatabase
import com.example.usageinsight.repository.UsageRepository
import com.example.usageinsight.repository.UsageRepositoryImpl

class UsageInsightApp : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var usageRepository: UsageRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "usage_insight_db"
        ).build()

        usageRepository = UsageRepositoryImpl(database)
    }
}
