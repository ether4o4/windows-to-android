package com.neversoft.launcher.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Windows 11 Fluent design tokens. A single observable [isDark] flag flips the
 * whole palette: every token is a computed getter that reads [isDark], so all
 * existing `NsColor.X` reads recompose automatically on theme change — no
 * call-site churn, no CompositionLocal plumbing.
 */
object NsColor {
    var isDark by mutableStateOf(true)

    private fun pick(dark: Color, light: Color): Color = if (isDark) dark else light

    // Accent (canonical Windows blue is constant; the lighter tint adapts).
    val Accent: Color get() = Color(0xFF0078D4)
    val AccentLight: Color get() = pick(Color(0xFF4CC2FF), Color(0xFF005FB8))
    val AccentLight2: Color get() = pick(Color(0xFF60CDFF), Color(0xFF0067C0))

    // Surfaces.
    val Mica: Color get() = pick(Color(0xFF202020), Color(0xFFF3F3F3))
    val Layer: Color get() = pick(Color(0xFF2B2B2B), Color(0xFFEDEDED))
    val Solid: Color get() = pick(Color(0xFF1C1C1C), Color(0xFFF9F9F9))

    // Text (Fluent alpha ramp).
    val Text: Color get() = pick(Color(0xFFFFFFFF), Color(0xFF1B1B1B))
    val TextSecondary: Color get() = pick(Color(0xC7FFFFFF), Color(0xC7000000))
    val TextTertiary: Color get() = pick(Color(0x8CFFFFFF), Color(0x8C000000))

    // Strokes / dividers.
    val Stroke: Color get() = pick(Color(0x17FFFFFF), Color(0x14000000))
    val StrokeStrong: Color get() = pick(Color(0x29FFFFFF), Color(0x24000000))

    // Control states.
    val ControlHover: Color get() = pick(Color(0x17FFFFFF), Color(0x0F000000))
    val ControlActive: Color get() = pick(Color(0x0FFFFFFF), Color(0x0A000000))
    val ControlSelected: Color get() = pick(Color(0x1FFFFFFF), Color(0x18000000))

    // Acrylic tints over the wallpaper (depth tiers).
    val AcrylicTaskbar: Color get() = pick(Color(0xCC2A2A2A), Color(0xCCF3F3F3))
    val AcrylicFlyout: Color get() = pick(Color(0xE6242426), Color(0xF2FBFBFB))
    val AcrylicStart: Color get() = pick(Color(0xF02A2A2C), Color(0xF5FAFAFA))

    // Scrim behind overlays.
    val Scrim: Color get() = pick(Color(0x33000000), Color(0x26000000))

    // Campbell terminal scheme (constant — terminal stays dark).
    val TermBg: Color get() = Color(0xFF0C0C0C)
    val TermFg: Color get() = Color(0xFFCCCCCC)

    val Danger: Color get() = pick(Color(0xFFF1707A), Color(0xFFC42B1C))
}

/** Geometry tokens (theme-independent). */
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
