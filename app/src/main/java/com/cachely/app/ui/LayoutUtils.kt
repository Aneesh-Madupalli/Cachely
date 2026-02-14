package com.cachely.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Returns responsive horizontal padding based on screen width.
 * Small phones: 16dp, medium: 24dp, large/tablet: 32dp.
 */
@Composable
fun screenPadding(): Dp {
    val config = LocalConfiguration.current
    return when {
        config.screenWidthDp < 360 -> 16.dp
        config.screenWidthDp < 600 -> 24.dp
        else -> 32.dp
    }
}
