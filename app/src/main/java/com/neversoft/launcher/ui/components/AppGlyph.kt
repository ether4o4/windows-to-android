package com.neversoft.launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.apps.AppEntry
import com.neversoft.launcher.ui.theme.NsColor

/** Renders an app's real launcher icon, or a coloured initial fallback. */
@Composable
fun AppGlyph(app: AppEntry, size: Dp) {
    val icon = app.icon
    if (icon != null) {
        Image(bitmap = icon, contentDescription = app.label, modifier = Modifier.size(size))
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(4.dp))
                .background(NsColor.Accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = app.label.take(1).uppercase(),
                color = NsColor.Text,
                fontSize = 12.sp,
            )
        }
    }
}
