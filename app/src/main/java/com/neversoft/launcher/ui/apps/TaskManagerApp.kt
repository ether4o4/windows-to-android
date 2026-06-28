package com.neversoft.launcher.ui.apps

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import kotlin.math.abs
import kotlin.math.sin

private data class ProcessRow(
    val label: String,
    val packageName: String,
    val cpu: Float,
    val memoryMb: Int,
)

private fun loadProcesses(ctx: android.content.Context): List<ProcessRow> = runCatching {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolved = pm.queryIntentActivities(intent, 0)
    resolved.mapNotNull { ri ->
        val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
        val label = runCatching { ri.loadLabel(pm).toString() }.getOrDefault(pkg)
        val h = abs(pkg.hashCode())
        // Deterministic, plausible mock metrics derived from the package name.
        val cpu = (h % 250) / 10f // 0.0 .. 24.9 %
        val mem = 40 + (h % 920) // 40 .. 959 MB
        ProcessRow(label = label, packageName = pkg, cpu = cpu, memoryMb = mem)
    }.sortedByDescending { it.cpu }
}.getOrDefault(emptyList())

@Composable
fun TaskManagerApp() {
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    val processes = remember { loadProcesses(context) }

    Column(
        Modifier
            .fillMaxSize()
            .background(NsColor.Solid)
    ) {
        // Chip tabs.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChipTab("Processes", Icons.Filled.Settings, tab == 0) { tab = 0 }
            ChipTab("Performance", Icons.Filled.Speed, tab == 1) { tab = 1 }
        }

        Box(Modifier.fillMaxSize()) {
            if (tab == 0) {
                ProcessesTab(processes) {
                    runCatching {
                        Toast.makeText(context, "End task: $it", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                PerformanceTab()
            }
        }
    }
}

@Composable
private fun ChipTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) LauncherState.accent else NsColor.ControlActive
    val fg = if (selected) Color.White else NsColor.Text
    Row(
        Modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.width(16.dp).height(16.dp))
        Text(label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProcessesTab(rows: List<ProcessRow>, onEndTask: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        // Header row with column labels.
        Row(
            Modifier
                .fillMaxWidth()
                .background(NsColor.Layer)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Name",
                color = NsColor.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            HeaderCell("CPU", 64.dp)
            HeaderCell("Memory", 84.dp)
            Spacer(Modifier.width(80.dp))
        }

        if (rows.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No running processes found", color = NsColor.TextTertiary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(rows) { row ->
                    ProcessRowItem(row, onEndTask)
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, w: androidx.compose.ui.unit.Dp) {
    Text(
        text,
        color = NsColor.TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.width(w),
    )
}

@Composable
private fun ProcessRowItem(row: ProcessRow, onEndTask: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            row.label,
            color = NsColor.Text,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            String.format("%.1f%%", row.cpu),
            color = NsColor.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(64.dp),
        )
        Text(
            "${row.memoryMb} MB",
            color = NsColor.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(84.dp),
        )
        Box(
            Modifier
                .width(80.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                "End task",
                color = NsColor.Danger,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .clickable { onEndTask(row.label) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun PerformanceTab() {
    // Subtle animated tick to drive the bars.
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(900)
            tick++
        }
    }

    // Deterministic base values that gently oscillate around a center.
    val cpu = oscillate(38f, tick, 0)
    val mem = oscillate(61f, tick, 1)
    val disk = oscillate(12f, tick, 2)
    val net = oscillate(27f, tick, 3)

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PerfCard("CPU", cpu, "%", Modifier.weight(1f).fillMaxHeight())
            PerfCard("Memory", mem, "%", Modifier.weight(1f).fillMaxHeight())
        }
        Row(
            Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PerfCard("Disk", disk, "%", Modifier.weight(1f).fillMaxHeight())
            PerfCard("Network", net, "%", Modifier.weight(1f).fillMaxHeight())
        }
    }
}

private fun oscillate(center: Float, tick: Int, seed: Int): Float {
    val v = center + (sin((tick + seed * 2) * 0.7f) * 6f)
    return v.coerceIn(2f, 99f)
}

@Composable
private fun PerfCard(title: String, value: Float, unit: String, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(targetValue = value / 100f, label = "perf-$title")
    Column(
        modifier
            .clip(RoundedCornerShape(NsDim.RadiusOverlay))
            .background(NsColor.ControlActive)
            .padding(16.dp),
    ) {
        Text(title, color = NsColor.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(
            String.format("%.0f%s", value, unit),
            color = NsColor.Text,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.weight(1f))
        // Simple bar — Box width fraction, accent colour.
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(NsDim.RadiusControl))
                .background(NsColor.StrokeStrong),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .background(LauncherState.accent),
            )
        }
    }
}
