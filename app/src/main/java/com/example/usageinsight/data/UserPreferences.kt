package com.example.usageinsight.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    private val API_KEY = stringPreferencesKey("api_key")
    private val PERMISSIONS_GRANTED = booleanPreferencesKey("permissions_granted")

    val apiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY] ?: ""
        }

    val hasPermissions: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PERMISSIONS_GRANTED] ?: false
        }

    suspend fun updateApiKey(newKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = newKey
        }
    }

    suspend fun setPermissionsGranted(granted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PERMISSIONS_GRANTED] = granted
        }
    }

    suspend fun getApiKey(): String? {
        return context.dataStore.data
            .map { preferences -> 
                preferences[API_KEY] 
            }
            .first()
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }
}
