package com.wengpixel.core.model

enum class CropRatio(val title: String, val ratio: Float) {
    FREE("Bebas", 0f),
    SQUARE("1:1 (Persegi)", 1f),
    FOUR_THREE("4:3 (Foto)", 4f / 3f),
    SIXTEEN_NINE("16:9 (Lanskap)", 16f / 9f),
    NINE_SIXTEEN("9:16 (Story/TikTok)", 9f / 16f)
}
