package com.neversoft.launcher.ui

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.apps.AppEntry
import com.neversoft.launcher.ui.components.AppGlyph
import com.neversoft.launcher.ui.components.AppIconTile
import com.neversoft.launcher.ui.modifier.acrylic
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

private const val GRID_COLS = 4
private const val MOST_USED_CAP = 16 // 4 rows × 4 cols

/**
 * Windows-11-style Start menu (spec §8): Spotlight-backed search on top, a
 * "Most used" 4×4 app grid with a "See all" expansion, and a permanent bottom
 * cluster (Photos · Music · Files · Control Center). No category boxes.
 */
@Composable
fun StartMenu(
    apps: List<AppEntry>,
    onLaunch: (String) -> Unit,
    onCommandPrompt: () -> Unit,
    onOpenApp: (LauncherApp) -> Unit,
    onLock: () -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var seeAll by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val orderedApps = remember(apps) { orderByUsage(context, apps) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Scrim)
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = NsDim.TaskbarHeight + 10.dp)
                .widthIn(max = 640.dp)
                .fillMaxWidth(0.97f)
                .heightIn(max = 640.dp)
                .acrylic(tint = NsColor.AcrylicStart, radius = NsDim.RadiusOverlay)
                .pointerInput(Unit) { detectTapGestures { /* consume */ } }
                .padding(20.dp),
        ) {
            SearchField(query) { query = it }
            Spacer(Modifier.height(16.dp))

            if (query.isBlank()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Most used", color = NsColor.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (seeAll) "Show less ‹" else "See all ›",
                        color = NsColor.AccentLight,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(NsDim.RadiusControl))
                            .clickable { seeAll = !seeAll }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))

                // Built-in shell apps first, then most-used installed apps.
                val installedShown = if (seeAll) {
                    orderedApps
                } else {
                    orderedApps.take((MOST_USED_CAP - InLauncherApps.size).coerceAtLeast(4))
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_COLS),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(InLauncherApps) { cat ->
                        StartTile(label = cat.app.title, onClick = { onOpenApp(cat.app) }) {
                            AppIconTile(cat.icon, cat.color, 40.dp)
                        }
                    }
                    items(installedShown) { app ->
                        StartTile(label = app.label, onClick = { onLaunch(app.packageName) }) {
                            AppGlyph(app, size = 34.dp)
                        }
                    }
                }
            } else {
                SpotlightResults(
                    query = query,
                    apps = apps,
                    onResultOpened = onDismiss,
                    onOpenFiles = { onOpenApp(LauncherApp.FileExplorer) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = NsColor.Stroke)
            Spacer(Modifier.height(12.dp))

            // Permanent bottom cluster (spec §4.2): Photos · Music · Files · Control Center.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClusterSlot(Icons.Filled.Photo, "Photos", Color(0xFFD81B60)) { onOpenApp(LauncherApp.Photos) }
                    ClusterSlot(Icons.Filled.MusicNote, "Music", Color(0xFF8E24AA)) { onOpenApp(LauncherApp.MediaPlayer) }
                    ClusterSlot(Icons.Filled.FolderOpen, "Files", Color(0xFFFFB300)) { onOpenApp(LauncherApp.FileExplorer) }
                    ClusterSlot(Icons.Filled.Settings, "Control Center", Color(0xFF8A97A6)) { onOpenApp(LauncherApp.Settings) }
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NsDim.RadiusControl))
                        .clickable(onClick = onLock)
                        .padding(8.dp),
                ) {
                    Icon(Icons.Filled.PowerSettingsNew, "Lock", tint = NsColor.Text, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun StartTile(label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = NsColor.Text,
            fontSize = 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ClusterSlot(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NsColor.ControlActive)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = NsColor.Text, fontSize = 11.sp)
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(NsColor.Solid)
            .border(1.dp, NsColor.Stroke, RoundedCornerShape(NsDim.RadiusControl))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, null, tint = NsColor.TextTertiary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    "Search apps, settings, documents",
                    color = NsColor.TextTertiary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = NsColor.Text, fontSize = 13.sp),
                cursorBrush = SolidColor(NsColor.AccentLight),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Order installed apps by recent foreground time (needs Usage Access; granted
 * via Settings). Degrades gracefully to the given order when unavailable.
 */
private fun orderByUsage(context: Context, apps: List<AppEntry>): List<AppEntry> = try {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    if (usm == null) {
        apps
    } else {
        val end = System.currentTimeMillis()
        val begin = end - 7L * 24 * 60 * 60 * 1000
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, begin, end)
        val byPkg = HashMap<String, Long>()
        stats?.forEach { s ->
            byPkg[s.packageName] = (byPkg[s.packageName] ?: 0L) + s.totalTimeInForeground
        }
        if (byPkg.isEmpty()) apps
        else apps.sortedByDescending { byPkg[it.packageName] ?: 0L }
    }
} catch (_: Exception) {
    apps
}
