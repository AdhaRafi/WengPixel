package com.wengpixel.core.common

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val exception: Throwable, val message: String = exception.localizedMessage ?: "Terjadi kesalahan") : AppResult<Nothing>
    data object Loading : AppResult<Nothing>
}
