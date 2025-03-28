package com.example.usageinsight.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.usageinsight.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _apiKey = MutableStateFlow<String?>(null)
    val apiKey: StateFlow<String?> = _apiKey

    init {
        loadApiKey()
    }

    private fun loadApiKey() {
        viewModelScope.launch {
            userPreferences.apiKey.collect { key ->
                _apiKey.value = key
            }
        }
    }

    fun updateApiKey(newKey: String) {
        viewModelScope.launch {
            userPreferences.updateApiKey(newKey)
        }
    }
}
