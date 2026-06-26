package com.neversoft.launcher.ui.apps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.neversoft.launcher.Brand
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Ghost Key File Explorer (reconstructed from the Ghost-key-file-explorer brief:
 * auto-tags who/what/when/where, global timeline, secure Vault, Limbo sandbox,
 * local-first), restyled to the NeverSoft / Win11 aesthetic.
 *
 * NOTE: original implementation matching that description — the actual repo
 * could not be read from this session to clone verbatim. Full-device browsing
 * needs all-files access (device-gated); the app's own storage always works.
 */
private enum class GkTab(val label: String) {
    Files("Files"), Timeline("Timeline"), Vault("Vault"), Limbo("Limbo")
}

@Composable
fun FileExplorerApp() {
    val context = LocalContext.current
    val root = remember { Environment.getExternalStorageDirectory() ?: context.filesDir }
    var current by remember { mutableStateOf(root) }
    var tab by remember { mutableStateOf(GkTab.Files) }
    var selected by remember { mutableStateOf<File?>(null) }
    var accessTick by remember { mutableIntStateOf(0) }

    val hasAccess = remember(accessTick) { hasStorageAccess(context) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { accessTick++ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Solid),
    ) {
        GkHeader(
            current = current,
            atRoot = current == root,
            hasAccess = hasAccess,
            onUp = { current = current.parentFile ?: root; selected = null },
            onHome = { current = root; selected = null },
            onGrant = { requestStorageAccess(context, permLauncher) },
        )
        GkTabs(tab) { tab = it; selected = null }

        Box(Modifier.weight(1f)) {
            when (tab) {
                GkTab.Files -> GkFiles(
                    current = current,
                    hasAccess = hasAccess,
                    onOpenDir = { current = it; selected = null },
                    onSelect = { selected = it },
                    onGrant = { requestStorageAccess(context, permLauncher) },
                )
                GkTab.Timeline -> GkTimeline(current = current)
                GkTab.Vault -> GkPlaceholder(
                    icon = Icons.Filled.Lock,
                    title = "Secure Vault",
                    body = "Local-first private vault. Files you move here stay encrypted on-device. (Vault encryption is a redma refinement.)",
                )
                GkTab.Limbo -> GkPlaceholder(
                    icon = Icons.Filled.Science,
                    title = "Limbo sandbox",
                    body = "Inspect untrusted files in isolation before opening them. (Sandboxed execution is a redma refinement.)",
                )
            }
        }

        selected?.let { f ->
            if (tab == GkTab.Files) GkDetail(f, onClose = { selected = null })
        }
    }
}

// ---------------- header / tabs ----------------

@Composable
private fun GkHeader(
    current: File,
    atRoot: Boolean,
    hasAccess: Boolean,
    onUp: () -> Unit,
    onHome: () -> Unit,
    onGrant: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(NsColor.Mica).padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderButton(Icons.Filled.Home, "Home", onHome)
            Spacer(Modifier.width(4.dp))
            HeaderButton(Icons.Filled.ArrowUpward, "Up", onUp, enabled = !atRoot)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .background(NsColor.Solid)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    text = winPath(current),
                    color = NsColor.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!hasAccess) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .background(LauncherState.accent)
                    .clickable(onClick = onGrant)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Lock, null, tint = NsColor.Text, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Grant all-files access to browse your device", color = NsColor.Text, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun HeaderButton(icon: ImageVector, desc: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon, desc,
            tint = if (enabled) NsColor.Text else NsColor.TextTertiary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun GkTabs(selected: GkTab, onSelect: (GkTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NsColor.Mica)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GkTab.entries.forEach { t ->
            val isSel = t == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSel) LauncherState.accent else NsColor.ControlActive)
                    .clickable { onSelect(t) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    t.label,
                    color = if (isSel) NsColor.Text else NsColor.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal,
                )
            }
        }
    }
}

// ---------------- files ----------------

@Composable
private fun GkFiles(
    current: File,
    hasAccess: Boolean,
    onOpenDir: (File) -> Unit,
    onSelect: (File) -> Unit,
    onGrant: () -> Unit,
) {
    val entries = remember(current, hasAccess) {
        (current.listFiles()?.toList() ?: emptyList())
            .sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
    }
    if (entries.isEmpty()) {
        EmptyState(
            text = if (hasAccess) "This folder is empty" else "No access — grant all-files access to browse",
            actionLabel = if (hasAccess) null else "Grant access",
            onAction = onGrant,
        )
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp)) {
        items(entries.size) { i ->
            val f = entries[i]
            FileRow(f, onClick = { if (f.isDirectory) onOpenDir(f) else onSelect(f) })
        }
    }
}

@Composable
private fun FileRow(f: File, onClick: () -> Unit) {
    val kind = fileKind(f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(kindIcon(kind), kind, tint = NsColor.AccentLight, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(f.name, color = NsColor.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (f.isDirectory) "$kind · ${f.listFilesCountSafe()} items" else "$kind · ${humanSize(f.length())}",
                color = NsColor.TextTertiary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Text(shortDate(f.lastModified()), color = NsColor.TextTertiary, fontSize = 11.sp)
    }
}

// ---------------- timeline (global, by day) ----------------

@Composable
private fun GkTimeline(current: File) {
    val files = remember(current) {
        (current.listFiles()?.filter { it.isFile } ?: emptyList())
            .sortedByDescending { it.lastModified() }
    }
    if (files.isEmpty()) {
        EmptyState("No files to show on the timeline", null) {}
        return
    }
    val grouped = remember(files) { files.groupBy { dayBucket(it.lastModified()) } }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp)) {
        grouped.forEach { (bucket, list) ->
            item {
                Text(
                    bucket,
                    color = NsColor.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 4.dp),
                )
            }
            items(list.size) { i ->
                val f = list[i]
                FileRow(f, onClick = {})
            }
        }
    }
}

// ---------------- detail (auto-tags who/what/when/where) ----------------

@Composable
private fun GkDetail(f: File, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NsColor.Mica)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(kindIcon(fileKind(f)), null, tint = NsColor.AccentLight, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(f.name, color = NsColor.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(NsDim.RadiusControl)).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Close, "Close", tint = NsColor.TextSecondary, modifier = Modifier.size(16.dp)) }
        }
        Spacer(Modifier.height(10.dp))
        Text("Auto-tags", color = NsColor.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TagChip("who · ${Brand.USER}")
            TagChip("what · ${fileKind(f)}")
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TagChip("when · ${shortDate(f.lastModified())}")
            TagChip("where · ${f.parentFile?.name ?: "root"}")
        }
        Spacer(Modifier.height(10.dp))
        Text("SKU  ${skuFor(f)}", color = NsColor.TextTertiary, fontSize = 11.sp)
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NsColor.ControlActive)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, color = NsColor.TextSecondary, fontSize = 11.sp)
    }
}

// ---------------- shared bits ----------------

@Composable
private fun GkPlaceholder(icon: ImageVector, title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = NsColor.AccentLight, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = NsColor.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = NsColor.TextTertiary, fontSize = 12.sp)
    }
}

@Composable
private fun EmptyState(text: String, actionLabel: String?, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Folder, null, tint = NsColor.TextTertiary, modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(10.dp))
        Text(text, color = NsColor.TextTertiary, fontSize = 13.sp)
        if (actionLabel != null) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .background(LauncherState.accent)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 16.dp, vertical = 9.dp),
            ) { Text(actionLabel, color = NsColor.Text, fontSize = 13.sp) }
        }
    }
}

// ---------------- helpers (pure) ----------------

private fun hasStorageAccess(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

private fun requestStorageAccess(
    context: Context,
    permLauncher: ActivityResultLauncher<String>,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
    } else {
        permLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

private fun fileKind(f: File): String = when {
    f.isDirectory -> "Folder"
    else -> when (f.extension.lowercase(Locale.getDefault())) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic" -> "Image"
        "mp4", "mkv", "mov", "avi", "webm", "3gp" -> "Video"
        "mp3", "wav", "flac", "ogg", "m4a", "aac" -> "Audio"
        "pdf", "doc", "docx", "txt", "md", "rtf", "odt" -> "Document"
        "zip", "rar", "7z", "tar", "gz" -> "Archive"
        "apk" -> "App"
        else -> "File"
    }
}

private fun kindIcon(kind: String): ImageVector = when (kind) {
    "Folder" -> Icons.Filled.Folder
    "Image" -> Icons.Filled.Image
    "Video" -> Icons.Filled.Movie
    "Audio" -> Icons.Filled.MusicNote
    "Document" -> Icons.Filled.Description
    "Archive" -> Icons.Filled.Archive
    "App" -> Icons.Filled.Android
    else -> Icons.Filled.InsertDriveFile
}

private fun File.listFilesCountSafe(): Int = this.listFiles()?.size ?: 0

private fun humanSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024L * 1024 -> "%.0f KB".format(bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / 1024.0 / 1024)
    else -> "%.1f GB".format(bytes / 1024.0 / 1024 / 1024)
}

private fun shortDate(millis: Long): String =
    SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(millis))

private fun dayBucket(millis: Long): String {
    val now = System.currentTimeMillis()
    val day = 24L * 60 * 60 * 1000
    val diff = now - millis
    return when {
        diff < day -> "Today"
        diff < 2 * day -> "Yesterday"
        diff < 7 * day -> "This week"
        diff < 30 * day -> "This month"
        else -> "Earlier"
    }
}

/** Stable per-path SKU (Ghost Key links related data via SKUs). */
private fun skuFor(f: File): String =
    "GK-" + abs(f.absolutePath.hashCode()).toString(16).uppercase().padStart(6, '0').take(6)

/** Map a real path to a Windows-style display path. */
private fun winPath(f: File): String {
    val ext = Environment.getExternalStorageDirectory()?.absolutePath
    val p = f.absolutePath
    val mapped = if (ext != null && p.startsWith(ext)) "C:\\Users\\${Brand.USER}" + p.removePrefix(ext) else p
    return mapped.replace('/', '\\')
}
