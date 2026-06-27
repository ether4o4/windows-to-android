package com.neversoft.launcher.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.neversoft.launcher.apps.AppEntry
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Spotlight-style device search UI, matching the Spotlight app: a search bar
 * with filter chips All / Media / Files / Hidden (Media reveals secondary
 * Photos / Videos / Audio), aggregating apps, contacts, media, and files with
 * ~220ms debounce and relevance ranking. Reimplemented in Compose to match the
 * Spotlight repo's documented behaviour.
 */
@Composable
fun SpotlightResults(
    query: String,
    apps: List<AppEntry>,
    onResultOpened: () -> Unit,
    onOpenFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(SpotFilter.All) }
    var sub by remember { mutableStateOf<MediaSub?>(null) }
    var results by remember { mutableStateOf(emptyList<SpotItem>()) }
    var loading by remember { mutableStateOf(false) }
    var permTick by remember { mutableIntStateOf(0) }

    val missing = remember(permTick) { missingPermissions(context) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permTick++ }

    // Debounced aggregated search on a background thread.
    LaunchedEffect(query, filter, sub, permTick) {
        val q = query.trim()
        if (q.isBlank()) {
            results = emptyList(); loading = false
            return@LaunchedEffect
        }
        loading = true
        delay(220)
        results = withContext(Dispatchers.IO) { runSpotlight(context, q, apps, filter, sub) }
        loading = false
    }

    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SpotFilter.entries.forEach { f ->
                Chip(f.label, selected = filter == f) {
                    filter = f
                    if (f != SpotFilter.Media) sub = null
                }
            }
        }
        if (filter == SpotFilter.Media) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("All", selected = sub == null) { sub = null }
                MediaSub.entries.forEach { m -> Chip(m.label, selected = sub == m) { sub = m } }
            }
        }
        if (missing.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            GrantRow { permLauncher.launch(missing.toTypedArray()) }
        }
        Spacer(Modifier.height(12.dp))

        Box(Modifier.weight(1f)) {
            when {
                loading && results.isEmpty() ->
                    CenterNote("Searching…")
                results.isEmpty() ->
                    EmptyResults(query, filter, onOpenFiles)
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(results.size) { i ->
                        val item = results[i]
                        ResultRow(item, onClick = { item.onOpen(context); onResultOpened() })
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(item: SpotItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
            val icon = item.appIcon
            if (icon != null) {
                Image(bitmap = icon, contentDescription = item.title, modifier = Modifier.size(28.dp))
            } else {
                Icon(kindIcon(item.kind), null, tint = NsColor.AccentLight, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = NsColor.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle, color = NsColor.TextTertiary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun GrantRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(LauncherState.accent)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Lock, null, tint = NsColor.Text, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("Grant access to search contacts, media & files", color = NsColor.Text, fontSize = 12.sp)
    }
}

@Composable
private fun CenterNote(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = NsColor.TextTertiary, fontSize = 13.sp)
    }
}

@Composable
private fun EmptyResults(query: String, filter: SpotFilter, onOpenFiles: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("No ${filter.label.lowercase()} results for \"$query\"", color = NsColor.TextTertiary, fontSize = 13.sp)
        if (filter == SpotFilter.Files || filter == SpotFilter.Hidden) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .background(LauncherState.accent)
                    .clickable(onClick = onOpenFiles)
                    .padding(horizontal = 16.dp, vertical = 9.dp),
            ) { Text("Open File Explorer", color = NsColor.Text, fontSize = 13.sp) }
        }
    }
}

private fun kindIcon(kind: SpotKind): ImageVector = when (kind) {
    SpotKind.App -> Icons.Filled.InsertDriveFile
    SpotKind.Contact -> Icons.Filled.Person
    SpotKind.Photo -> Icons.Filled.Photo
    SpotKind.Video -> Icons.Filled.Movie
    SpotKind.Audio -> Icons.Filled.MusicNote
    SpotKind.FileItem -> Icons.Filled.FolderOpen
}

private fun runtimePerms(): List<String> = buildList {
    add(Manifest.permission.READ_CONTACTS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.READ_MEDIA_IMAGES)
        add(Manifest.permission.READ_MEDIA_VIDEO)
        add(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

private fun missingPermissions(context: Context): List<String> =
    runtimePerms().filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }
