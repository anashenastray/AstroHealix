package com.example.astrohealix.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// ASTROHEALIX APPLE-LEVEL VOID BLACK GLASSMORPHISM DESIGN SYSTEM
// (Monochrome, Frosted Specular Materials, Zero Cyan/Green/Orange Noise)
// ============================================================================

// Base Void Canvas
val VoidBlack = Color(0xFF000000)             // Pure True Void Black (OLED Zero)
val BgApp = VoidBlack                         // Root background
val BgAppGradientStart = Color(0xFF050508)    // Subtle top specular atmospheric depth
val BgAppGradientEnd = Color(0xFF000000)      // Pure void

// Apple Frosted Glass Surface Tiers (Over Void Black)
val GlassUltraThin = Color(0x0AFFFFFF)        // ~4% white: lowest surface tier
val GlassThin = Color(0x12FFFFFF)             // ~7% white: default card & container surface
val GlassRegular = Color(0x1AFFFFFF)          // ~10% white: elevated cards & dialogs
val GlassThick = Color(0x24FFFFFF)            // ~14% white: active controls & hover states
val GlassEmphasized = Color(0x33FFFFFF)       // ~20% white: selected tab capsules & prominent pills

// Backward-compatible alias mappings
val BgSurface = GlassThin
val BgSurfaceElevated = GlassRegular
val BgGlassSurface = GlassThin
val BgGlassSurfaceElevated = GlassRegular
val BgGlassCard = GlassThin
val BgGlassHover = GlassThick
val BgGlassDark = Color(0x08FFFFFF)

// Apple Specular Hairline Glass Borders
val BorderGlassSpecular = Color(0x38FFFFFF)   // ~22% white: top light catch reflection
val BorderGlassSubtle = Color(0x18FFFFFF)     // ~9% white: side and bottom boundary
val BorderSubtle = BorderGlassSubtle
val BorderActive = Color(0xFFFFFFFF)          // Pure white active highlight

// Dynamic Glass Specular Brushes
val GlassCardBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x40FFFFFF), // Top specular sheen catching light
        Color(0x18FFFFFF), // Soft mid glass border
        Color(0x0AFFFFFF)  // Faint bottom edge
    )
)

val GlassCardFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x18FFFFFF), // Frosted top light sheen
        Color(0x0EFFFFFF)  // Translucent dark glass well
    )
)

val GlassElevatedBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x55FFFFFF),
        Color(0x24FFFFFF),
        Color(0x10FFFFFF)
    )
)

val GlassElevatedFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x26FFFFFF),
        Color(0x1AFFFFFF)
    )
)

// Apple VisionOS Frosted Modal Glass (Deep Void Translucent Occlusion + Specular Sheen)
val GlassModalFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xF414141A), // Frosted elevated dark glass with top light sheen
        Color(0xFA08080C)  // Deep void dark glass well
    )
)

// Typography Hierarchy (Pure Monochrome Starkness)
val TextPrimary = Color(0xFFFFFFFF)           // 100% Stark Pure White
val TextSecondary = Color(0xFFA1A1AA)         // Frosted Silver (Zinc-400)
val TextTertiary = Color(0xFF71717A)          // Muted Charcoal (Zinc-500)
val TextDisabled = Color(0xFF52525B)          // Offline / Disabled (Zinc-600)
val TextCyan = TextPrimary                    // Neutralized to pure white

// Minimalist Glass Status Tokens (Monochrome Opacity & Stark Contrast)
val StatusNormal = Color(0xFFFFFFFF)          // Stark White
val StatusMonitor = Color(0xFFE4E4E7)         // Refined Silver White (Zinc-200)
val StatusCritical = Color(0xFFFFFFFF)        // High-Contrast Stark White
val StatusUnavailable = Color(0xFF71717A)     // Translucent Grey (Zinc-500)

// Subtle Frosted Glass Pill Backings
val StatusNormalBg = Color(0x1AFFFFFF)        // Frosted Glass White
val StatusMonitorBg = Color(0x22FFFFFF)       // Slightly Elevated Glass
val StatusCriticalBg = Color(0x30FFFFFF)      // Emphasized Contrast Glass
val StatusUnavailableBg = Color(0x12FFFFFF)   // Recessed Glass

// Alert Surface Accents (Pure Smoky Dark Glass with Specular Edge)
val CriticalSurfaceGlow = Color(0x22FFFFFF)
val MonitorSurfaceGlow = Color(0x1AFFFFFF)
val CyanGlow = Color(0x20FFFFFF)
