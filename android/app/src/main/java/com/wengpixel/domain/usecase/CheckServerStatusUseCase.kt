package com.wengpixel.domain.usecase

import com.wengpixel.core.common.AppResult
import com.wengpixel.data.remote.dto.ServerStatusDto
import com.wengpixel.domain.repository.ImageProcessingRepository
import javax.inject.Inject

class CheckServerStatusUseCase @Inject constructor(
    private val repository: ImageProcessingRepository
) {
    suspend operator fun invoke(): AppResult<ServerStatusDto> {
        return repository.checkServerStatus()
    }
}
