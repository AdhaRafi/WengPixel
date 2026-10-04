package com.wengpixel.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderStatusDto(
    @SerialName("name") val name: String,
    @SerialName("is_ready") val isReady: Boolean,
    @SerialName("details") val details: String,
    @SerialName("setup_guide") val setupGuide: String? = null
)

@Serializable
data class ServerStatusDto(
    @SerialName("app_name") val appName: String,
    @SerialName("version") val version: String,
    @SerialName("status") val status: String,
    @SerialName("bg_removal") val bgRemoval: ProviderStatusDto,
    @SerialName("upscaling") val upscaling: ProviderStatusDto,
    @SerialName("max_file_size_mb") val maxFileSizeMb: Int = 20,
    @SerialName("supported_formats") val supportedFormats: List<String> = emptyList()
)
