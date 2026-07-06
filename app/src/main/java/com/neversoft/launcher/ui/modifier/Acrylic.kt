package com.neversoft.launcher.ui.modifier

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neversoft.launcher.ui.theme.NsColor

/**
 * Fluent acrylic surface (doctrine §1.2). Over the gradient wallpaper a
 * translucent tint + hairline stroke already reads as frosted glass; for a
 * photo wallpaper, enable true backdrop blur on-device (see [blurLayer] and the
 * README note — stock Compose has no first-class backdrop blur).
 */
fun Modifier.acrylic(
    tint: Color = NsColor.AcrylicFlyout,
    radius: Dp = 8.dp,
    stroke: Boolean = true,
): Modifier {
    var m: Modifier = this
        .clip(RoundedCornerShape(radius))
        .background(tint)
    if (stroke) {
        m = m.border(1.dp, NsColor.Stroke, RoundedCornerShape(radius))
    }
    return m
}

/**
 * Real Gaussian blur of a composable's own pixels via RenderEffect (Android 12+,
 * doctrine §0). No-op below API 31. Use to blur a wallpaper copy drawn inside a
 * panel to achieve genuine acrylic over photo wallpapers.
 */
fun Modifier.blurLayer(radius: Dp): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && radius.value > 0f) {
        this.graphicsLayer {
            val px = radius.toPx()
            renderEffect = RenderEffect
                .createBlurEffect(px, px, Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
        }
    } else {
        this
    }
