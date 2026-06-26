package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Widgets
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.apps.AppEntry
import com.neversoft.launcher.ui.components.AppGlyph
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Bottom edge-anchored, centered acrylic taskbar (doctrine §1.4). */
@Composable
fun Taskbar(
    apps: List<AppEntry>,
    startActive: Boolean,
    quickActive: Boolean,
    notifActive: Boolean,
    onToggleStart: () -> Unit,
    onToggleQuick: () -> Unit,
    onToggleNotif: () -> Unit,
    onLaunch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NsDim.TaskbarHeight)
            .background(NsColor.AcrylicTaskbar),
    ) {
        // Top hairline divider.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(NsColor.Stroke),
        )

        // Far-left widgets entry (weather/news mini board).
        Text(
            text = "72°  Sunny",
            color = NsColor.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 6.dp)
                .clip(RoundedCornerShape(NsDim.RadiusControl))
                .clickable { /* Widgets board (Win+W) — backlog §2.1 step 8 */ }
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )

        // Centered cluster: Start, Search, Task view, Widgets, then running apps.
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            TaskbarButton(active = startActive, onClick = onToggleStart) {
                Icon(
                    Icons.Filled.GridView, "Start",
                    tint = NsColor.AccentLight, modifier = Modifier.size(NsDim.TaskbarGlyph),
                )
            }
            TaskbarButton(onClick = onToggleStart) {
                Icon(
                    Icons.Filled.Search, "Search",
                    tint = NsColor.Text, modifier = Modifier.size(NsDim.TaskbarGlyph),
                )
            }
            TaskbarButton(onClick = { /* Task view — backlog §2.1 step 8 */ }) {
                Icon(
                    Icons.Filled.ViewModule, "Task view",
                    tint = NsColor.Text, modifier = Modifier.size(NsDim.TaskbarGlyph),
                )
            }
            TaskbarButton(onClick = { /* Widgets — backlog §2.1 step 8 */ }) {
                Icon(
                    Icons.Filled.Widgets, "Widgets",
                    tint = NsColor.Text, modifier = Modifier.size(NsDim.TaskbarGlyph),
                )
            }
            apps.take(6).forEach { app ->
                TaskbarButton(onClick = { onLaunch(app.packageName) }) {
                    AppGlyph(app, size = NsDim.TaskbarGlyph)
                }
            }
        }

        // System tray (bottom-right): network/volume/battery → Quick Settings,
        // clock → Notification Center.
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            TrayCluster(active = quickActive, onClick = onToggleQuick)
            Clock(active = notifActive, onClick = onToggleNotif)
        }
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
                .size(NsDim.IconButton)
                .clip(RoundedCornerShape(NsDim.RadiusControl))
                .background(if (active) NsColor.ControlHover else androidx.compose.ui.graphics.Color.Transparent)
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
                    .background(NsColor.AccentLight),
            )
        }
    }
}

@Composable
private fun TrayCluster(active: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(if (active) NsColor.ControlHover else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Wifi, "Network", tint = NsColor.Text, modifier = Modifier.size(16.dp))
        Icon(Icons.Filled.VolumeUp, "Volume", tint = NsColor.Text, modifier = Modifier.size(16.dp))
        Icon(Icons.Filled.BatteryFull, "Battery", tint = NsColor.Text, modifier = Modifier.size(16.dp))
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
            .background(if (active) NsColor.ControlHover else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(time, color = NsColor.Text, fontSize = 12.sp, lineHeight = 14.sp)
        Text(date, color = NsColor.Text, fontSize = 12.sp, lineHeight = 14.sp)
    }
}
