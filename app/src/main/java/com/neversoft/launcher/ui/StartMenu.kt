package com.neversoft.launcher.ui

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.Brand
import com.neversoft.launcher.apps.AppEntry
import com.neversoft.launcher.ui.components.AppGlyph
import com.neversoft.launcher.ui.modifier.acrylic
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

/** Centered acrylic Start menu above the taskbar (doctrine §1.4). */
@Composable
fun StartMenu(
    apps: List<AppEntry>,
    onLaunch: (String) -> Unit,
    onCommandPrompt: () -> Unit,
    onOpenApp: (LauncherApp) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }

    // Full-screen scrim; tapping outside the panel dismisses.
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
                .heightIn(max = 620.dp)
                .acrylic(tint = NsColor.AcrylicStart, radius = NsDim.RadiusOverlay)
                .pointerInput(Unit) { detectTapGestures { /* consume taps on panel */ } }
                .padding(20.dp),
        ) {
            SearchField(query) { query = it }
            Spacer(Modifier.height(16.dp))

            if (query.isBlank()) {
                Text("Pinned", color = NsColor.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item {
                        StartTile(label = "Command Prompt", onClick = onCommandPrompt) {
                            Icon(Icons.Filled.Terminal, null, tint = LauncherState.accent, modifier = Modifier.size(30.dp))
                        }
                    }
                    items(InLauncherApps) { cat ->
                        StartTile(label = cat.app.title, onClick = { onOpenApp(cat.app) }) {
                            Icon(cat.icon, null, tint = NsColor.AccentLight, modifier = Modifier.size(30.dp))
                        }
                    }
                    items(apps) { app ->
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

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountCircle, null, tint = NsColor.Text, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text(Brand.USER, color = NsColor.Text, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NsDim.RadiusControl))
                        .clickable(onClick = onDismiss)
                        .padding(8.dp),
                ) {
                    Icon(Icons.Filled.PowerSettingsNew, "Power", tint = NsColor.Text, modifier = Modifier.size(22.dp))
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
                    "Search for apps, settings, and documents",
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
