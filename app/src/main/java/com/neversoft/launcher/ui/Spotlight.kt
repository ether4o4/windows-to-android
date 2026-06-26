package com.neversoft.launcher.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.apps.AppEntry
import com.neversoft.launcher.ui.components.AppGlyph
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

/**
 * Spotlight-style device search (reconstructed from the Spotlight app's brief:
 * "search your entire device with filtered selections"). Drives the Start-menu
 * search bar: filter chips (All / Apps / Settings / Web / Files) over apps,
 * system settings, a web query, and the file explorer.
 *
 * NOTE: this is an original implementation matching that description — the
 * actual Spotlight repo could not be read from this session to clone verbatim.
 */
enum class SpotFilter(val label: String) {
    All("All"), Apps("Apps"), Settings("Settings"), Files("Files"), Web("Web")
}

private class SettingEntry(
    val title: String,
    val keywords: String,
    val icon: ImageVector,
    val onOpen: () -> Unit,
)

fun webSearch(context: Context, query: String) {
    val url = "https://www.google.com/search?q=" + Uri.encode(query)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show() }
}

@Composable
fun SpotlightResults(
    query: String,
    apps: List<AppEntry>,
    onLaunchApp: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onCommandPrompt: () -> Unit,
    onOpenFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(SpotFilter.All) }
    val q = query.trim()

    val appResults = if (q.isBlank()) emptyList()
    else apps.filter { it.label.contains(q, ignoreCase = true) }

    val settingItems = listOf(
        SettingEntry("Settings", "settings system options", Icons.Filled.Settings, onOpenSettings),
        SettingEntry("About this PC", "about device version edition", Icons.Filled.Info, onOpenSettings),
        SettingEntry("Personalization", "wallpaper accent theme background taskbar color", Icons.Filled.Palette, onOpenSettings),
        SettingEntry("Command Prompt", "terminal cmd shell console", Icons.Filled.Terminal, onCommandPrompt),
        SettingEntry("File Explorer", "files folders storage ghost key", Icons.Filled.FolderOpen, onOpenFiles),
    )
    val settingResults = if (q.isBlank()) emptyList()
    else settingItems.filter {
        it.title.contains(q, ignoreCase = true) || it.keywords.contains(q, ignoreCase = true)
    }

    Column(modifier = modifier) {
        // Filter chips.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SpotFilter.entries.forEach { f ->
                FilterChip(label = f.label, selected = filter == f) { filter = f }
            }
        }
        Spacer(Modifier.height(12.dp))

        val showApps = filter == SpotFilter.All || filter == SpotFilter.Apps
        val showSettings = filter == SpotFilter.All || filter == SpotFilter.Settings
        val showFiles = filter == SpotFilter.All || filter == SpotFilter.Files
        val showWeb = filter == SpotFilter.All || filter == SpotFilter.Web

        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (showApps && appResults.isNotEmpty()) {
                item { SectionHeader("Apps") }
                items(appResults.size) { i ->
                    val app = appResults[i]
                    ResultRow(title = app.label, subtitle = "App", onClick = { onLaunchApp(app.packageName) }) {
                        AppGlyph(app, size = 28.dp)
                    }
                }
            }
            if (showSettings && settingResults.isNotEmpty()) {
                item { SectionHeader("Settings & actions") }
                items(settingResults.size) { i ->
                    val s = settingResults[i]
                    ResultRow(title = s.title, subtitle = "System", onClick = s.onOpen) {
                        Icon(s.icon, null, tint = NsColor.AccentLight, modifier = Modifier.size(24.dp))
                    }
                }
            }
            if (showFiles && q.isNotBlank()) {
                item { SectionHeader("Files") }
                item {
                    ResultRow(
                        title = "Search files for \"$q\"",
                        subtitle = "Open File Explorer",
                        onClick = onOpenFiles,
                    ) { Icon(Icons.Filled.FolderOpen, null, tint = NsColor.AccentLight, modifier = Modifier.size(24.dp)) }
                }
            }
            if (showWeb && q.isNotBlank()) {
                item { SectionHeader("Web") }
                item {
                    ResultRow(
                        title = "Search the web for \"$q\"",
                        subtitle = "Open in browser",
                        onClick = { webSearch(context, q) },
                    ) { Icon(Icons.Filled.Public, null, tint = NsColor.AccentLight, modifier = Modifier.size(24.dp)) }
                }
            }

            val nothing = appResults.isEmpty() && settingResults.isEmpty() &&
                (filter == SpotFilter.Apps || filter == SpotFilter.Settings)
            if (nothing) {
                item {
                    Text(
                        "No matches for \"$q\"",
                        color = NsColor.TextTertiary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) LauncherState.accent else NsColor.ControlActive)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            label,
            color = if (selected) NsColor.Text else NsColor.TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        color = NsColor.TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 4.dp),
    )
}

@Composable
private fun ResultRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) { leading() }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = NsColor.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = NsColor.TextTertiary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
