package com.example.astrohealix.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// ASTROHEALIX VOID BLACK GLASSMORPHISM DESIGN SYSTEM
// ============================================================================

// Base Void Canvas Tokens
val VoidBlack = Color(0xFF020408)             // Deepest Obsidian Space Void
val BgApp = VoidBlack                         // Root app background
val BgAppGradientStart = Color(0xFF070B14)    // Subtle cosmic zenith tint
val BgAppGradientEnd = Color(0xFF010204)      // Pure void nadir

// Translucent Glass Surfaces
val BgGlassSurface = Color(0x3B101728)         // Smoky obsidian frosted glass
val BgGlassSurfaceElevated = Color(0x52141F36) // Elevated glass inspection container
val BgGlassCard = Color(0x380D1526)            // Card frosted glass backing
val BgGlassHover = Color(0x521A2847)           // Interactive hover glass state
val BgGlassDark = Color(0x26060A14)            // Recessed dark glass well

// Specular Glass Hairline Borders
val BorderSubtle = Color(0x24475D85)          // Frosted glass boundary line
val BorderGlassSpecular = Color(0x33A0C4FF)    // Ambient starlight reflection border
val BorderActive = Color(0xFF00F0FF)          // Electric Cyan Neon Accent
val CyanGlow = Color(0x3300F0FF)              // 20% Cyan Glow Halo

// Glass Specular Gradients
val GlassCardBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x4000F0FF), // Top specular starlight highlight
        Color(0x1A475D85), // Subtle mid glass tone
        Color(0x0A000000)  // Faded shadow bottom
    )
)

val GlassCardFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x38182238), // Slightly lighter top frosted sheen
        Color(0x280B1220)  // Deep void glass base
    )
)

// Legacy alias mapping for instant compatibility
val BgSurface = BgGlassSurface
val BgSurfaceElevated = BgGlassSurfaceElevated

// Typography Colors
val TextPrimary = Color(0xFFFFFFFF)           // Pure Stark White
val TextSecondary = Color(0xFF94A3B8)         // Frosted Titanium Slate
val TextDisabled = Color(0xFF475569)          // Offline / Data Unavailable Text
val TextCyan = Color(0xFF38E1FF)              // Electric Telemetry Cyan

// Health Status & Alert Color Tokens (Vibrant Neon Glow on Glass)
val StatusNormal = Color(0xFF00FFA3)          // Neon Emerald Ice
val StatusMonitor = Color(0xFFFFBA08)         // Radiant Pulsar Amber
val StatusCritical = Color(0xFFFF2A55)        // Quantum Supernova Crimson
val StatusUnavailable = Color(0xFF64748B)     // Frosted Smoked Slate

// Translucent Neon Glass Pills (12-16% Opacity + Glass Sheen)
val StatusNormalBg = Color(0x1F00FFA3)
val StatusMonitorBg = Color(0x24FFBA08)
val StatusCriticalBg = Color(0x26FF2A55)
val StatusUnavailableBg = Color(0x2664748B)

// Glass Alert Glow Backings
val CriticalSurfaceGlow = Color(0x2E2A0A14)
val MonitorSurfaceGlow = Color(0x2E2A1C08)
