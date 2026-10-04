package com.wengpixel.feature.home.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wengpixel.core.common.AppResult
import com.wengpixel.core.common.NetworkMonitor
import com.wengpixel.data.local.file.ImageFileManager
import com.wengpixel.domain.usecase.CheckServerStatusUseCase
import com.wengpixel.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeNavigationEvent {
    data class NavigateToEditor(val imagePath: String, val initialTool: String = "ALL") : HomeNavigationEvent
    data object NavigateToSettings : HomeNavigationEvent
    data object NavigateToHistory : HomeNavigationEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val checkServerStatusUseCase: CheckServerStatusUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val networkMonitor: NetworkMonitor,
    private val fileManager: ImageFileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvents: SharedFlow<HomeNavigationEvent> = _navigationEvents.asSharedFlow()

    init {
        observeNetwork()
        observeRecentHistory()
        checkServer()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
                if (online && !_uiState.value.isServerReady) {
                    checkServer()
                }
            }
        }
    }

    private fun observeRecentHistory() {
        viewModelScope.launch {
            getHistoryUseCase.getRecent(5).collect { historyList ->
                _uiState.update { it.copy(recentHistory = historyList) }
            }
        }
    }

    fun checkServer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingServer = true) }
            when (val result = checkServerStatusUseCase()) {
                is AppResult.Success -> {
                    val status = result.data
                    val ready = status.bgRemoval.isReady && status.upscaling.isReady
                    _uiState.update {
                        it.copy(
                            isServerReady = ready,
                            serverStatus = status,
                            serverErrorMessage = if (!ready) "Penyedia AI belum sepenuhnya siap." else null,
                            isCheckingServer = false
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isServerReady = false,
                            serverErrorMessage = result.message,
                            isCheckingServer = false
                        )
                    }
                }
                AppResult.Loading -> {}
            }
        }
    }

    fun onImagePicked(uri: Uri, preferredTool: String = "ALL") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingImage = true) }
            try {
                val tempFile = fileManager.copyUriToTempCache(uri)
                _uiState.update { it.copy(isLoadingImage = false) }
                _navigationEvents.emit(HomeNavigationEvent.NavigateToEditor(tempFile.absolutePath, preferredTool))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingImage = false) }
            }
        }
    }

    fun onHistoryItemClicked(imagePath: String) {
        viewModelScope.launch {
            _navigationEvents.emit(HomeNavigationEvent.NavigateToEditor(imagePath, "ALL"))
        }
    }
}
