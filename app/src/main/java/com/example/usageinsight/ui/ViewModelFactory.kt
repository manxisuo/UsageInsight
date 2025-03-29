package com.example.usageinsight.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.usageinsight.UsageInsightApp
import com.example.usageinsight.data.AppDatabase
import com.example.usageinsight.data.UserPreferences
import com.example.usageinsight.data.dao.UnlockDao
import com.example.usageinsight.data.dao.FitnessDao
import com.example.usageinsight.repository.DeepSeekAnalysisRepository
import com.example.usageinsight.repository.UsageRepositoryImpl
import com.example.usageinsight.ui.home.HomeViewModel
import com.example.usageinsight.ui.settings.SettingsViewModel
import com.example.usageinsight.ui.stats.StatsViewModel
import com.example.usageinsight.ui.fitness.FitnessViewModel
import com.example.usageinsight.repository.FitnessRepository
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
    private val application: Application,
    private val database: AppDatabase,
    private val unlockDao: UnlockDao
) : ViewModelProvider.Factory {

    private val userPreferences = UserPreferences(application)
    private val analysisRepository = DeepSeekAnalysisRepository(userPreferences)
    private val usageRepository = UsageRepositoryImpl(database, unlockDao)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    repository = usageRepository,
                    analysisRepository = analysisRepository,
                    userPreferences = userPreferences
                ) as T
            }
            modelClass.isAssignableFrom(StatsViewModel::class.java) -> {
                StatsViewModel(usageRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(userPreferences, database, application) as T
            }
            modelClass.isAssignableFrom(FitnessViewModel::class.java) -> {
                FitnessViewModel(FitnessRepository(application, database.fitnessDao())) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        fun create(application: Application): ViewModelFactory {
            val app = application as UsageInsightApp
            return ViewModelFactory(
                application = application,
                database = app.database,
                unlockDao = app.unlockDao
            )
        }
    }
}
