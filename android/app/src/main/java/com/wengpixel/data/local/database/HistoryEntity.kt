package com.wengpixel.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wengpixel.core.model.HistoryItem
import com.wengpixel.core.model.ProcessType

@Entity(tableName = "history_entries")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalImagePath: String,
    val editedImagePath: String,
    val thumbnailPath: String,
    val processType: String,
    val width: Int,
    val height: Int,
    val fileSizeBytes: Long,
    val timestamp: Long,
    val notes: String? = null
) {
    fun toDomainModel(): HistoryItem {
        return HistoryItem(
            id = id,
            originalImagePath = originalImagePath,
            editedImagePath = editedImagePath,
            thumbnailPath = thumbnailPath,
            processType = ProcessType.fromString(processType),
            width = width,
            height = height,
            fileSizeBytes = fileSizeBytes,
            timestamp = timestamp,
            notes = notes
        )
    }

    companion object {
        fun fromDomainModel(item: HistoryItem): HistoryEntity {
            return HistoryEntity(
                id = item.id,
                originalImagePath = item.originalImagePath,
                editedImagePath = item.editedImagePath,
                thumbnailPath = item.thumbnailPath,
                processType = item.processType.name,
                width = item.width,
                height = item.height,
                fileSizeBytes = item.fileSizeBytes,
                timestamp = item.timestamp,
                notes = item.notes
            )
        }
    }
}
