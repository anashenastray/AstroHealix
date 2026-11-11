package com.example.astrohealix.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// ASTROHEALIX APPLE-LEVEL LUMINOUS GLASSMORPHISM DESIGN SYSTEM
// (Bright Frosted Specular Materials over Deep Obsidian Space Titanium)
// ============================================================================

// Base Obsidian Canvas (Noticeably brighter than void black, high luminous contrast)
val VoidBlack = Color(0xFF131722)             // Luminous Obsidian Space Slate (Apple dark titanium)
val BgApp = VoidBlack                         // Root background
val BgAppGradientStart = Color(0xFF1B2030)    // Subtle top specular atmospheric depth
val BgAppGradientEnd = Color(0xFF11141E)      // Deep obsidian space base

// Apple Frosted Glass Surface Tiers (Luminous Frosted White over Obsidian)
val GlassUltraThin = Color(0x20FFFFFF)        // ~12% white: subtle recessed containers
val GlassThin = Color(0x2CFFFFFF)             // ~17% white: default cards & glass containers
val GlassRegular = Color(0x3BFFFFFF)          // ~23% white: elevated cards, active panels
val GlassThick = Color(0x4EFFFFFF)            // ~31% white: prominent pills, active tab states
val GlassEmphasized = Color(0x65FFFFFF)       // ~40% white: high-contrast selections & badges

// Backward-compatible alias mappings
val BgSurface = GlassThin
val BgSurfaceElevated = GlassRegular
val BgGlassSurface = GlassThin
val BgGlassSurfaceElevated = GlassRegular
val BgGlassCard = GlassThin
val BgGlassHover = GlassThick
val BgGlassDark = Color(0x18FFFFFF)

// Apple Specular Hairline Glass Borders
val BorderGlassSpecular = Color(0x85FFFFFF)   // ~52% white: bright top light catch reflection
val BorderGlassSubtle = Color(0x30FFFFFF)     // ~19% white: crisp glass boundary
val BorderSubtle = BorderGlassSubtle
val BorderActive = Color(0xFFFFFFFF)          // 100% pure white active highlight

// Dynamic Glass Specular Brushes (Bright, high-fidelity light-catching rims)
val GlassCardBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x85FFFFFF), // Bright top specular sheen catching ambient light
        Color(0x40FFFFFF), // Crisp mid glass border
        Color(0x20FFFFFF)  // Faint bottom edge
    )
)

val GlassCardFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x36FFFFFF), // Luminous frosted top light sheen
        Color(0x22FFFFFF)  // Translucent dark glass well
    )
)

val GlassElevatedBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xA5FFFFFF), // Radiant top specular rim
        Color(0x55FFFFFF), // Mid sheen
        Color(0x30FFFFFF)  // Bottom edge
    )
)

val GlassElevatedFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x4AFFFFFF), // Radiant frosted glass surface
        Color(0x30FFFFFF)  // Elevated translucent base
    )
)

// Apple VisionOS Frosted Modal Glass (Deep Translucent Obsidian Occlusion + Specular Sheen)
val GlassModalFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xF6202534), // Frosted elevated obsidian glass with top light sheen
        Color(0xFC161924)  // Deep obsidian base
    )
)

// Typography Hierarchy (Pure Monochrome Starkness & High Readability)
val TextPrimary = Color(0xFFFFFFFF)           // 100% Stark Pure White
val TextSecondary = Color(0xFFE2E8F0)         // Bright Frosted Silver White (Slate-200)
val TextTertiary = Color(0xFFCBD5E1)          // Clear Silver (Slate-300)
val TextDisabled = Color(0xFF94A3B8)          // Recessed (Slate-400)
val TextCyan = TextPrimary                    // Neutralized to pure white

// Minimalist Glass Status Tokens (Monochrome Opacity & Stark Contrast)
val StatusNormal = Color(0xFFFFFFFF)          // Stark White
val StatusMonitor = Color(0xFFF8FAFC)         // Refined Silver White (Slate-50)
val StatusCritical = Color(0xFFFFFFFF)        // High-Contrast Stark White
val StatusUnavailable = Color(0xFFCBD5E1)     // Frosted Silver Grey

// Subtle Frosted Glass Pill Backings
val StatusNormalBg = Color(0x30FFFFFF)        // Frosted Glass White
val StatusMonitorBg = Color(0x3DFFFFFF)       // Slightly Elevated Glass
val StatusCriticalBg = Color(0x52FFFFFF)      // Emphasized Contrast Glass
val StatusUnavailableBg = Color(0x20FFFFFF)   // Recessed Glass

// Alert Surface Accents (Smoky Obsidian Glass with Specular Edge)
val CriticalSurfaceGlow = Color(0x40FFFFFF)
val MonitorSurfaceGlow = Color(0x30FFFFFF)
val CyanGlow = Color(0x35FFFFFF)
