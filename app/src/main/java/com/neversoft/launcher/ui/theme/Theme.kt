package com.neversoft.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * NeverSoft (Fluent) Compose theme. Reads [NsColor.isDark] so a theme toggle
 * recomposes the whole tree; components style themselves from NsColor tokens.
 */
@Composable
fun NeverSoftTheme(content: @Composable () -> Unit) {
    val colors = if (NsColor.isDark) {
        darkColorScheme(
            primary = NsColor.Accent,
            onPrimary = NsColor.Text,
            secondary = NsColor.AccentLight,
            background = NsColor.Solid,
            onBackground = NsColor.Text,
            surface = NsColor.Layer,
            onSurface = NsColor.Text,
            surfaceVariant = NsColor.Mica,
            onSurfaceVariant = NsColor.TextSecondary,
            outline = NsColor.StrokeStrong,
            error = NsColor.Danger,
        )
    } else {
        lightColorScheme(
            primary = NsColor.Accent,
            onPrimary = NsColor.Text,
            secondary = NsColor.AccentLight,
            background = NsColor.Solid,
            onBackground = NsColor.Text,
            surface = NsColor.Layer,
            onSurface = NsColor.Text,
            surfaceVariant = NsColor.Mica,
            onSurfaceVariant = NsColor.TextSecondary,
            outline = NsColor.StrokeStrong,
            error = NsColor.Danger,
        )
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content,
    )
}
