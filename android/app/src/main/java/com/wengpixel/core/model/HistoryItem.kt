package com.wengpixel.core.model

data class HistoryItem(
    val id: Long = 0,
    val originalImagePath: String,
    val editedImagePath: String,
    val thumbnailPath: String,
    val processType: ProcessType,
    val width: Int,
    val height: Int,
    val fileSizeBytes: Long,
    val timestamp: Long,
    val notes: String? = null
)
