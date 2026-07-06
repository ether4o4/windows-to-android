package com.neversoft.launcher.ui.apps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewList
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
 * Ghost Key File Explorer, matching the Ghost-key-file-explorer spec (read from
 * its public README): dual-pane manager with list/grid views and Windows-style
 * move/copy between panes; auto-tags (who/what/when/where), per-file SKU, global
 * timeline, AES-GCM vault types (Standard/Forensic/Ephemeral), Limbo sandbox.
 * Reimplemented in Compose (the original is React/TS); vault crypto + sandbox
 * execution are on-device refinements.
 */
private enum class GkTab(val label: String) {
    Files("Files"), Timeline("Timeline"), Vault("Vault"), Limbo("Limbo")
}

private class PaneState(start: File) {
    var current by mutableStateOf(start)
    var selected by mutableStateOf<File?>(null)
    var rev by mutableIntStateOf(0) // bump to force a re-list after writes
    fun refresh() { rev++ }
}

@Composable
fun FileExplorerApp() {
    val context = LocalContext.current
    val root = remember { Environment.getExternalStorageDirectory() ?: context.filesDir }
    var tab by remember { mutableStateOf(GkTab.Files) }
    var dual by remember { mutableStateOf(false) }
    var grid by remember { mutableStateOf(false) }
    var accessTick by remember { mutableIntStateOf(0) }

    val hasAccess = remember(accessTick) { hasStorageAccess(context) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { accessTick++ }

    val left = remember { PaneState(root) }
    val right = remember { PaneState(root) }
    var activeIdx by remember { mutableIntStateOf(0) }
    val active = if (activeIdx == 0) left else right
    val other = if (activeIdx == 0) right else left

    Column(Modifier.fillMaxSize().background(NsColor.Solid)) {
        GkTopBar(
            grid = grid, dual = dual, hasAccess = hasAccess,
            onToggleGrid = { grid = !grid },
            onToggleDual = { dual = !dual; if (!dual) activeIdx = 0 },
            onGrant = { requestStorageAccess(context, permLauncher) },
        )
        GkTabs(tab) { tab = it }

        Box(Modifier.weight(1f)) {
            when (tab) {
                GkTab.Files -> {
                    if (dual) {
                        Row(Modifier.fillMaxSize()) {
                            PaneView(left, grid, active = activeIdx == 0, dual = true, root = root,
                                onActivate = { activeIdx = 0 }, onGrant = { requestStorageAccess(context, permLauncher) },
                                modifier = Modifier.weight(1f))
                            Box(Modifier.width(1.dp).fillMaxHeight().background(NsColor.StrokeStrong))
                            PaneView(right, grid, active = activeIdx == 1, dual = true, root = root,
                                onActivate = { activeIdx = 1 }, onGrant = { requestStorageAccess(context, permLauncher) },
                                modifier = Modifier.weight(1f))
                        }
                    } else {
                        PaneView(left, grid, active = true, dual = false, root = root,
                            onActivate = {}, onGrant = { requestStorageAccess(context, permLauncher) },
                            modifier = Modifier.fillMaxSize())
                    }
                }
                GkTab.Timeline -> GkTimeline(active.current)
                GkTab.Vault -> GkVault()
                GkTab.Limbo -> GkPlaceholder(
                    Icons.Filled.Science, "Limbo sandbox",
                    "Inspect untrusted files in isolation before opening them. (Sandboxed execution is a redma refinement.)",
                )
            }
        }

        if (tab == GkTab.Files) {
            active.selected?.let { sel ->
                SelectionPanel(
                    file = sel,
                    dual = dual,
                    onClose = { active.selected = null },
                    onNewFolder = {
                        newFolder(active.current)?.let { Toast.makeText(context, "Created ${it.name}", Toast.LENGTH_SHORT).show() }
                            ?: Toast.makeText(context, "Couldn't create folder", Toast.LENGTH_SHORT).show()
                        active.refresh()
                    },
                    onDelete = {
                        val ok = runCatching { sel.deleteRecursively() }.getOrDefault(false)
                        Toast.makeText(context, if (ok) "Deleted ${sel.name}" else "Delete failed", Toast.LENGTH_SHORT).show()
                        active.selected = null; active.refresh()
                    },
                    onMove = {
                        val ok = moveInto(sel, other.current)
                        Toast.makeText(context, if (ok) "Moved to ${other.current.name}" else "Move failed", Toast.LENGTH_SHORT).show()
                        active.selected = null; active.refresh(); other.refresh()
                    },
                    onCopy = {
                        val ok = copyInto(sel, other.current)
                        Toast.makeText(context, if (ok) "Copied to ${other.current.name}" else "Copy failed", Toast.LENGTH_SHORT).show()
                        other.refresh()
                    },
                )
            }
        }
    }
}

// ---------------- top bar / tabs ----------------

@Composable
private fun GkTopBar(
    grid: Boolean, dual: Boolean, hasAccess: Boolean,
    onToggleGrid: () -> Unit, onToggleDual: () -> Unit, onGrant: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(NsColor.Mica).padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ToggleButton(if (grid) Icons.Filled.ViewList else Icons.Filled.GridView, "View", onToggleGrid)
            Spacer(Modifier.width(4.dp))
            ToggleButton(Icons.Filled.ViewColumn, "Dual pane", onToggleDual, on = dual)
            Spacer(Modifier.weight(1f))
            if (!hasAccess) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NsDim.RadiusControl))
                        .background(LauncherState.accent)
                        .clickable(onClick = onGrant)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Lock, null, tint = NsColor.Text, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Grant access", color = NsColor.Text, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ToggleButton(icon: ImageVector, desc: String, onClick: () -> Unit, on: Boolean = false) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(if (on) LauncherState.accent else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, desc, tint = NsColor.Text, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun GkTabs(selected: GkTab, onSelect: (GkTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(NsColor.Mica).padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GkTab.entries.forEach { t ->
            val sel = t == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (sel) LauncherState.accent else NsColor.ControlActive)
                    .clickable { onSelect(t) }
                    .padding(horizontal = 13.dp, vertical = 6.dp),
            ) {
                Text(t.label, color = if (sel) NsColor.Text else NsColor.TextSecondary, fontSize = 12.sp,
                    fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal)
            }
        }
    }
}

// ---------------- pane ----------------

@Composable
private fun PaneView(
    pane: PaneState,
    grid: Boolean,
    active: Boolean,
    dual: Boolean,
    root: File,
    onActivate: () -> Unit,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = remember(pane.current, pane.rev) {
        (pane.current.listFiles()?.toList() ?: emptyList())
            .sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
    }
    val border = if (dual && active) Modifier.border(1.dp, LauncherState.accent) else Modifier

    Column(
        modifier = modifier
            .then(border)
            .clickable(onClick = onActivate),
    ) {
        // Pane breadcrumb.
        Row(Modifier.fillMaxWidth().padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            PaneIcon(Icons.Filled.Home, "Home") { pane.current = root; pane.selected = null; onActivate() }
            PaneIcon(Icons.Filled.ArrowUpward, "Up", enabled = pane.current != root) {
                pane.current = pane.current.parentFile ?: root; pane.selected = null; onActivate()
            }
            Spacer(Modifier.width(6.dp))
            Text(pane.current.name.ifBlank { "/" }, color = NsColor.TextSecondary, fontSize = 11.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        if (entries.isEmpty()) {
            EmptyState("Empty", "Grant access", onGrant)
            return@Column
        }

        if (grid) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(if (dual) 72.dp else 88.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            ) {
                items(entries.size) { i ->
                    val f = entries[i]
                    FileCell(f, selected = pane.selected == f) { onClickEntry(pane, f, onActivate) }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 6.dp)) {
                items(entries.size) { i ->
                    val f = entries[i]
                    FileRow(f, compact = dual, selected = pane.selected == f) { onClickEntry(pane, f, onActivate) }
                }
            }
        }
    }
}

private fun onClickEntry(pane: PaneState, f: File, onActivate: () -> Unit) {
    onActivate()
    if (f.isDirectory) { pane.current = f; pane.selected = null } else { pane.selected = f }
}

@Composable
private fun PaneIcon(icon: ImageVector, desc: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(NsDim.RadiusControl))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, desc, tint = if (enabled) NsColor.Text else NsColor.TextTertiary, modifier = Modifier.size(16.dp)) }
}

@Composable
private fun FileRow(f: File, compact: Boolean, selected: Boolean, onClick: () -> Unit) {
    val kind = fileKind(f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(if (selected) NsColor.ControlSelected else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(kindIcon(kind), kind, tint = NsColor.AccentLight, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(f.name, color = NsColor.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!compact) {
                Text(
                    if (f.isDirectory) "$kind · ${f.listFilesCountSafe()} items" else "$kind · ${humanSize(f.length())}",
                    color = NsColor.TextTertiary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!compact) Text(shortDate(f.lastModified()), color = NsColor.TextTertiary, fontSize = 11.sp)
    }
}

@Composable
private fun FileCell(f: File, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(if (selected) NsColor.ControlSelected else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(kindIcon(fileKind(f)), null, tint = NsColor.AccentLight, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(6.dp))
        Text(f.name, color = NsColor.Text, fontSize = 11.sp, maxLines = 2, textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis)
    }
}

// ---------------- selection action panel (auto-tags + ops) ----------------

@Composable
private fun SelectionPanel(
    file: File,
    dual: Boolean,
    onClose: () -> Unit,
    onNewFolder: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onCopy: () -> Unit,
) {
    var confirmDelete by remember(file) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().background(NsColor.Mica).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(kindIcon(fileKind(file)), null, tint = NsColor.AccentLight, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(file.name, color = NsColor.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Box(Modifier.size(26.dp).clip(RoundedCornerShape(NsDim.RadiusControl)).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Close, "Close", tint = NsColor.TextSecondary, modifier = Modifier.size(15.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        // Auto-tags (who/what/when/where) + SKU.
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TagChip("who · ${Brand.USER}"); TagChip("what · ${fileKind(file)}")
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TagChip("when · ${shortDate(file.lastModified())}"); TagChip("where · ${file.parentFile?.name ?: "root"}")
        }
        Spacer(Modifier.height(6.dp))
        Text("SKU  ${skuFor(file)}", color = NsColor.TextTertiary, fontSize = 11.sp)
        Spacer(Modifier.height(10.dp))
        // Operations.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(Icons.Filled.CreateNewFolder, "New folder", onNewFolder)
            if (dual) {
                ActionButton(Icons.Filled.DriveFileMove, "Move →", onMove)
                ActionButton(Icons.Filled.ContentCopy, "Copy →", onCopy)
            }
            if (confirmDelete) {
                ActionButton(Icons.Filled.DeleteOutline, "Confirm?", onDelete, danger = true)
            } else {
                ActionButton(Icons.Filled.DeleteOutline, "Delete", { confirmDelete = true }, danger = true)
            }
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit, danger: Boolean = false) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(NsColor.ControlActive)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (danger) NsColor.Danger else NsColor.Text, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = if (danger) NsColor.Danger else NsColor.Text, fontSize = 11.sp)
    }
}

// ---------------- timeline ----------------

@Composable
private fun GkTimeline(dir: File) {
    val files = remember(dir) {
        (dir.listFiles()?.filter { it.isFile } ?: emptyList()).sortedByDescending { it.lastModified() }
    }
    if (files.isEmpty()) { EmptyState("No files on the timeline", null) {}; return }
    val grouped = remember(files) { files.groupBy { dayBucket(it.lastModified()) } }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp)) {
        grouped.forEach { (bucket, list) ->
            item {
                Text(bucket, color = NsColor.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 4.dp))
            }
            items(list.size) { i -> FileRow(list[i], compact = false, selected = false) {} }
        }
    }
}

// ---------------- vault ----------------

@Composable
private fun GkVault() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Secure Vault", color = NsColor.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text("AES-GCM encrypted containers, local-first. (Encryption is a redma refinement.)",
            color = NsColor.TextTertiary, fontSize = 12.sp)
        VaultCard(Icons.Filled.Lock, "Standard", "Everyday encrypted storage for sensitive files.")
        VaultCard(Icons.Filled.Fingerprint, "Forensic", "Tamper-evident container with access logging.")
        VaultCard(Icons.Filled.Timer, "Ephemeral", "Auto-wipes contents after a set time or on close.")
    }
}

@Composable
private fun VaultCard(icon: ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(NsDim.RadiusOverlay)).background(NsColor.ControlActive).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = NsColor.AccentLight, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = NsColor.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(body, color = NsColor.TextTertiary, fontSize = 11.sp)
        }
        Icon(Icons.Filled.Lock, null, tint = NsColor.TextTertiary, modifier = Modifier.size(16.dp))
    }
}

// ---------------- shared ----------------

@Composable
private fun GkPlaceholder(icon: ImageVector, title: String, body: String) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = NsColor.AccentLight, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = NsColor.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = NsColor.TextTertiary, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun EmptyState(text: String, actionLabel: String?, onAction: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.Folder, null, tint = NsColor.TextTertiary, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        Text(text, color = NsColor.TextTertiary, fontSize = 12.sp, textAlign = TextAlign.Center)
        if (actionLabel != null) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(NsDim.RadiusControl)).background(LauncherState.accent)
                    .clickable(onClick = onAction).padding(horizontal = 14.dp, vertical = 8.dp),
            ) { Text(actionLabel, color = NsColor.Text, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(NsColor.ControlActive).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text, color = NsColor.TextSecondary, fontSize = 11.sp)
    }
}

// ---------------- helpers ----------------

private fun hasStorageAccess(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

private fun requestStorageAccess(context: Context, permLauncher: ActivityResultLauncher<String>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }.onFailure {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    } else {
        permLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

private fun newFolder(dir: File): File? {
    var name = "New folder"
    var n = 2
    var target = File(dir, name)
    while (target.exists()) { name = "New folder ($n)"; target = File(dir, name); n++ }
    return runCatching { if (target.mkdirs()) target else null }.getOrNull()
}

private fun moveInto(src: File, destDir: File): Boolean = runCatching {
    val dest = File(destDir, src.name)
    if (dest.absolutePath == src.absolutePath) return false
    if (src.renameTo(dest)) return true
    if (src.copyRecursively(dest, overwrite = false)) src.deleteRecursively() else false
}.getOrDefault(false)

private fun copyInto(src: File, destDir: File): Boolean = runCatching {
    val dest = File(destDir, src.name)
    if (dest.absolutePath == src.absolutePath) return false
    src.copyRecursively(dest, overwrite = false)
}.getOrDefault(false)

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
    val day = 24L * 60 * 60 * 1000
    val diff = System.currentTimeMillis() - millis
    return when {
        diff < day -> "Today"
        diff < 2 * day -> "Yesterday"
        diff < 7 * day -> "This week"
        diff < 30 * day -> "This month"
        else -> "Earlier"
    }
}

private fun skuFor(f: File): String =
    "GK-" + abs(f.absolutePath.hashCode()).toString(16).uppercase().padStart(6, '0').take(6)
