package com.wengpixel.domain.repository

import com.wengpixel.core.common.AppResult
import com.wengpixel.data.remote.dto.ServerStatusDto
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ImageProcessingRepository {
    fun removeBackground(imageFile: File): Flow<AppResult<File>>
    fun upscaleImage(imageFile: File, scale: Int): Flow<AppResult<File>>
    suspend fun checkServerStatus(): AppResult<ServerStatusDto>
}
