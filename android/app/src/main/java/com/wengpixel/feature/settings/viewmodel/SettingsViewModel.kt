package com.wengpixel.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wengpixel.core.common.AppResult
import com.wengpixel.domain.repository.SettingsRepository
import com.wengpixel.domain.usecase.CheckServerStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val checkServerStatusUseCase: CheckServerStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.backendUrl.collect { url ->
                _uiState.update { it.copy(backendUrl = url) }
            }
        }
        viewModelScope.launch {
            settingsRepository.exportFormat.collect { format ->
                _uiState.update { it.copy(exportFormat = format) }
            }
        }
        viewModelScope.launch {
            settingsRepository.exportQuality.collect { quality ->
                _uiState.update { it.copy(exportQuality = quality) }
            }
        }
        viewModelScope.launch {
            settingsRepository.autoSaveHistory.collect { enabled ->
                _uiState.update { it.copy(autoSaveHistory = enabled) }
            }
        }
    }

    fun updateBackendUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.updateBackendUrl(url)
            _uiState.update { it.copy(backendUrl = url, testConnectionResult = null, testConnectionError = null) }
        }
    }

    fun updateExportFormat(format: String) {
        viewModelScope.launch {
            settingsRepository.updateExportFormat(format)
        }
    }

    fun updateExportQuality(quality: Int) {
        viewModelScope.launch {
            settingsRepository.updateExportQuality(quality)
        }
    }

    fun updateAutoSaveHistory(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAutoSaveHistory(enabled)
        }
    }

    fun testServerConnection() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTestingConnection = true,
                    testConnectionResult = null,
                    testConnectionError = null
                )
            }
            when (val result = checkServerStatusUseCase()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isTestingConnection = false,
                            testConnectionResult = result.data,
                            testConnectionError = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isTestingConnection = false,
                            testConnectionResult = null,
                            testConnectionError = result.message
                        )
                    }
                }
                AppResult.Loading -> {}
            }
        }
    }
}
