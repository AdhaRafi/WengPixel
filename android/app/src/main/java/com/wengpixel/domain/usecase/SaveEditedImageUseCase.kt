package com.wengpixel.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.wengpixel.core.model.ProcessType
import com.wengpixel.data.local.file.ImageFileManager
import com.wengpixel.domain.repository.HistoryRepository
import com.wengpixel.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

class SaveEditedImageUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val fileManager: ImageFileManager
) {
    suspend fun saveToGallery(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): Uri? {
        return fileManager.exportToGallery(bitmap, format, quality)
    }

    suspend fun recordHistory(
        originalFile: File,
        editedBitmap: Bitmap,
        processType: ProcessType,
        format: Bitmap.CompressFormat
    ): Long {
        val autoSave = settingsRepository.autoSaveHistory.first()
        return if (autoSave) {
            historyRepository.saveHistory(originalFile, editedBitmap, processType, format)
        } else {
            -1L
        }
    }
}
