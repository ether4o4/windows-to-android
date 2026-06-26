package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val GLYPH = 20.dp
private val BTN = 38.dp

/**
 * Bottom edge-anchored acrylic taskbar (doctrine §1.4). Three non-overlapping
 * zones: weather/widgets (left) · Start/Search/Task view (centered) ·
 * system tray + clock (right). Flexible spacers keep zones apart on any width.
 */
@Composable
fun Taskbar(
    startActive: Boolean,
    quickActive: Boolean,
    notifActive: Boolean,
    taskViewActive: Boolean,
    widgetsActive: Boolean,
    onToggleStart: () -> Unit,
    onToggleQuick: () -> Unit,
    onToggleNotif: () -> Unit,
    onToggleTaskView: () -> Unit,
    onToggleWidgets: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LauncherState.taskbarHeight)
            .background(NsColor.AcrylicTaskbar),
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(NsColor.Stroke),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left zone: weather / widgets entry.
            WeatherChip(active = widgetsActive, onClick = onToggleWidgets)

            Spacer(Modifier.weight(1f))

            // Center zone: core navigation.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TaskbarButton(active = startActive, onClick = onToggleStart) {
                    Icon(Icons.Filled.GridView, "Start", tint = LauncherState.accent, modifier = Modifier.size(GLYPH))
                }
                TaskbarButton(onClick = onToggleStart) {
                    Icon(Icons.Filled.Search, "Search", tint = NsColor.Text, modifier = Modifier.size(GLYPH))
                }
                TaskbarButton(active = taskViewActive, onClick = onToggleTaskView) {
                    Icon(Icons.Filled.ViewModule, "Task view", tint = NsColor.Text, modifier = Modifier.size(GLYPH))
                }
            }

            Spacer(Modifier.weight(1f))

            // Right zone: system tray + clock.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                TrayCluster(active = quickActive, onClick = onToggleQuick)
                Clock(active = notifActive, onClick = onToggleNotif)
            }
        }
    }
}

@Composable
private fun WeatherChip(active: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(if (active) NsColor.ControlHover else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(Icons.Filled.WbSunny, "Widgets", tint = NsColor.AccentLight, modifier = Modifier.size(15.dp))
        Text("72°", color = NsColor.TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun TaskbarButton(
    active: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(BTN)
                .clip(RoundedCornerShape(NsDim.RadiusControl))
                .background(if (active) NsColor.ControlHover else Color.Transparent)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { content() }

        if (active) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .width(16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(LauncherState.accent),
            )
        }
    }
}

@Composable
private fun TrayCluster(active: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(if (active) NsColor.ControlHover else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Wifi, "Network", tint = NsColor.Text, modifier = Modifier.size(15.dp))
        Icon(Icons.Filled.VolumeUp, "Volume", tint = NsColor.Text, modifier = Modifier.size(15.dp))
        Icon(Icons.Filled.BatteryFull, "Battery", tint = NsColor.Text, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun Clock(active: Boolean, onClick: () -> Unit) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(15_000)
        }
    }
    val time = remember(now.minute) { now.format(DateTimeFormatter.ofPattern("h:mm a")) }
    val date = remember(now.dayOfYear) { now.format(DateTimeFormatter.ofPattern("M/d/yyyy")) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(if (active) NsColor.ControlHover else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(time, color = NsColor.Text, fontSize = 11.sp, lineHeight = 13.sp)
        Text(date, color = NsColor.Text, fontSize = 11.sp, lineHeight = 13.sp)
    }
}
