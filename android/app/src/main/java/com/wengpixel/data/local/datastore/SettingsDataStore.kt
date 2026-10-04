package com.wengpixel.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wengpixel_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_BACKEND_URL = stringPreferencesKey("backend_url")
        val KEY_EXPORT_FORMAT = stringPreferencesKey("export_format")
        val KEY_EXPORT_QUALITY = intPreferencesKey("export_quality")
        val KEY_AUTO_SAVE_HISTORY = booleanPreferencesKey("auto_save_history")

        const val DEFAULT_BACKEND_URL = "http://10.0.2.2:8000"
        const val DEFAULT_EXPORT_FORMAT = "PNG"
        const val DEFAULT_EXPORT_QUALITY = 95
    }

    val backendUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_BACKEND_URL] ?: DEFAULT_BACKEND_URL
    }

    val exportFormat: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_EXPORT_FORMAT] ?: DEFAULT_EXPORT_FORMAT
    }

    val exportQuality: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_EXPORT_QUALITY] ?: DEFAULT_EXPORT_QUALITY
    }

    val autoSaveHistory: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_SAVE_HISTORY] ?: true
    }

    suspend fun setBackendUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[KEY_BACKEND_URL] = url.trim().trimEnd('/')
        }
    }

    suspend fun setExportFormat(format: String) {
        dataStore.edit { preferences ->
            preferences[KEY_EXPORT_FORMAT] = format.uppercase()
        }
    }

    suspend fun setExportQuality(quality: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_EXPORT_QUALITY] = quality.coerceIn(10, 100)
        }
    }

    suspend fun setAutoSaveHistory(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_SAVE_HISTORY] = enabled
        }
    }
}
