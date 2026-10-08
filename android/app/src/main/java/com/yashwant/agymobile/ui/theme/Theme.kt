package com.yashwant.agymobile.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AntigravityBlue,
    onPrimary = AntigravityDarkBg,
    primaryContainer = AntigravitySurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = AntigravityCyan,
    onSecondary = AntigravityDarkBg,
    secondaryContainer = AntigravitySurfaceHighlight,
    onSecondaryContainer = TextPrimary,
    background = AntigravityDarkBg,
    onBackground = TextPrimary,
    surface = AntigravitySurface,
    onSurface = TextPrimary,
    surfaceVariant = AntigravitySurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = AntigravityBorder
)

@Composable
fun AgyTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AntigravityDarkBg.toArgb()
                window.navigationBarColor = AntigravityDarkBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AgyTypography,
        content = content
    )
}
