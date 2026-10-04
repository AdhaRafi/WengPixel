package com.wengpixel.domain.usecase

import com.wengpixel.core.model.HistoryItem
import com.wengpixel.domain.repository.HistoryRepository
import javax.inject.Inject

class DeleteHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend fun delete(item: HistoryItem) = repository.deleteHistory(item)
    suspend fun clearAll() = repository.clearAllHistory()
}
