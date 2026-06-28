package com.neversoft.launcher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class ClockTab(val label: String) {
    World("World Clock"),
    Stopwatch("Stopwatch"),
    Timer("Timer"),
}

private data class City(val name: String, val zone: String)

private val WORLD_CITIES = listOf(
    City("New York", "America/New_York"),
    City("London", "Europe/London"),
    City("Tokyo", "Asia/Tokyo"),
    City("Sydney", "Australia/Sydney"),
)

private val TIME_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm:ss a", Locale.US)
private val CITY_TIME_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private val DATE_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)

/** mm:ss.cs from a millisecond duration (centiseconds, two digits). */
private fun formatStopwatch(millis: Long): String {
    val totalCs = millis / 10
    val cs = totalCs % 100
    val totalSec = totalCs / 100
    val sec = totalSec % 60
    val min = totalSec / 60
    return "%02d:%02d.%02d".format(min, sec, cs)
}

/** mm:ss from a whole-second count. */
private fun formatTimer(totalSeconds: Int): String {
    val s = if (totalSeconds < 0) 0 else totalSeconds
    val min = s / 60
    val sec = s % 60
    return "%02d:%02d".format(min, sec)
}

private fun cityOffsetLabel(now: ZonedDateTime, local: ZonedDateTime): String {
    return runCatching {
        val cityOffset = now.offset.totalSeconds
        val localOffset = local.offset.totalSeconds
        val diffMin = (cityOffset - localOffset) / 60
        val sign = if (diffMin >= 0) "+" else "-"
        val absMin = kotlin.math.abs(diffMin)
        val h = absMin / 60
        val m = absMin % 60
        if (m == 0) "${sign}${h}h" else "${sign}${h}h ${m}m"
    }.getOrDefault("")
}

@Composable
fun ClockApp() {
    var tab by remember { mutableStateOf(ClockTab.World) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Solid)
            .padding(16.dp)
    ) {
        // Chip-row tab switch.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClockTab.values().forEach { t ->
                TabChip(
                    label = t.label,
                    selected = tab == t,
                    modifier = Modifier.weight(1f),
                    onClick = { tab = t }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        when (tab) {
            ClockTab.World -> WorldClockSection()
            ClockTab.Stopwatch -> StopwatchSection()
            ClockTab.Timer -> TimerSection()
        }
    }
}

@Composable
private fun TabChip(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val accent = LauncherState.accent
    val bg: Color = if (selected) accent else NsColor.ControlActive
    val fg: Color = if (selected) NsColor.Text else NsColor.TextSecondary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun WorldClockSection() {
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }

    val localZoned = remember(now) {
        runCatching { ZonedDateTime.now(ZoneId.systemDefault()) }
            .getOrDefault(now.atZone(ZoneId.systemDefault()))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Big live local time.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NsDim.RadiusOverlay))
                .background(NsColor.ControlActive)
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = runCatching { now.format(TIME_FMT) }.getOrDefault("--:--:--"),
                    color = NsColor.Text,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Light
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = runCatching { now.format(DATE_FMT) }.getOrDefault(""),
                    color = NsColor.TextSecondary,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Cities",
            color = NsColor.TextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        WORLD_CITIES.forEach { city ->
            CityRow(city = city, local = localZoned)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CityRow(city: City, local: ZonedDateTime) {
    val cityNow = remember(city.zone, local) {
        runCatching { ZonedDateTime.now(ZoneId.of(city.zone)) }.getOrNull()
    }
    val timeText = remember(cityNow) {
        runCatching { cityNow?.format(CITY_TIME_FMT) ?: "--:--" }.getOrDefault("--:--")
    }
    val offsetText = remember(cityNow) {
        if (cityNow != null) cityOffsetLabel(cityNow, local) else ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusOverlay))
            .background(NsColor.ControlActive)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = city.name, color = NsColor.Text, fontSize = 16.sp)
            if (offsetText.isNotEmpty()) {
                Text(text = offsetText, color = NsColor.TextTertiary, fontSize = 12.sp)
            }
        }
        Text(
            text = timeText,
            color = NsColor.TextSecondary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
private fun StopwatchSection() {
    var running by remember { mutableStateOf(false) }
    // Accumulated elapsed time across pauses, plus the start anchor.
    var elapsed by remember { mutableStateOf(0L) }
    var startAt by remember { mutableStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(running) {
        if (running) {
            while (true) {
                elapsed = (System.currentTimeMillis() - startAt)
                delay(31)
            }
        }
    }

    val accent = LauncherState.accent

    Column(modifier = Modifier.fillMaxSize()) {
        // Big elapsed time.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NsDim.RadiusOverlay))
                .background(NsColor.ControlActive)
                .padding(vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatStopwatch(elapsed),
                color = NsColor.Text,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ControlButton(
                label = if (running) "Stop" else "Start",
                icon = if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                background = if (running) NsColor.ControlSelected else accent,
                contentColor = NsColor.Text,
                modifier = Modifier.weight(1f)
            ) {
                if (running) {
                    running = false
                } else {
                    startAt = System.currentTimeMillis() - elapsed
                    running = true
                }
            }
            ControlButton(
                label = "Lap",
                icon = Icons.Filled.Add,
                background = NsColor.ControlSelected,
                contentColor = NsColor.Text,
                modifier = Modifier.weight(1f)
            ) {
                if (running) laps.add(0, elapsed)
            }
            ControlButton(
                label = "Reset",
                icon = Icons.Filled.Refresh,
                background = NsColor.ControlSelected,
                contentColor = NsColor.Text,
                modifier = Modifier.weight(1f)
            ) {
                running = false
                elapsed = 0L
                startAt = 0L
                laps.clear()
            }
        }

        Spacer(Modifier.height(20.dp))

        if (laps.isNotEmpty()) {
            Text(
                text = "Laps",
                color = NsColor.TextTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(laps.size) { i ->
                    val lapTime = laps[i]
                    // laps are newest-first (added at index 0); number them descending.
                    val index = laps.size - i
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NsDim.RadiusControl))
                            .background(NsColor.ControlActive)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lap $index",
                            color = NsColor.TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatStopwatch(lapTime),
                            color = NsColor.Text,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

private val TIMER_PRESETS = listOf(30, 60, 300, 600)

@Composable
private fun TimerSection() {
    // Configured duration (seconds) and the live remaining count.
    var configured by remember { mutableIntStateOf(60) }
    var remaining by remember { mutableIntStateOf(60) }
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        if (running) {
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
            }
            if (remaining <= 0) {
                running = false
                finished = true
            }
        }
    }

    val accent = LauncherState.accent
    val idle = !running

    Column(modifier = Modifier.fillMaxSize()) {
        // Big countdown.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NsDim.RadiusOverlay))
                .background(NsColor.ControlActive)
                .padding(vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTimer(remaining),
                    color = if (finished) accent else NsColor.Text,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light
                )
                if (finished) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Time's up",
                        color = accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // +/- adjusters (only meaningful when idle).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdjustButton(icon = Icons.Filled.Remove, modifier = Modifier.weight(1f)) {
                if (idle) {
                    configured = (configured - 30).coerceAtLeast(10)
                    remaining = configured
                    finished = false
                }
            }
            Box(
                modifier = Modifier.weight(1.4f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Set: ${formatTimer(configured)}",
                    color = NsColor.TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            AdjustButton(icon = Icons.Filled.Add, modifier = Modifier.weight(1f)) {
                if (idle) {
                    configured = (configured + 30).coerceAtMost(60 * 99)
                    remaining = configured
                    finished = false
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Presets.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TIMER_PRESETS.forEach { secs ->
                val selected = idle && configured == secs
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(NsDim.RadiusControl))
                        .background(if (selected) accent.copy(alpha = 0.30f) else NsColor.ControlActive)
                        .clickable {
                            if (idle) {
                                configured = secs
                                remaining = secs
                                finished = false
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatTimer(secs),
                        color = NsColor.Text,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ControlButton(
                label = if (running) "Stop" else "Start",
                icon = if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                background = if (running) NsColor.ControlSelected else accent,
                contentColor = NsColor.Text,
                modifier = Modifier.weight(1f)
            ) {
                if (running) {
                    running = false
                } else {
                    if (remaining <= 0) remaining = configured
                    finished = false
                    if (remaining > 0) running = true
                }
            }
            ControlButton(
                label = "Reset",
                icon = Icons.Filled.Refresh,
                background = NsColor.ControlSelected,
                contentColor = NsColor.Text,
                modifier = Modifier.weight(1f)
            ) {
                running = false
                finished = false
                remaining = configured
            }
        }
    }
}

@Composable
private fun ControlButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    contentColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(background)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(0.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun AdjustButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(NsColor.ControlSelected)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NsColor.Text,
            modifier = Modifier.size(20.dp)
        )
    }
}
