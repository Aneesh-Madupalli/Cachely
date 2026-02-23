package com.cachely.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

enum class CachelyThemeMode {
    LIGHT,
    DARK
}

val LocalThemeMode = staticCompositionLocalOf { CachelyThemeMode.DARK }
val LocalSetThemeMode = staticCompositionLocalOf<(CachelyThemeMode) -> Unit> { {} }

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = OnSurfaceVariant,
    background = BackgroundDark,
    onBackground = OnBackground,
    error = ErrorMuted
)

private val LightColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceElevatedLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    error = ErrorMuted
)

private val CachelyTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

private val CachelyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun CachelyTheme(
    mode: CachelyThemeMode = CachelyThemeMode.DARK,
    onModeChange: (CachelyThemeMode) -> Unit = {},
    content: @Composable () -> Unit
) {
    val colorScheme = when (mode) {
        CachelyThemeMode.DARK -> DarkColorScheme
        CachelyThemeMode.LIGHT -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            val useDarkIcons = mode == CachelyThemeMode.LIGHT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkIcons
            window.navigationBarColor = colorScheme.background.toArgb()
        }
    }
    CompositionLocalProvider(
        LocalThemeMode provides mode,
        LocalSetThemeMode provides onModeChange
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CachelyTypography,
            shapes = CachelyShapes,
            content = content
        )
    }
}
