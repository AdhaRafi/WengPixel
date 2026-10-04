package com.wengpixel.feature.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wengpixel.core.model.HistoryItem
import com.wengpixel.domain.usecase.DeleteHistoryUseCase
import com.wengpixel.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val deleteHistoryUseCase: DeleteHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getHistoryUseCase.getAll().collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    fun deleteItem(item: HistoryItem) {
        viewModelScope.launch {
            deleteHistoryUseCase.delete(item)
        }
    }

    fun setConfirmClearDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isConfirmClearDialogOpen = visible) }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            val currentItems = _uiState.value.items
            currentItems.forEach { item ->
                deleteHistoryUseCase.delete(item)
            }
            deleteHistoryUseCase.clearAll()
            _uiState.update { it.copy(isConfirmClearDialogOpen = false) }
        }
    }
}
