package com.wengpixel.feature.editor.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wengpixel.core.common.AppResult
import com.wengpixel.core.common.DispatcherProvider
import com.wengpixel.core.common.ImageUtils
import com.wengpixel.core.model.CropRatio
import com.wengpixel.core.model.ImageDimensions
import com.wengpixel.core.model.ProcessType
import com.wengpixel.core.model.ScaleFactor
import com.wengpixel.data.local.datastore.SettingsDataStore
import com.wengpixel.data.local.file.ImageFileManager
import com.wengpixel.domain.usecase.RemoveBackgroundUseCase
import com.wengpixel.domain.usecase.SaveEditedImageUseCase
import com.wengpixel.domain.usecase.UpscaleImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val removeBackgroundUseCase: RemoveBackgroundUseCase,
    private val upscaleImageUseCase: UpscaleImageUseCase,
    private val saveEditedImageUseCase: SaveEditedImageUseCase,
    private val fileManager: ImageFileManager,
    private val settingsDataStore: SettingsDataStore,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<EditorMessage>()
    val messages: SharedFlow<EditorMessage> = _messages.asSharedFlow()

    // Referensi bitmap mentah transparan sebelum diberi warna latar
    private var baseTransparentBitmap: Bitmap? = null
    private var processingJob: kotlinx.coroutines.Job? = null

    fun initializeImage(filePath: String, initialTool: String = "ALL") {
        viewModelScope.launch(dispatchers.io) {
            val file = File(filePath)
            if (!file.exists()) {
                _messages.emit(EditorMessage.Error("Berkas gambar tidak ditemukan di perangkat."))
                return@launch
            }

            val bitmap = ImageUtils.loadRotatedBitmapFromFile(file, maxDimension = 3000)
            if (bitmap == null) {
                _messages.emit(EditorMessage.Error("Gagal membaca berkas gambar atau format tidak valid."))
                return@launch
            }

            val dimensions = ImageDimensions(bitmap.width, bitmap.height)
            val tab = when (initialTool) {
                "REMOVE_BG" -> EditorTab.REMOVE_BG
                "UPSCALE" -> EditorTab.UPSCALE
                "COLOR" -> EditorTab.BACKGROUND_COLOR
                "CROP" -> EditorTab.CROP_RESIZE
                else -> EditorTab.REMOVE_BG
            }

            _uiState.update {
                it.copy(
                    originalImagePath = filePath,
                    originalBitmap = bitmap,
                    editedBitmap = null,
                    currentWorkingBitmap = bitmap,
                    originalDimensions = dimensions,
                    currentDimensions = dimensions,
                    activeToolTab = tab,
                    selectedBgColor = 0
                )
            }
        }
    }

    fun selectTab(tab: EditorTab) {
        _uiState.update { it.copy(activeToolTab = tab) }
        if (tab == EditorTab.CROP_RESIZE) {
            _uiState.update { it.copy(isCropSheetVisible = true) }
        }
    }

    fun setCropSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isCropSheetVisible = visible) }
    }

    fun setHoldOriginal(isHolding: Boolean) {
        _uiState.update { it.copy(isShowingOriginalHold = isHolding) }
    }

    fun setScaleFactor(scale: ScaleFactor) {
        _uiState.update { it.copy(selectedScaleFactor = scale) }
    }

    fun cancelProcessing() {
        processingJob?.cancel()
        processingJob = null
        _uiState.update {
            it.copy(
                isProcessing = false,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            _messages.emit(EditorMessage.Info("Proses AI dibatalkan."))
        }
    }

    fun applyRemoveBackground() {
        val currentBitmap = _uiState.value.currentWorkingBitmap ?: return
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingMessage = "Menghapus latar belakang gambar via AI...",
                    errorMessage = null
                )
            }

            val tempFile = withContext(dispatchers.io) {
                // Optimasi transmisi: batasi dimensi upload maks 1920px agar respons instan di jaringan
                val maxDim = 1920
                val uploadBitmap = if (currentBitmap.width > maxDim || currentBitmap.height > maxDim) {
                    val ratio = currentBitmap.width.toFloat() / currentBitmap.height.toFloat()
                    val (w, h) = if (ratio > 1f) maxDim to (maxDim / ratio).toInt() else (maxDim * ratio).toInt() to maxDim
                    Bitmap.createScaledBitmap(currentBitmap, w, h, true)
                } else {
                    currentBitmap
                }
                fileManager.saveBitmapToTempCache(uploadBitmap, Bitmap.CompressFormat.PNG)
            }

            removeBackgroundUseCase(tempFile).collect { result ->
                when (result) {
                    is AppResult.Loading -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = true,
                                processingMessage = "Sedang memproses AI pada server..."
                            )
                        }
                    }
                    is AppResult.Success -> {
                        val resultFile = result.data
                        val resultBitmap = withContext(dispatchers.io) {
                            ImageUtils.loadRotatedBitmapFromFile(resultFile)
                        }

                        if (resultBitmap != null) {
                            baseTransparentBitmap = resultBitmap
                            val newDim = ImageDimensions(resultBitmap.width, resultBitmap.height)
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    editedBitmap = resultBitmap,
                                    currentWorkingBitmap = resultBitmap,
                                    currentDimensions = newDim,
                                    lastAppliedProcess = ProcessType.REMOVE_BACKGROUND,
                                    selectedBgColor = 0
                                )
                            }
                            _messages.emit(EditorMessage.Success("Latar belakang berhasil dihapus! Format transparan PNG aktif."))
                        } else {
                            _uiState.update { it.copy(isProcessing = false) }
                            _messages.emit(EditorMessage.Error("Gagal membaca hasil keluaran gambar dari server."))
                        }
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                errorMessage = result.message
                            )
                        }
                        _messages.emit(EditorMessage.Error(result.message))
                    }
                }
            }
        }
    }

    fun applyUpscale(scaleFactor: ScaleFactor = _uiState.value.selectedScaleFactor) {
        val currentBitmap = _uiState.value.currentWorkingBitmap ?: return
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingMessage = "Meningkatkan resolusi foto ${scaleFactor.factor}x...",
                    errorMessage = null
                )
            }

            val tempFile = withContext(dispatchers.io) {
                fileManager.saveBitmapToTempCache(currentBitmap, Bitmap.CompressFormat.PNG)
            }

            upscaleImageUseCase(tempFile, scaleFactor.factor).collect { result ->
                when (result) {
                    is AppResult.Loading -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = true,
                                processingMessage = "Sedang meningkatkan kualitas & ketajaman..."
                            )
                        }
                    }
                    is AppResult.Success -> {
                        val resultFile = result.data
                        val resultBitmap = withContext(dispatchers.io) {
                            ImageUtils.loadRotatedBitmapFromFile(resultFile, maxDimension = 8000)
                        }

                        if (resultBitmap != null) {
                            val newDim = ImageDimensions(resultBitmap.width, resultBitmap.height)
                            val proc = if (scaleFactor == ScaleFactor.X4) ProcessType.UPSCALE_4X else ProcessType.UPSCALE_2X
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    editedBitmap = resultBitmap,
                                    currentWorkingBitmap = resultBitmap,
                                    currentDimensions = newDim,
                                    lastAppliedProcess = proc
                                )
                            }
                            _messages.emit(
                                EditorMessage.Success(
                                    "Resolusi berhasil ditingkatkan ${scaleFactor.factor}x menjadi ${newDim.resolutionLabel}!"
                                )
                            )
                        } else {
                            _uiState.update { it.copy(isProcessing = false) }
                            _messages.emit(EditorMessage.Error("Gagal memuat hasil peningkatan resolusi."))
                        }
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                errorMessage = result.message
                            )
                        }
                        _messages.emit(EditorMessage.Error(result.message))
                    }
                }
            }
        }
    }

    fun applyBackgroundColor(colorInt: Int) {
        val source = baseTransparentBitmap ?: _uiState.value.editedBitmap ?: _uiState.value.currentWorkingBitmap ?: return
        viewModelScope.launch(dispatchers.io) {
            val newBitmap = ImageUtils.applyBackgroundColor(source, colorInt)
            withContext(dispatchers.main) {
                _uiState.update {
                    it.copy(
                        selectedBgColor = colorInt,
                        editedBitmap = newBitmap,
                        currentWorkingBitmap = newBitmap,
                        lastAppliedProcess = ProcessType.COLOR_BACKGROUND
                    )
                }
            }
        }
    }

    fun applyCrop(ratio: CropRatio) {
        val currentBitmap = _uiState.value.currentWorkingBitmap ?: return
        viewModelScope.launch(dispatchers.io) {
            val cropped = if (ratio == CropRatio.FREE) {
                currentBitmap
            } else {
                ImageUtils.cropCenterWithRatio(currentBitmap, ratio.ratio)
            }
            val newDim = ImageDimensions(cropped.width, cropped.height)
            withContext(dispatchers.main) {
                _uiState.update {
                    it.copy(
                        editedBitmap = cropped,
                        currentWorkingBitmap = cropped,
                        currentDimensions = newDim,
                        lastAppliedProcess = ProcessType.CROP_RESIZE
                    )
                }
                _messages.emit(EditorMessage.Success("Gambar berhasil dipotong ke rasio ${ratio.title}."))
            }
        }
    }

    fun applyResize(targetWidth: Int, targetHeight: Int) {
        val currentBitmap = _uiState.value.currentWorkingBitmap ?: return
        viewModelScope.launch(dispatchers.io) {
            val resized = ImageUtils.resizeBitmap(currentBitmap, targetWidth, targetHeight)
            val newDim = ImageDimensions(resized.width, resized.height)
            withContext(dispatchers.main) {
                _uiState.update {
                    it.copy(
                        editedBitmap = resized,
                        currentWorkingBitmap = resized,
                        currentDimensions = newDim,
                        lastAppliedProcess = ProcessType.CROP_RESIZE
                    )
                }
                _messages.emit(EditorMessage.Success("Ukuran berhasil diubah menjadi ${newDim.resolutionLabel}."))
            }
        }
    }

    fun resetToOriginal() {
        val orig = _uiState.value.originalBitmap ?: return
        val origDim = _uiState.value.originalDimensions
        baseTransparentBitmap = null
        _uiState.update {
            it.copy(
                editedBitmap = null,
                currentWorkingBitmap = orig,
                currentDimensions = origDim,
                selectedBgColor = 0,
                lastAppliedProcess = null
            )
        }
        viewModelScope.launch {
            _messages.emit(EditorMessage.Info("Telah dikembalikan ke gambar asli."))
        }
    }

    fun saveToGallery() {
        val bitmapToSave = _uiState.value.editedBitmap ?: _uiState.value.currentWorkingBitmap ?: return
        val origFile = File(_uiState.value.originalImagePath)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingMessage = "Menyimpan ke Galeri..."
                )
            }

            val formatPref = settingsDataStore.exportFormat.first()
            val qualityPref = settingsDataStore.exportQuality.first()

            // Jika latar transparan (selectedBgColor == 0), selalu simpan sebagai PNG untuk menjaga transparansi
            val compressFormat = if (_uiState.value.selectedBgColor == 0 || formatPref == "PNG") {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }

            val uri = withContext(dispatchers.io) {
                saveEditedImageUseCase.saveToGallery(bitmapToSave, compressFormat, qualityPref)
            }

            if (uri != null) {
                val procType = _uiState.value.lastAppliedProcess ?: ProcessType.REMOVE_BACKGROUND
                withContext(dispatchers.io) {
                    saveEditedImageUseCase.recordHistory(origFile, bitmapToSave, procType, compressFormat)
                }

                _uiState.update { it.copy(isProcessing = false) }
                _messages.emit(EditorMessage.Success("Berhasil disimpan ke Galeri (Pictures/WengPixel)!"))
            } else {
                _uiState.update { it.copy(isProcessing = false) }
                _messages.emit(EditorMessage.Error("Gagal menyimpan gambar ke penyimpanan galeri."))
            }
        }
    }

    fun getShareableFile(): File? {
        val bitmap = _uiState.value.editedBitmap ?: _uiState.value.currentWorkingBitmap ?: return null
        val format = if (_uiState.value.selectedBgColor == 0) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        return fileManager.saveBitmapToTempCache(bitmap, format)
    }
}
