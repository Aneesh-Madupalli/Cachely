package com.cachely.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cachely.app.data.CacheCleaner
import com.cachely.app.data.PreferencesRepository
import com.cachely.app.ui.HomeScreen
import com.cachely.app.ui.HomeViewModel
import com.cachely.app.ui.HomeViewModelFactory
import com.cachely.app.ui.PermissionScreen
import com.cachely.app.ui.SettingsScreen
import com.cachely.app.ui.SettingsViewModel
import com.cachely.app.ui.SettingsViewModelFactory

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PERMISSION = "permission"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val cacheCleaner = remember(context) { CacheCleaner(context.applicationContext) }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(cacheCleaner)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            val context = LocalContext.current
            val preferences = remember { PreferencesRepository(context.applicationContext) }
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(preferences)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToPermission = { navController.navigate(Routes.PERMISSION) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PERMISSION) {
            PermissionScreen(
                onEnable = { navController.popBackStack() },
                onNotNow = { navController.popBackStack() }
            )
        }
    }
}
