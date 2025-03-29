package com.example.usageinsight

import android.app.Application
import androidx.room.Room
import com.example.usageinsight.data.AppDatabase
import com.example.usageinsight.data.UserPreferences
import com.example.usageinsight.data.dao.UnlockDao
import com.example.usageinsight.repository.AnalysisRepository
import com.example.usageinsight.repository.DeepSeekAnalysisRepository
import com.example.usageinsight.repository.UsageRepository
import com.example.usageinsight.repository.UsageRepositoryImpl
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class UsageInsightApp : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var usageRepository: UsageRepository
        private set

    lateinit var analysisRepository: AnalysisRepository
        private set

    @Inject
    lateinit var unlockDao: UnlockDao

    override fun onCreate() {
        super.onCreate()
        
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "usage_insight_db"
        )
        .fallbackToDestructiveMigration()  // 允许破坏性迁移
        .build()

        usageRepository = UsageRepositoryImpl(database, unlockDao)
        analysisRepository = DeepSeekAnalysisRepository(UserPreferences(this))
    }
}
