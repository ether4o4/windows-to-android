package com.neversoft.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * NeverSoft (Fluent dark) Compose theme. Wraps Material3 so default Text/Icon
 * colours follow the token palette, but components style themselves from NsColor.
 */
@Composable
fun NeverSoftTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
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
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content,
    )
}
