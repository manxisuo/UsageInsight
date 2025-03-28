package com.example.usageinsight.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    private val API_KEY = stringPreferencesKey("api_key")

    val apiKey: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY]
        }

    suspend fun updateApiKey(newKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = newKey
        }
    }
}
