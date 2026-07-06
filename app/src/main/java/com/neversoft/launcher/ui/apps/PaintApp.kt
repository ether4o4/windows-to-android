package com.neversoft.launcher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import androidx.compose.foundation.Canvas as DrawCanvas

/** A single freehand stroke: a list of points plus its colour and width. */
private class PaintStroke(
    val points: MutableList<Offset>,
    val color: Color,
    val width: Float,
)

@Composable
fun PaintApp() {
    val strokes = remember { mutableStateListOf<PaintStroke>() }
    var current by remember { mutableStateOf<PaintStroke?>(null) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableFloatStateOf(8f) }

    val palette = listOf(
        LauncherState.accent,
        Color.Black,
        Color.White,
        Color(0xFFE53935),
        Color(0xFF43A047),
        Color(0xFF1E88E5),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Solid),
    ) {
        // Toolbar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NsColor.Mica)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Colour swatches.
            palette.forEach { swatch ->
                val isSelected = swatch == selectedColor
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NsColor.Text else NsColor.StrokeStrong,
                            shape = CircleShape,
                        )
                        .clickable { selectedColor = swatch },
                )
            }

            Spacer(Modifier.width(8.dp))

            // Stroke-width slider.
            Text(
                text = "Size",
                color = NsColor.TextSecondary,
                fontSize = 12.sp,
            )
            Slider(
                value = strokeWidth,
                onValueChange = { strokeWidth = it },
                valueRange = 1f..40f,
                colors = SliderDefaults.colors(
                    thumbColor = LauncherState.accent,
                    activeTrackColor = LauncherState.accent,
                    inactiveTrackColor = NsColor.StrokeStrong,
                ),
                modifier = Modifier.width(140.dp),
            )

            Spacer(Modifier.width(8.dp))

            // Undo.
            Button(
                onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) },
                shape = RoundedCornerShape(NsDim.RadiusControl),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NsColor.ControlActive,
                    contentColor = NsColor.Text,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Undo",
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("Undo", fontSize = 12.sp)
            }

            // Clear.
            Button(
                onClick = {
                    strokes.clear()
                    current = null
                },
                shape = RoundedCornerShape(NsDim.RadiusControl),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NsColor.ControlActive,
                    contentColor = NsColor.Text,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("Clear", fontSize = 12.sp)
            }
        }

        // Drawing canvas.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(NsDim.RadiusOverlay))
                .background(Color.White),
        ) {
            DrawCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                current = PaintStroke(
                                    points = mutableStateListOf(offset),
                                    color = selectedColor,
                                    width = strokeWidth,
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val stroke = current
                                if (stroke != null) {
                                    stroke.points.add(change.position)
                                    // Trigger recomposition by reassigning the holder.
                                    current = stroke
                                }
                            },
                            onDragEnd = {
                                current?.let { strokes.add(it) }
                                current = null
                            },
                            onDragCancel = {
                                current?.let { strokes.add(it) }
                                current = null
                            },
                        )
                    },
            ) {
                fun drawStroke(stroke: PaintStroke) {
                    val pts = stroke.points
                    if (pts.size == 1) {
                        // A single tap: draw a dot.
                        drawLine(
                            color = stroke.color,
                            start = pts[0],
                            end = pts[0],
                            strokeWidth = stroke.width,
                            cap = StrokeCap.Round,
                        )
                    } else {
                        for (i in 0 until pts.size - 1) {
                            drawLine(
                                color = stroke.color,
                                start = pts[i],
                                end = pts[i + 1],
                                strokeWidth = stroke.width,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }

                strokes.forEach { drawStroke(it) }
                current?.let { drawStroke(it) }
            }
        }
    }
}
