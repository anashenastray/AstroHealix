package com.example.astrohealix.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.astrohealix.model.AppThemeMode

private fun buildColorScheme(colors: VitaColors): ColorScheme = if (colors.isDark) {
    darkColorScheme(
        primary = colors.borderActive,
        onPrimary = colors.bgPrimary,
        primaryContainer = colors.bgSurfaceHover,
        onPrimaryContainer = colors.textPrimary,
        secondary = colors.statusNormal,
        onSecondary = colors.bgPrimary,
        secondaryContainer = colors.statusNormalBg,
        onSecondaryContainer = colors.statusNormal,
        tertiary = colors.statusMonitor,
        onTertiary = colors.bgPrimary,
        tertiaryContainer = colors.statusMonitorBg,
        onTertiaryContainer = colors.statusMonitor,
        background = colors.bgPrimary,
        onBackground = colors.textPrimary,
        surface = colors.bgSurface,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.bgSurfaceHover,
        onSurfaceVariant = colors.textSecondary,
        outline = colors.borderSubtle,
        outlineVariant = colors.borderSubtle,
        error = colors.statusCritical,
        onError = colors.onColoredButton,
        errorContainer = colors.statusCriticalBg,
        onErrorContainer = colors.statusCritical
    )
} else {
    lightColorScheme(
        primary = colors.borderActive,
        onPrimary = colors.onColoredButton,
        primaryContainer = colors.bgSurfaceHover,
        onPrimaryContainer = colors.textPrimary,
        secondary = colors.statusNormal,
        onSecondary = colors.onColoredButton,
        secondaryContainer = colors.statusNormalBg,
        onSecondaryContainer = colors.statusNormal,
        tertiary = colors.statusMonitor,
        onTertiary = colors.onColoredButton,
        tertiaryContainer = colors.statusMonitorBg,
        onTertiaryContainer = colors.statusMonitor,
        background = colors.bgPrimary,
        onBackground = colors.textPrimary,
        surface = colors.bgSurface,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.bgSurfaceHover,
        onSurfaceVariant = colors.textSecondary,
        outline = colors.borderSubtle,
        outlineVariant = colors.borderSubtle,
        error = colors.statusCritical,
        onError = colors.onColoredButton,
        errorContainer = colors.statusCriticalBg,
        onErrorContainer = colors.statusCritical
    )
}

@Composable
fun AstroHealixTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }
    val activeColors = if (isDark) DarkVitaColors else LightVitaColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalVitaColors provides activeColors) {
        MaterialTheme(
            colorScheme = buildColorScheme(activeColors),
            typography = vitaTypography(activeColors),
            content = content
        )
    }
}
