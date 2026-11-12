package com.example.astrohealix.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// ASTROHEALIX NASA VITA-SYS DESIGN SYSTEM — Dark (OLED) & Light (Daylight) Tokens
// ============================================================================

@Immutable
data class VitaColors(
    val bgPrimary: Color,
    val bgSurface: Color,
    val bgSurfaceHover: Color,
    val borderSubtle: Color,
    val borderActive: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val statusNormal: Color,
    val statusMonitor: Color,
    val statusCritical: Color,
    val statusUnavailable: Color,
    val statusNormalBg: Color,
    val statusMonitorBg: Color,
    val statusCriticalBg: Color,
    val statusUnavailableBg: Color,
    val criticalSurfaceGlow: Color,
    val monitorSurfaceGlow: Color,
    val cyanGlow: Color,
    val privateAccent: Color,
    val privateSurface: Color,
    val privateButtonContainer: Color,
    val onColoredButton: Color,
    val isDark: Boolean
)

val DarkVitaColors = VitaColors(
    bgPrimary = Color(0xFF0B0F19),             // Deep Space Navy — main background
    bgSurface = Color(0xFF161C2E),             // Slate Blue-Grey — cards/containers
    bgSurfaceHover = Color(0xFF1F2840),        // Interactive surface hover
    borderSubtle = Color(0xFF2A354D),          // Container divider
    borderActive = Color(0xFF00E5FF),          // Cyan highlight border
    textPrimary = Color(0xFFF4F6FB),           // Stark White 98%
    textSecondary = Color(0xFF8E9BAE),         // Muted Slate
    textTertiary = Color(0xFF6B7A90),          // Dim Slate
    textDisabled = Color(0xFF4A5568),          // Offline/deactivated
    statusNormal = Color(0xFF00E676),          // Medical Emerald Green
    statusMonitor = Color(0xFFFFB300),         // Caution Amber
    statusCritical = Color(0xFFFF3D00),        // Alert Red
    statusUnavailable = Color(0xFF4A5568),     // Data Grey
    statusNormalBg = Color(0x2600E676),        // 15% opacity of StatusNormal
    statusMonitorBg = Color(0x26FFB300),       // 15% opacity of StatusMonitor
    statusCriticalBg = Color(0x26FF3D00),      // 15% opacity of StatusCritical
    statusUnavailableBg = Color(0x264A5568),   // 15% opacity of StatusUnavailable
    criticalSurfaceGlow = Color(0xFF1C1520),   // Dark red-tinted bg for critical cards
    monitorSurfaceGlow = Color(0xFF1C1A15),    // Subtle amber-tinted bg for monitor cards
    cyanGlow = Color(0x3500E5FF),              // Cyan accent glow
    privateAccent = Color(0xFF9F85FF),         // Confidential wellbeing accent
    privateSurface = Color(0xFF19162D),        // Confidential wellbeing surface
    privateButtonContainer = Color(0xFF6A4FE0),
    onColoredButton = Color.White,
    isDark = true
)

val LightVitaColors = VitaColors(
    bgPrimary = Color(0xFFF3F6FA),             // Daylight Cabin Mist — main background
    bgSurface = Color(0xFFFFFFFF),             // Clinical Crisp White — cards/containers
    bgSurfaceHover = Color(0xFFE6ECF5),        // Interactive slate tint
    borderSubtle = Color(0xFFD0D9E6),          // Clean instrument divider
    borderActive = Color(0xFF006699),          // Deep Telemetry Cerulean (>= 6:1 contrast)
    textPrimary = Color(0xFF0F172A),           // Deep Ink Navy
    textSecondary = Color(0xFF475569),         // Instrument Slate
    textTertiary = Color(0xFF64748B),          // Muted Slate
    textDisabled = Color(0xFF94A3B8),          // Offline/deactivated
    statusNormal = Color(0xFF008744),          // Deep Clinical Emerald Green
    statusMonitor = Color(0xFFB26A00),         // High-contrast Aerospace Amber
    statusCritical = Color(0xFFD32F2F),        // Alert Crimson Red
    statusUnavailable = Color(0xFF64748B),     // Neutral Slate Grey
    statusNormalBg = Color(0x1F008744),        // 12% green tint
    statusMonitorBg = Color(0x1FB26A00),       // 12% amber tint
    statusCriticalBg = Color(0x1AD32F2F),      // 10% red tint
    statusUnavailableBg = Color(0x1F64748B),   // 12% neutral tint
    criticalSurfaceGlow = Color(0xFFFFF1F2),   // Soft crimson-white card tint
    monitorSurfaceGlow = Color(0xFFFFF8EB),    // Soft amber-white card tint
    cyanGlow = Color(0x24006699),              // Cerulean accent tint
    privateAccent = Color(0xFF5B3CC4),         // Confidential wellbeing deep violet
    privateSurface = Color(0xFFF3F0FF),        // Soft lavender-white private surface
    privateButtonContainer = Color(0xFF5B3CC4),
    onColoredButton = Color.White,
    isDark = false
)

val LocalVitaColors = staticCompositionLocalOf { DarkVitaColors }

// Base Surface & Structural
val BgPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.bgPrimary
val BgSurface: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.bgSurface
val BgSurfaceHover: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.bgSurfaceHover
val BorderSubtle: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.borderSubtle
val BorderActive: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.borderActive

// Backward-compatible structural aliases
val BgApp: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.bgPrimary
val BgSurfaceElevated: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.bgSurfaceHover

// Typography
val TextPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.textPrimary
val TextSecondary: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.textSecondary
val TextTertiary: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.textTertiary
val TextDisabled: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.textDisabled
val TextCyan: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.borderActive

// Health Status
val StatusNormal: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.statusNormal
val StatusMonitor: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.statusMonitor
val StatusCritical: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.statusCritical
val StatusUnavailable: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.statusUnavailable

// Status pill backgrounds
val StatusNormalBg: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.statusNormalBg
val StatusMonitorBg: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.statusMonitorBg
val StatusCriticalBg: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.statusCriticalBg
val StatusUnavailableBg: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.statusUnavailableBg

// Alert surface accents
val CriticalSurfaceGlow: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.criticalSurfaceGlow
val MonitorSurfaceGlow: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.monitorSurfaceGlow
val CyanGlow: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.cyanGlow

// Wellbeing / button contrast tokens
val PrivateAccent: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.privateAccent
val PrivateSurface: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.privateSurface
val PrivateButtonContainer: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.privateButtonContainer
val OnColoredButton: Color
    @Composable @ReadOnlyComposable get() = LocalVitaColors.current.onColoredButton
