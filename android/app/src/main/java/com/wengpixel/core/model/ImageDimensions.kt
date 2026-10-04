package com.wengpixel.core.model

data class ImageDimensions(
    val width: Int,
    val height: Int
) {
    val resolutionLabel: String get() = "${width} × ${height} px"
}
