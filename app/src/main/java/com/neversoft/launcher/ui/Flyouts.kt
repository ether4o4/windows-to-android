package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.modifier.acrylic
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/** Quick Settings flyout (Win+A): toggles + brightness/volume sliders. */
@Composable
fun QuickSettingsFlyout(onDismiss: () -> Unit) {
    Scrim(onDismiss) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = NsDim.TaskbarHeight + 10.dp)
                .width(360.dp)
                .acrylic(tint = NsColor.AcrylicFlyout, radius = NsDim.RadiusOverlay)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(16.dp),
        ) {
            val toggles = listOf(
                Triple(Icons.Filled.Wifi, "Wi-Fi", true),
                Triple(Icons.Filled.Bluetooth, "Bluetooth", false),
                Triple(Icons.Filled.AirplanemodeActive, "Airplane", false),
                Triple(Icons.Filled.BatteryFull, "Battery saver", false),
                Triple(Icons.Filled.DoNotDisturbOn, "Focus", false),
                Triple(Icons.Filled.AccessibilityNew, "Accessibility", false),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                toggles.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { (icon, label, on) ->
                            QuickToggle(icon, label, on, Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            SliderRow(Icons.Filled.Brightness6, 0.7f)
            SliderRow(Icons.Filled.VolumeUp, 0.5f)
        }
    }
}

/** Notification Center (click clock): notifications stack + month calendar. */
@Composable
fun NotificationFlyout(onDismiss: () -> Unit) {
    Scrim(onDismiss) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = NsDim.TaskbarHeight + 10.dp)
                .width(360.dp)
                .acrylic(tint = NsColor.AcrylicFlyout, radius = NsDim.RadiusOverlay)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(16.dp),
        ) {
            Text("Notifications", color = NsColor.Text, fontSize = 15.sp)
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(NsColor.ControlActive)
                    .padding(22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No new notifications", color = NsColor.TextTertiary, fontSize = 13.sp)
            }
            Spacer(Modifier.height(18.dp))
            MonthCalendar()
        }
    }
}

@Composable
private fun Scrim(onDismiss: () -> Unit, content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Scrim)
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        content = content,
    )
}

@Composable
private fun QuickToggle(icon: ImageVector, label: String, initialOn: Boolean, modifier: Modifier = Modifier) {
    var on by remember { mutableStateOf(initialOn) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (on) NsColor.Accent else NsColor.ControlActive)
            .clickable { on = !on }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon, label,
            tint = if (on) NsColor.Text else NsColor.TextSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            color = if (on) NsColor.Text else NsColor.TextSecondary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SliderRow(icon: ImageVector, initial: Float) {
    var v by remember { mutableStateOf(initial) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = NsColor.TextSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Slider(
            value = v,
            onValueChange = { v = it },
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = NsColor.AccentLight,
                activeTrackColor = NsColor.Accent,
                inactiveTrackColor = NsColor.StrokeStrong,
            ),
        )
    }
}

@Composable
private fun MonthCalendar() {
    val today = remember { LocalDate.now() }
    val ym = remember { YearMonth.from(today) }
    val firstDow = ym.atDay(1).dayOfWeek.value % 7 // Monday=1..Sunday=7 -> Sunday=0
    val length = ym.lengthOfMonth()
    val header = remember { today.format(DateTimeFormatter.ofPattern("MMMM yyyy")) }

    val cells = buildList {
        repeat(firstDow) { add(0) }
        for (d in 1..length) add(d)
    }

    Column {
        Text(header, color = NsColor.Text, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(
                    it, color = NsColor.TextTertiary, fontSize = 11.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { d ->
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (d != 0) {
                            val isToday = d == today.dayOfMonth
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isToday) NsColor.Accent else androidx.compose.ui.graphics.Color.Transparent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(d.toString(), color = NsColor.Text, fontSize = 12.sp)
                            }
                        }
                    }
                }
                repeat(7 - week.size) { Box(Modifier.weight(1f)) {} }
            }
        }
    }
}
