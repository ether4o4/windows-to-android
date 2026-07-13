package com.neversoft.launcher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A launcher theme tints the system surfaces — taskbar, Start menu, flyouts —
 * the same color, so switching it recolors the whole shell. Ported from the
 * MorsVitaEst launcher theme system (the Haze-based blur helpers were left out;
 * these are pure Compose brushes). "Glass" keeps NeverSoft 11's existing
 * frosted acrylic; every other theme is a color + translucent-white sheen so
 * the wallpaper still shows through.
 */
data class LauncherTheme(
    val id: String,
    val label: String,
    val panel: Color,
    val content: Color,
    val glass: Boolean,
) {
    /** True when this theme's surfaces are light (→ dark content), so the shell
     *  should flip to light mode for readable text. */
    val isLight: Boolean get() = content.luminanceApprox() < 0.5f
}

/** Cheap perceived-luminance of a color (no ColorSpace math needed here). */
private fun Color.luminanceApprox(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

val LauncherThemes: List<LauncherTheme> = listOf(
    LauncherTheme("glass", "Glass", Color.White.copy(alpha = 0.16f), Color.White, glass = true),
    LauncherTheme("macbook", "MacBook", Color(0xF2ECEEF1), Color(0xFF1D1D1F), glass = false),
    LauncherTheme("red", "Red", Color(0xF26E1A22), Color.White, glass = false),
    LauncherTheme("purple", "Purple", Color(0xF22E1A47), Color.White, glass = false),
    LauncherTheme("blue", "Blue", Color(0xF2143257), Color.White, glass = false),
    LauncherTheme("green", "Green", Color(0xF2123A28), Color.White, glass = false),
    LauncherTheme("black", "Black", Color(0xF20A0B0E), Color.White, glass = false),
    LauncherTheme("darkgrey", "Dark Grey", Color(0xF21E2228), Color.White, glass = false),
    LauncherTheme("lightgrey", "Light Grey", Color(0xF2C7CCD3), Color(0xFF14171C), glass = false),
    LauncherTheme("white", "White", Color(0xF2F0F2F6), Color(0xFF14171C), glass = false),
)

fun resolveLauncherTheme(id: String): LauncherTheme =
    LauncherThemes.firstOrNull { it.id == id } ?: LauncherThemes.first()

/**
 * Themed surface fill shared by taskbar, Start menu and flyouts. "Glass" stays a
 * clean translucent-white pane; every colored theme is a translucent-white sheen
 * easing into the theme color so the wallpaper still shows through.
 */
fun LauncherTheme.surfaceBrush(): Brush = if (glass) {
    Brush.verticalGradient(
        listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.10f)),
    )
} else {
    Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.20f),
            panel.copy(alpha = 0.82f),
            panel.copy(alpha = 0.92f),
        ),
    )
}
