package com.wengpixel.data.repository

import android.graphics.Bitmap
import com.wengpixel.core.common.DispatcherProvider
import com.wengpixel.core.model.HistoryItem
import com.wengpixel.core.model.ProcessType
import com.wengpixel.data.local.database.HistoryDao
import com.wengpixel.data.local.database.HistoryEntity
import com.wengpixel.data.local.file.ImageFileManager
import com.wengpixel.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao,
    private val fileManager: ImageFileManager,
    private val dispatchers: DispatcherProvider
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<HistoryItem>> {
        return historyDao.getAllHistory()
            .map { list -> list.map { it.toDomainModel() } }
            .flowOn(dispatchers.io)
    }

    override fun getRecentHistory(limit: Int): Flow<List<HistoryItem>> {
        return historyDao.getRecentHistory(limit)
            .map { list -> list.map { it.toDomainModel() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getHistoryById(id: Long): HistoryItem? = withContext(dispatchers.io) {
        historyDao.getHistoryById(id)?.toDomainModel()
    }

    override suspend fun saveHistory(
        originalFile: File,
        editedBitmap: Bitmap,
        processType: ProcessType,
        format: Bitmap.CompressFormat,
        notes: String?
    ): Long = withContext(dispatchers.io) {
        val (origPath, editPath, thumbPath) = fileManager.persistForHistory(
            originalFile = originalFile,
            editedBitmap = editedBitmap,
            format = format
        )
        val editFile = File(editPath)

        val entity = HistoryEntity(
            originalImagePath = origPath,
            editedImagePath = editPath,
            thumbnailPath = thumbPath,
            processType = processType.name,
            width = editedBitmap.width,
            height = editedBitmap.height,
            fileSizeBytes = editFile.length(),
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        historyDao.insertHistory(entity)
    }

    override suspend fun deleteHistory(item: HistoryItem): Unit = withContext(dispatchers.io) {
        historyDao.deleteHistoryById(item.id)
        fileManager.deleteHistoryFiles(
            originalPath = item.originalImagePath,
            editedPath = item.editedImagePath,
            thumbnailPath = item.thumbnailPath
        )
    }

    override suspend fun clearAllHistory(): Unit = withContext(dispatchers.io) {
        historyDao.clearAllHistory()
    }
}
