package com.wengpixel.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    @SerialName("error") val error: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("request_id") val requestId: String? = null,
    @SerialName("setup_guide") val setupGuide: String? = null,
    @SerialName("detail") val detail: String? = null
)
