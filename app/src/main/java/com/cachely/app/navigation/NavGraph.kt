package com.cachely.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cachely.app.R
import com.cachely.app.data.AppScanner
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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME

    val cacheCleaner = remember(context) { CacheCleaner(context.applicationContext) }
    val appScanner = remember(context) { AppScanner(context.applicationContext) }

    val showBottomBar = currentRoute == Routes.HOME || currentRoute == Routes.SETTINGS

    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = com.cachely.app.ui.theme.SurfaceDark,
                    contentColor = com.cachely.app.ui.theme.OnSurface
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            if (currentRoute != Routes.HOME) {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_home),
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.cachely.app.ui.theme.Accent,
                            selectedTextColor = com.cachely.app.ui.theme.Accent,
                            unselectedIconColor = com.cachely.app.ui.theme.OnSurfaceVariant,
                            unselectedTextColor = com.cachely.app.ui.theme.OnSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = {
                            if (currentRoute != Routes.SETTINGS) {
                                navController.navigate(Routes.SETTINGS) {
                                    popUpTo(Routes.HOME) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.cachely.app.ui.theme.Accent,
                            selectedTextColor = com.cachely.app.ui.theme.Accent,
                            unselectedIconColor = com.cachely.app.ui.theme.OnSurfaceVariant,
                            unselectedTextColor = com.cachely.app.ui.theme.OnSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.HOME) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(cacheCleaner, appScanner)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToPermission = { navController.navigate(Routes.PERMISSION) }
                )
            }
            composable(Routes.SETTINGS) {
                val ctx = LocalContext.current
                val preferences = remember { PreferencesRepository(ctx.applicationContext) }
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModelFactory(preferences)
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToPermission = { navController.navigate(Routes.PERMISSION) },
                    onNavigateBack = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack(Routes.HOME, false)
                            } else {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
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
}
