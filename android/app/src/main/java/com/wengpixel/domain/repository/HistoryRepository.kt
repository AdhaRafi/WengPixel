package com.wengpixel.domain.repository

import android.graphics.Bitmap
import com.wengpixel.core.model.HistoryItem
import com.wengpixel.core.model.ProcessType
import kotlinx.coroutines.flow.Flow
import java.io.File

interface HistoryRepository {
    fun getAllHistory(): Flow<List<HistoryItem>>
    fun getRecentHistory(limit: Int): Flow<List<HistoryItem>>
    suspend fun getHistoryById(id: Long): HistoryItem?
    suspend fun saveHistory(
        originalFile: File,
        editedBitmap: Bitmap,
        processType: ProcessType,
        format: Bitmap.CompressFormat,
        notes: String? = null
    ): Long
    suspend fun deleteHistory(item: HistoryItem)
    suspend fun clearAllHistory()
}
