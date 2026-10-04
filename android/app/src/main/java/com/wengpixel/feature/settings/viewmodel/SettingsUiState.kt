package com.wengpixel.feature.settings.viewmodel

import com.wengpixel.data.remote.dto.ServerStatusDto

data class SettingsUiState(
    val backendUrl: String = "http://10.0.2.2:8000",
    val exportFormat: String = "PNG",
    val exportQuality: Int = 95,
    val autoSaveHistory: Boolean = true,
    val isTestingConnection: Boolean = false,
    val testConnectionResult: ServerStatusDto? = null,
    val testConnectionError: String? = null
)
