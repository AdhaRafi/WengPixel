package com.wengpixel.core.model

enum class ProcessType(val displayName: String, val iconResName: String) {
    REMOVE_BACKGROUND("Hapus Latar", "ic_remove_bg"),
    UPSCALE_2X("Tingkatkan 2x", "ic_upscale_2x"),
    UPSCALE_4X("Tingkatkan 4x", "ic_upscale_4x"),
    CROP_RESIZE("Potong & Ukuran", "ic_crop"),
    COLOR_BACKGROUND("Ganti Latar", "ic_color_bg");

    companion object {
        fun fromString(value: String): ProcessType {
            return entries.find { it.name == value } ?: REMOVE_BACKGROUND
        }
    }
}
