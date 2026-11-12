package com.example.astrohealix.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// ASTROHEALIX NASA VITA-SYS DESIGN SYSTEM — Space-Grade Color Tokens
// ============================================================================

// Base Surface & Structural
val BgPrimary = Color(0xFF0B0F19)             // Deep Space Navy — main background
val BgSurface = Color(0xFF161C2E)             // Slate Blue-Grey — cards/containers
val BgSurfaceHover = Color(0xFF1F2840)        // Interactive surface hover
val BorderSubtle = Color(0xFF2A354D)          // Container divider
val BorderActive = Color(0xFF00E5FF)          // Cyan highlight border

// Backward-compatible structural aliases
val BgApp = BgPrimary
val BgSurfaceElevated = BgSurfaceHover

// Typography
val TextPrimary = Color(0xFFF4F6FB)           // Stark White 98%
val TextSecondary = Color(0xFF8E9BAE)         // Muted Slate
val TextTertiary = Color(0xFF6B7A90)          // Dim Slate
val TextDisabled = Color(0xFF4A5568)          // Offline/deactivated
val TextCyan = BorderActive                   // Cyan accent text

// Health Status
val StatusNormal = Color(0xFF00E676)          // Medical Emerald Green
val StatusMonitor = Color(0xFFFFB300)         // Caution Amber
val StatusCritical = Color(0xFFFF3D00)        // Alert Red
val StatusUnavailable = Color(0xFF4A5568)     // Data Grey

// Status pill backgrounds at 15% opacity
val StatusNormalBg = Color(0x2600E676)        // 15% opacity of StatusNormal
val StatusMonitorBg = Color(0x26FFB300)       // 15% opacity of StatusMonitor
val StatusCriticalBg = Color(0x26FF3D00)      // 15% opacity of StatusCritical
val StatusUnavailableBg = Color(0x264A5568)   // 15% opacity of StatusUnavailable

// Alert surface accents
val CriticalSurfaceGlow = Color(0xFF1C1520)   // Dark red-tinted bg for critical cards
val MonitorSurfaceGlow = Color(0xFF1C1A15)    // Subtle amber-tinted bg for monitor cards
val CyanGlow = Color(0x3500E5FF)              // Cyan accent glow
