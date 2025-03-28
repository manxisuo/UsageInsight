package com.example.usageinsight

import android.app.Application
import androidx.room.Room
import com.example.usageinsight.data.AppDatabase

class UsageInsightApp : Application() {
    lateinit var database: AppDatabase
        private set
        
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "usage_insight_db"
        ).build()
    }
}
