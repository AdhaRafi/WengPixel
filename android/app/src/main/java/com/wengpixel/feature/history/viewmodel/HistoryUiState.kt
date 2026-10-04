package com.wengpixel.feature.history.viewmodel

import com.wengpixel.core.model.HistoryItem

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val selectedItem: HistoryItem? = null,
    val isConfirmClearDialogOpen: Boolean = false
)
