package com.example.astrohealix.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BorderActive,
    onPrimary = BgPrimary,
    primaryContainer = BgSurfaceHover,
    onPrimaryContainer = TextPrimary,
    secondary = StatusNormal,
    onSecondary = BgPrimary,
    secondaryContainer = StatusNormalBg,
    onSecondaryContainer = StatusNormal,
    tertiary = StatusMonitor,
    onTertiary = BgPrimary,
    tertiaryContainer = StatusMonitorBg,
    onTertiaryContainer = StatusMonitor,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = BgSurfaceHover,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle,
    error = StatusCritical,
    onError = TextPrimary,
    errorContainer = StatusCriticalBg,
    onErrorContainer = StatusCritical
)

@Composable
fun AstroHealixTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = BgPrimary.toArgb()
                it.navigationBarColor = BgPrimary.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
