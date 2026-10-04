package com.wengpixel.feature.home.viewmodel

import com.wengpixel.core.model.HistoryItem
import com.wengpixel.data.remote.dto.ServerStatusDto

data class HomeUiState(
    val isOnline: Boolean = true,
    val isServerReady: Boolean = false,
    val serverStatus: ServerStatusDto? = null,
    val serverErrorMessage: String? = null,
    val isCheckingServer: Boolean = false,
    val recentHistory: List<HistoryItem> = emptyList(),
    val isLoadingImage: Boolean = false
)
