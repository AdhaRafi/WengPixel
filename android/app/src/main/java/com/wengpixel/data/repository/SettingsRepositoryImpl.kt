package com.wengpixel.data.repository

import com.wengpixel.data.local.datastore.SettingsDataStore
import com.wengpixel.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override val backendUrl: Flow<String> = settingsDataStore.backendUrl
    override val exportFormat: Flow<String> = settingsDataStore.exportFormat
    override val exportQuality: Flow<Int> = settingsDataStore.exportQuality
    override val autoSaveHistory: Flow<Boolean> = settingsDataStore.autoSaveHistory

    override suspend fun updateBackendUrl(url: String) {
        settingsDataStore.setBackendUrl(url)
    }

    override suspend fun updateExportFormat(format: String) {
        settingsDataStore.setExportFormat(format)
    }

    override suspend fun updateExportQuality(quality: Int) {
        settingsDataStore.setExportQuality(quality)
    }

    override suspend fun updateAutoSaveHistory(enabled: Boolean) {
        settingsDataStore.setAutoSaveHistory(enabled)
    }
}
