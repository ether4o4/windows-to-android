package com.neversoft.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Windows 11 Fluent design tokens — dark theme primary (doctrine §1.1–1.3).
 * Single source of truth; every surface pulls colour/geometry from here.
 */
object NsColor {
    // Accent.
    val Accent = Color(0xFF0078D4) // canonical Windows blue
    val AccentLight = Color(0xFF4CC2FF) // Fluent dark accent variant
    val AccentLight2 = Color(0xFF60CDFF)

    // Surfaces.
    val Mica = Color(0xFF202020)
    val Layer = Color(0xFF2B2B2B)
    val Solid = Color(0xFF1C1C1C)

    // Text (Fluent alpha ramp).
    val Text = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xC7FFFFFF) // 78%
    val TextTertiary = Color(0x8CFFFFFF) // 55%

    // Strokes / dividers.
    val Stroke = Color(0x17FFFFFF) // ~9%
    val StrokeStrong = Color(0x29FFFFFF) // ~16%

    // Control states.
    val ControlHover = Color(0x17FFFFFF)
    val ControlActive = Color(0x0FFFFFFF)
    val ControlSelected = Color(0x1FFFFFFF)

    // Acrylic tints over the wallpaper (depth tiers, doctrine luxury §4.3).
    val AcrylicTaskbar = Color(0xCC2A2A2A) // thin
    val AcrylicFlyout = Color(0xE6242426) // thick
    val AcrylicStart = Color(0xF02A2A2C) // thickest

    // Scrim behind overlays.
    val Scrim = Color(0x33000000)

    // Campbell terminal scheme (doctrine §1.5).
    val TermBg = Color(0xFF0C0C0C)
    val TermFg = Color(0xFFCCCCCC)

    val Danger = Color(0xFFF1707A)
}

/** Geometry tokens (doctrine §1.2). */
object NsDim {
    val TaskbarHeight = 48.dp
    val TaskbarHeightSmall = 32.dp

    val RadiusOverlay = 8.dp // windows / flyouts
    val RadiusControl = 4.dp // buttons / controls

    val IconButton = 40.dp
    val TaskbarGlyph = 22.dp
    val DesktopIcon = 44.dp
    val StartTile = 64.dp
}
