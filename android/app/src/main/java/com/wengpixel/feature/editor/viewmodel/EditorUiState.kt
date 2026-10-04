package com.wengpixel.feature.editor.viewmodel

import android.graphics.Bitmap
import com.wengpixel.core.model.CropRatio
import com.wengpixel.core.model.ImageDimensions
import com.wengpixel.core.model.ProcessType
import com.wengpixel.core.model.ScaleFactor
import java.io.File

sealed interface EditorMessage {
    data class Success(val message: String) : EditorMessage
    data class Error(val message: String) : EditorMessage
    data class Info(val message: String) : EditorMessage
}

data class EditorUiState(
    val originalImagePath: String = "",
    val originalBitmap: Bitmap? = null,
    val editedBitmap: Bitmap? = null,
    val currentWorkingBitmap: Bitmap? = null,
    val isProcessing: Boolean = false,
    val processingMessage: String = "",
    val activeToolTab: EditorTab = EditorTab.REMOVE_BG,
    val selectedBgColor: Int = 0, // 0 = transparan
    val selectedScaleFactor: ScaleFactor = ScaleFactor.X2,
    val originalDimensions: ImageDimensions? = null,
    val currentDimensions: ImageDimensions? = null,
    val isShowingOriginalHold: Boolean = false,
    val isCropSheetVisible: Boolean = false,
    val lastAppliedProcess: ProcessType? = null,
    val exportSuccessMessage: String? = null,
    val errorMessage: String? = null
)

enum class EditorTab(val title: String) {
    REMOVE_BG("Hapus Latar"),
    UPSCALE("Tingkatkan"),
    BACKGROUND_COLOR("Warna Latar"),
    CROP_RESIZE("Potong & Ukuran")
}
