package com.wengpixel.feature.editor.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.wengpixel.core.designsystem.PrimaryBlue
import com.wengpixel.core.designsystem.components.BeforeAfterSlider
import com.wengpixel.core.designsystem.components.ColorPickerRow
import com.wengpixel.core.designsystem.components.ProcessingLoadingOverlay
import com.wengpixel.core.designsystem.components.WengPixelTopBar
import com.wengpixel.core.model.ScaleFactor
import com.wengpixel.feature.editor.viewmodel.EditorMessage
import com.wengpixel.feature.editor.viewmodel.EditorTab
import com.wengpixel.feature.editor.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    imagePath: String,
    initialTool: String,
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(imagePath) {
        viewModel.initializeImage(imagePath, initialTool)
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            when (message) {
                is EditorMessage.Success -> snackbarHostState.showSnackbar(message.message)
                is EditorMessage.Error -> snackbarHostState.showSnackbar(message.message)
                is EditorMessage.Info -> snackbarHostState.showSnackbar(message.message)
            }
        }
    }

    Scaffold(
        topBar = {
            WengPixelTopBar(
                title = "Editor Foto",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.resetToOriginal() }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Kembali ke Asli",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = {
                        val shareFile = viewModel.getShareableFile()
                        if (shareFile != null) {
                            shareImageFile(context, shareFile)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { viewModel.saveToGallery() }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Simpan ke Galeri",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Informasi Dimensi Gambar (Ukuran Asli vs Hasil)
            if (uiState.currentDimensions != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resolusi: ${uiState.currentDimensions!!.resolutionLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (uiState.originalDimensions != null &&
                            uiState.originalDimensions != uiState.currentDimensions
                        ) {
                            Text(
                                text = "Asli: ${uiState.originalDimensions!!.resolutionLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Canvas Gambar Utama dengan Slider Sebelum/Sesudah
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                BeforeAfterSlider(
                    originalBitmap = uiState.originalBitmap,
                    editedBitmap = uiState.editedBitmap,
                    backgroundColor = uiState.selectedBgColor,
                    isShowingOriginalHold = uiState.isShowingOriginalHold,
                    modifier = Modifier.fillMaxSize()
                )

                // Tombol Tahan untuk Melihat Asli
                if (uiState.editedBitmap != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        viewModel.setHoldOriginal(true)
                                        tryAwaitRelease()
                                        viewModel.setHoldOriginal(false)
                                    }
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tahan untuk Melihat Asli",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Tab Pemilih Alat Editor
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column {
                    TabRow(
                        selectedTabIndex = uiState.activeToolTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        EditorTab.entries.forEach { tab ->
                            Tab(
                                selected = uiState.activeToolTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                text = {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (uiState.activeToolTab == tab) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    // Panel Kontrol Sesuai Tab Aktif
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (uiState.activeToolTab) {
                            EditorTab.REMOVE_BG -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Hapus latar otomatis dengan AI untuk menghasilkan PNG transparan.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.applyRemoveBackground() },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoFixHigh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Proses Hapus Latar Belakang")
                                    }
                                }
                            }

                            EditorTab.UPSCALE -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        FilterChip(
                                            selected = uiState.selectedScaleFactor == ScaleFactor.X2,
                                            onClick = { viewModel.setScaleFactor(ScaleFactor.X2) },
                                            label = { Text("Skala 2x (Dua Kali)") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        FilterChip(
                                            selected = uiState.selectedScaleFactor == ScaleFactor.X4,
                                            onClick = { viewModel.setScaleFactor(ScaleFactor.X4) },
                                            label = { Text("Skala 4x (Empat Kali)") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.applyUpscale() },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Tingkatkan Resolusi (${uiState.selectedScaleFactor.label})")
                                    }
                                }
                            }

                            EditorTab.BACKGROUND_COLOR -> {
                                Column {
                                    Text(
                                        text = "Pilih warna pengganti latar belakang transparan:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    ColorPickerRow(
                                        selectedColor = uiState.selectedBgColor,
                                        onColorSelected = { viewModel.applyBackgroundColor(it) }
                                    )
                                }
                            }

                            EditorTab.CROP_RESIZE -> {
                                Button(
                                    onClick = { viewModel.setCropSheetVisible(true) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Crop,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Buka Pengaturan Potong & Ukuran")
                                }
                            }
                        }
                    }
                }
            }
        }

        // BottomSheet untuk Potong & Ukuran
        if (uiState.isCropSheetVisible && uiState.currentDimensions != null) {
            CropResizeBottomSheet(
                currentWidth = uiState.currentDimensions!!.width,
                currentHeight = uiState.currentDimensions!!.height,
                onDismiss = { viewModel.setCropSheetVisible(false) },
                onApplyCrop = { viewModel.applyCrop(it) },
                onApplyResize = { w, h -> viewModel.applyResize(w, h) }
            )
        }

        // Overlay Saat AI Memproses
        if (uiState.isProcessing) {
            ProcessingLoadingOverlay(
                message = uiState.processingMessage,
                onCancel = { viewModel.cancelProcessing() }
            )
        }
    }
}

private fun shareImageFile(context: Context, file: java.io.File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan Hasil WengPixel"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
