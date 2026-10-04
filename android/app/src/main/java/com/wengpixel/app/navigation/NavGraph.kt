package com.wengpixel.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wengpixel.feature.editor.ui.EditorScreen
import com.wengpixel.feature.editor.viewmodel.EditorViewModel
import com.wengpixel.feature.history.ui.HistoryScreen
import com.wengpixel.feature.history.viewmodel.HistoryViewModel
import com.wengpixel.feature.home.ui.HomeScreen
import com.wengpixel.feature.home.viewmodel.HomeViewModel
import com.wengpixel.feature.settings.ui.SettingsScreen
import com.wengpixel.feature.settings.viewmodel.SettingsViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun WengPixelNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(route = Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToEditor = { path, tool ->
                    navController.navigate(Screen.Editor.createRoute(path, tool))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("imagePath") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("tool") {
                    type = NavType.StringType
                    defaultValue = "ALL"
                }
            )
        ) { backStackEntry ->
            val rawPath = backStackEntry.arguments?.getString("imagePath") ?: ""
            val tool = backStackEntry.arguments?.getString("tool") ?: "ALL"
            val decodedPath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8.toString())

            val editorViewModel: EditorViewModel = hiltViewModel()
            EditorScreen(
                imagePath = decodedPath,
                initialTool = tool,
                viewModel = editorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(route = Screen.History.route) {
            val historyViewModel: HistoryViewModel = hiltViewModel()
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenInEditor = { imagePath ->
                    navController.navigate(Screen.Editor.createRoute(imagePath, "ALL"))
                }
            )
        }

        composable(route = Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
