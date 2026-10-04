package com.wengpixel.domain.usecase

import com.wengpixel.core.common.AppResult
import com.wengpixel.domain.repository.ImageProcessingRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class UpscaleImageUseCase @Inject constructor(
    private val repository: ImageProcessingRepository
) {
    operator fun invoke(imageFile: File, scale: Int): Flow<AppResult<File>> {
        return repository.upscaleImage(imageFile, scale)
    }
}
