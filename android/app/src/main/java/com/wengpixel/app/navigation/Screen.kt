package com.wengpixel.app.navigation

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Settings : Screen("settings")

    data object Editor : Screen("editor?imagePath={imagePath}&tool={tool}") {
        fun createRoute(imagePath: String, tool: String = "ALL"): String {
            val encodedPath = URLEncoder.encode(imagePath, StandardCharsets.UTF_8.toString())
            return "editor?imagePath=$encodedPath&tool=$tool"
        }
    }
}
