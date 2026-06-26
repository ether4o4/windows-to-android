package com.neversoft.launcher.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import kotlin.math.roundToInt

/**
 * An in-launcher window with a drawn Windows-style title bar (drag, minimize,
 * maximize, close) — the "windowed app" feel for our own apps (doctrine §2.1
 * step 6 / §3 Tier-B chrome, minus the GPLv3 terminal embed).
 */
@Composable
fun AppWindow(
    title: String,
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var maximized by remember { mutableStateOf(false) }
    var collapsed by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val winWidth = if (maximized) maxWidth * 0.98f else 380.dp
        val bodyHeight = if (maximized) maxHeight * 0.82f else 460.dp

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { if (maximized) IntOffset(0, 0) else IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .width(winWidth)
                .clip(RoundedCornerShape(NsDim.RadiusOverlay))
                .background(NsColor.Solid)
                .border(1.dp, NsColor.StrokeStrong, RoundedCornerShape(NsDim.RadiusOverlay)),
        ) {
            // Title bar.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(NsColor.Mica)
                    .pointerInput(maximized) {
                        if (!maximized) {
                            detectDragGestures { _, drag ->
                                offsetX += drag.x
                                offsetY += drag.y
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(12.dp))
                Text(title, color = NsColor.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                CaptionButton(Icons.Filled.Remove, "Minimize") { collapsed = !collapsed }
                CaptionButton(Icons.Filled.CropSquare, "Maximize") { maximized = !maximized }
                CaptionButton(Icons.Filled.Close, "Close", danger = true, onClick = onClose)
            }

            if (!collapsed) {
                Box(Modifier.height(bodyHeight)) { content() }
            }
        }
    }
}

@Composable
private fun CaptionButton(
    icon: ImageVector,
    description: String,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 34.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (danger) NsColor.Danger else NsColor.TextSecondary,
            modifier = Modifier.size(14.dp),
        )
    }
}
