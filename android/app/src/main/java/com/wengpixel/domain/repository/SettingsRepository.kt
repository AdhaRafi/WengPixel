package com.wengpixel.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val backendUrl: Flow<String>
    val exportFormat: Flow<String>
    val exportQuality: Flow<Int>
    val autoSaveHistory: Flow<Boolean>

    suspend fun updateBackendUrl(url: String)
    suspend fun updateExportFormat(format: String)
    suspend fun updateExportQuality(quality: Int)
    suspend fun updateAutoSaveHistory(enabled: Boolean)
}
