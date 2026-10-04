package com.wengpixel.domain.usecase

import com.wengpixel.core.model.HistoryItem
import com.wengpixel.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    fun getAll(): Flow<List<HistoryItem>> = repository.getAllHistory()
    fun getRecent(limit: Int = 5): Flow<List<HistoryItem>> = repository.getRecentHistory(limit)
    suspend fun getById(id: Long): HistoryItem? = repository.getHistoryById(id)
}
