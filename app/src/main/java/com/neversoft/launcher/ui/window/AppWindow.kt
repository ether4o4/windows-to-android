package com.neversoft.launcher.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import kotlin.math.roundToInt

private enum class SnapZone { None, Max, Left, Right, Top, Bottom, TL, TR, BL, BR }

private class Geom(val w: Dp, val h: Dp, val x: Dp, val y: Dp)

private fun geomFor(zone: SnapZone, w: Dp, h: Dp): Geom? = when (zone) {
    SnapZone.None -> null
    SnapZone.Max -> Geom(w, h, 0.dp, 0.dp)
    SnapZone.Left -> Geom(w / 2f, h, 0.dp, 0.dp)
    SnapZone.Right -> Geom(w / 2f, h, w / 2f, 0.dp)
    SnapZone.Top -> Geom(w, h / 2f, 0.dp, 0.dp)
    SnapZone.Bottom -> Geom(w, h / 2f, 0.dp, h / 2f)
    SnapZone.TL -> Geom(w / 2f, h / 2f, 0.dp, 0.dp)
    SnapZone.TR -> Geom(w / 2f, h / 2f, w / 2f, 0.dp)
    SnapZone.BL -> Geom(w / 2f, h / 2f, 0.dp, h / 2f)
    SnapZone.BR -> Geom(w / 2f, h / 2f, w / 2f, h / 2f)
}

/**
 * In-launcher window with a drawn Windows-style title bar (drag, minimize,
 * maximize, close) plus Snap Layouts: long-press maximize for the snap grid;
 * tap maximize to toggle full. Dragging the title bar un-snaps to floating.
 */
@Composable
fun AppWindow(
    title: String,
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    var snap by remember { mutableStateOf(SnapZone.None) }
    var collapsed by remember { mutableStateOf(false) }
    var showSnap by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxW = maxWidth
        val maxH = maxHeight
        val defaultW = if (maxW < 400.dp) maxW * 0.96f else 380.dp
        val defaultH = if (maxH < 520.dp) maxH * 0.82f else 460.dp
        val geom = geomFor(snap, maxW, maxH)

        val winW = geom?.w ?: defaultW
        val bodyH = geom?.h ?: defaultH
        val align = if (geom == null) Alignment.Center else Alignment.TopStart

        Column(
            modifier = Modifier
                .align(align)
                .then(
                    if (geom != null) Modifier.offset(geom.x, geom.y)
                    else Modifier.offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) },
                )
                .width(winW)
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
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { snap = SnapZone.None },
                        ) { _, drag ->
                            offsetX += drag.x
                            offsetY += drag.y
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(12.dp))
                Text(title, color = NsColor.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                CaptionButton(Icons.Filled.Remove, "Minimize") { collapsed = !collapsed }
                // Maximize: tap toggles full, long-press opens Snap Layouts.
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 34.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { snap = if (snap == SnapZone.Max) SnapZone.None else SnapZone.Max },
                                onLongPress = { showSnap = true },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.CropSquare, "Maximize", tint = NsColor.TextSecondary, modifier = Modifier.size(14.dp))
                }
                CaptionButton(Icons.Filled.Close, "Close", danger = true, onClick = onClose)
            }

            if (showSnap) {
                SnapLayoutPicker(
                    onPick = { snap = it; showSnap = false; offsetX = 0f; offsetY = 0f },
                    onDismiss = { showSnap = false },
                )
            }

            if (!collapsed) {
                Box(Modifier.height(bodyH)) { content() }
            }
        }
    }
}

@Composable
private fun SnapLayoutPicker(onPick: (SnapZone) -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NsColor.Mica)
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Snap layout", color = NsColor.TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Text(
                "Float",
                color = NsColor.AccentLight,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .clickable { onPick(SnapZone.None) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SnapCell(SnapZone.Left, onPick)
            SnapCell(SnapZone.Right, onPick)
            SnapCell(SnapZone.Top, onPick)
            SnapCell(SnapZone.Bottom, onPick)
            SnapCell(SnapZone.Max, onPick)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SnapCell(SnapZone.TL, onPick)
            SnapCell(SnapZone.TR, onPick)
            SnapCell(SnapZone.BL, onPick)
            SnapCell(SnapZone.BR, onPick)
        }
    }
}

@Composable
private fun SnapCell(zone: SnapZone, onPick: (SnapZone) -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 30.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(NsColor.ControlActive)
            .border(1.dp, NsColor.Stroke, RoundedCornerShape(3.dp))
            .clickable { onPick(zone) }
            .padding(3.dp),
    ) {
        val fill = when (zone) {
            SnapZone.Left -> Modifier.fillMaxHeight().fillMaxWidth(0.5f).align(Alignment.CenterStart)
            SnapZone.Right -> Modifier.fillMaxHeight().fillMaxWidth(0.5f).align(Alignment.CenterEnd)
            SnapZone.Top -> Modifier.fillMaxWidth().fillMaxHeight(0.5f).align(Alignment.TopCenter)
            SnapZone.Bottom -> Modifier.fillMaxWidth().fillMaxHeight(0.5f).align(Alignment.BottomCenter)
            SnapZone.TL -> Modifier.fillMaxWidth(0.5f).fillMaxHeight(0.5f).align(Alignment.TopStart)
            SnapZone.TR -> Modifier.fillMaxWidth(0.5f).fillMaxHeight(0.5f).align(Alignment.TopEnd)
            SnapZone.BL -> Modifier.fillMaxWidth(0.5f).fillMaxHeight(0.5f).align(Alignment.BottomStart)
            SnapZone.BR -> Modifier.fillMaxWidth(0.5f).fillMaxHeight(0.5f).align(Alignment.BottomEnd)
            else -> Modifier.fillMaxSize()
        }
        Box(fill.clip(RoundedCornerShape(2.dp)).background(LauncherState.accent))
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
