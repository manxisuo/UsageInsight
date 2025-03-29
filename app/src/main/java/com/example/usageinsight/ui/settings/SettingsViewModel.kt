package com.example.usageinsight.ui.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.usageinsight.data.UserPreferences
import com.example.usageinsight.data.AppDatabase
import com.example.usageinsight.service.UsageDataCollectorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val database: AppDatabase,
    private val context: Context
) : ViewModel() {
    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey

    init {
        viewModelScope.launch {
            userPreferences.apiKey.collect { savedKey ->
                _apiKey.value = savedKey ?: ""
            }
        }
    }

    fun updateApiKey(newKey: String) {
        viewModelScope.launch {
            userPreferences.saveApiKey(newKey)
            _apiKey.value = newKey
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            database.appUsageDao().deleteAllData()
        }
    }

    fun collectDataNow() {
        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(Intent(UsageDataCollectorService.ACTION_COLLECT_NOW))
    }
}
