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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Windows 11 File Explorer, rebuilt in Compose from the ForTheWin layout
 * (nav sidebar · command bar · address/breadcrumb bar · Quick access grid +
 * Favorites + Recent home, real folder browsing on navigation). Theme-aware via
 * [NsColor] so it matches the active launcher theme. Real filesystem via java.io.
 */

private data class SideEntry(val label: String, val icon: ImageVector, val dir: File?)

@Composable
fun FileExplorerApp() {
    val context = LocalContext.current
    val root = remember { Environment.getExternalStorageDirectory() ?: context.filesDir }

    // current == null → the "Home" landing (Quick access / Recent).
    var current by remember { mutableStateOf<File?>(null) }
    val backStack = remember { mutableStateListOf<File?>() }
    var rev by remember { mutableIntStateOf(0) }

    var accessTick by remember { mutableIntStateOf(0) }
    val hasAccess = remember(accessTick) { hasStorageAccess(context) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { accessTick++ }

    fun go(dir: File?) { backStack.add(current); current = dir }
    fun back() { if (backStack.isNotEmpty()) current = backStack.removeAt(backStack.lastIndex) }

    val sidebar = remember(root) {
        listOf(
            SideEntry("Home", Icons.Filled.Home, null),
            SideEntry("Downloads", Icons.Filled.Download, File(root, "Download")),
            SideEntry("Documents", Icons.Filled.Description, File(root, "Documents")),
            SideEntry("Pictures", Icons.Filled.Image, File(root, "Pictures")),
            SideEntry("Music", Icons.Filled.MusicNote, File(root, "Music")),
            SideEntry("Videos", Icons.Filled.Movie, File(root, "Movies")),
            SideEntry("This PC", Icons.Filled.Computer, root),
        )
    }

    Row(Modifier.fillMaxSize().background(NsColor.Solid)) {
        // ── Left nav sidebar ──
        Column(
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight()
                .background(NsColor.Mica)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
        ) {
            sidebar.forEach { e ->
                val selected = (e.dir?.absolutePath == current?.absolutePath)
                SidebarItem(e, selected) { go(e.dir) }
            }
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(NsColor.Stroke))

        // ── Main content ──
        Column(Modifier.weight(1f).fillMaxHeight()) {
            CommandBar(
                hasAccess = hasAccess,
                onNew = {
                    val dir = current ?: root
                    val made = newFolder(dir)
                    Toast.makeText(
                        context,
                        if (made != null) "Created ${made.name}" else "Couldn't create folder",
                        Toast.LENGTH_SHORT,
                    ).show()
                    rev++
                },
                onGrant = { requestStorageAccess(context, permLauncher) },
            )
            AddressBar(
                current = current,
                root = root,
                canBack = backStack.isNotEmpty(),
                onBack = { back() },
                onUp = {
                    val c = current
                    if (c != null && c.absolutePath != root.absolutePath) go(c.parentFile ?: root) else go(null)
                },
                onHome = { go(null) },
                onCrumb = { go(it) },
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(NsColor.Stroke))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (current == null) {
                    HomeLanding(root = root, onOpenDir = { go(it) }, onOpenFile = { openFile(context, it) })
                } else {
                    FolderView(dir = current!!, rev = rev, onOpenDir = { go(it) }, onOpenFile = { openFile(context, it) })
                }
            }
        }
    }
}

// ────────────────────────── sidebar ──────────────────────────

@Composable
private fun SidebarItem(entry: SideEntry, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) NsColor.ControlSelected else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp))
                .background(if (selected) LauncherState.accent else Color.Transparent),
        )
        Spacer(Modifier.width(6.dp))
        Icon(entry.icon, null, tint = NsColor.AccentLight, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(entry.label, color = NsColor.Text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ────────────────────────── command bar ──────────────────────────

@Composable
private fun CommandBar(hasAccess: Boolean, onNew: () -> Unit, onGrant: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(NsColor.Mica)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(NsColor.ControlActive)
                .clickable(onClick = onNew)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Add, "New", tint = NsColor.Text, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("New", color = NsColor.Text, fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        BarGlyph(Icons.Filled.Edit)
        BarGlyph(Icons.Filled.Share)
        BarGlyph(Icons.Filled.Delete)
        Spacer(Modifier.width(4.dp))
        Text("Sort", color = NsColor.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(4.dp))
        Text("View", color = NsColor.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(4.dp))
        Spacer(Modifier.weight(1f))
        if (!hasAccess) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(LauncherState.accent)
                    .clickable(onClick = onGrant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Lock, null, tint = Color.White, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
                Text("Grant access", color = Color.White, fontSize = 11.sp)
            }
        } else {
            Text("Details", color = LauncherState.accent, fontSize = 12.sp, modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
private fun BarGlyph(icon: ImageVector) {
    Icon(icon, null, tint = NsColor.TextSecondary, modifier = Modifier.size(18.dp).padding(end = 0.dp))
    Spacer(Modifier.width(8.dp))
}

// ────────────────────────── address / breadcrumb ──────────────────────────

@Composable
private fun AddressBar(
    current: File?,
    root: File,
    canBack: Boolean,
    onBack: () -> Unit,
    onUp: () -> Unit,
    onHome: () -> Unit,
    onCrumb: (File) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavGlyph(Icons.Filled.ChevronLeft, "Back", enabled = canBack, onClick = onBack)
        NavGlyph(Icons.Filled.ArrowUpward, "Up", enabled = current != null, onClick = onUp)
        NavGlyph(Icons.Filled.Home, "Home", enabled = true, onClick = onHome)
        Spacer(Modifier.width(6.dp))
        // Breadcrumb pill.
        Row(
            modifier = Modifier
                .weight(1f)
                .height(26.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(NsColor.ControlActive)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Folder, null, tint = NsColor.AccentLight, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            val crumbs = breadcrumb(current, root)
            crumbs.forEachIndexed { i, seg ->
                Text(
                    seg.first,
                    color = if (i == crumbs.lastIndex) NsColor.Text else NsColor.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.clickable { seg.second?.let(onCrumb) ?: onHome() },
                )
                if (i != crumbs.lastIndex) {
                    Text("  ›  ", color = NsColor.TextTertiary, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.width(6.dp))
        Row(
            modifier = Modifier.width(120.dp).height(26.dp).clip(RoundedCornerShape(4.dp))
                .background(NsColor.ControlActive).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Search", color = NsColor.TextTertiary, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Search, null, tint = NsColor.TextTertiary, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun NavGlyph(icon: ImageVector, desc: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(26.dp).clip(RoundedCornerShape(4.dp)).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, desc, tint = if (enabled) NsColor.Text else NsColor.TextTertiary, modifier = Modifier.size(16.dp)) }
}

// ────────────────────────── home landing ──────────────────────────

@Composable
private fun HomeLanding(root: File, onOpenDir: (File) -> Unit, onOpenFile: (File) -> Unit) {
    val quick = remember(root) {
        listOf(
            Triple("Desktop", Color(0xFF4C8DFF), File(root, "Desktop")),
            Triple("Downloads", Color(0xFF3FD07A), File(root, "Download")),
            Triple("Documents", Color(0xFF4C8DFF), File(root, "Documents")),
            Triple("Pictures", Color(0xFFFF8D28), File(root, "Pictures")),
            Triple("Music", Color(0xFFFF5C8A), File(root, "Music")),
            Triple("Videos", Color(0xFF9D5CFF), File(root, "Movies")),
        )
    }
    val recent = remember { recentFiles(root) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Header("Quick access")
        Spacer(Modifier.height(10.dp))
        quick.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (name, color, dir) ->
                    QuickTile(name, color, Modifier.weight(1f)) { onOpenDir(dir) }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(12.dp))
        Header("Favorites")
        Spacer(Modifier.height(6.dp))
        Text(
            "After you've favorited some files, we'll show them here.",
            color = NsColor.TextTertiary, fontSize = 12.sp,
        )

        Spacer(Modifier.height(16.dp))
        Header("Recent")
        Spacer(Modifier.height(6.dp))
        if (recent.isEmpty()) {
            Text("Nothing recent yet.", color = NsColor.TextTertiary, fontSize = 12.sp)
        } else {
            recent.forEach { f -> FileRow(f) { onOpenFile(f) } }
        }
        Spacer(Modifier.height(12.dp))
        Text("${quick.size + recent.size} items", color = NsColor.TextTertiary, fontSize = 11.sp)
    }
}

@Composable
private fun Header(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.ChevronRight, null, tint = NsColor.TextTertiary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = NsColor.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuickTile(name: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NsColor.ControlActive)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Folder, null, tint = color, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(name, color = NsColor.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Stored locally", color = NsColor.TextTertiary, fontSize = 11.sp)
        }
    }
}

// ────────────────────────── folder browsing ──────────────────────────

@Composable
private fun FolderView(dir: File, rev: Int, onOpenDir: (File) -> Unit, onOpenFile: (File) -> Unit) {
    val entries = remember(dir, rev) {
        (dir.listFiles()?.toList() ?: emptyList())
            .sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase(Locale.getDefault()) })
    }
    if (entries.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Folder, null, tint = NsColor.TextTertiary, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(8.dp))
            Text("This folder is empty", color = NsColor.TextTertiary, fontSize = 12.sp)
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp)) {
        items(entries.size) { i ->
            val f = entries[i]
            FileRow(f) { if (f.isDirectory) onOpenDir(f) else onOpenFile(f) }
        }
        item {
            Text("${entries.size} items", color = NsColor.TextTertiary, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun FileRow(f: File, onClick: () -> Unit) {
    val kind = fileKind(f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(kindIcon(kind), kind, tint = NsColor.AccentLight, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(f.name, color = NsColor.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                if (f.isDirectory) "Folder" else "$kind · ${humanSize(f.length())}",
                color = NsColor.TextTertiary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Text(shortDate(f.lastModified()), color = NsColor.TextTertiary, fontSize = 11.sp)
    }
}

// ────────────────────────── helpers ──────────────────────────

private fun breadcrumb(current: File?, root: File): List<Pair<String, File?>> {
    if (current == null) return listOf("Home" to null)
    val out = mutableListOf<Pair<String, File?>>("Home" to null)
    if (current.absolutePath == root.absolutePath) { out.add("This PC" to root); return out }
    val rel = current.absolutePath.removePrefix(root.absolutePath).trim('/')
    var acc = root
    out.add("This PC" to root)
    if (rel.isNotEmpty()) {
        rel.split('/').forEach { seg ->
            acc = File(acc, seg)
            out.add(seg to acc)
        }
    }
    return out
}

private fun recentFiles(root: File): List<File> {
    val folders = listOf(
        File(root, "Download"), File(root, "Documents"),
        File(root, "DCIM/Screenshots"), File(root, "Pictures"),
    )
    return folders.filter { it.isDirectory }
        .flatMap { it.listFiles()?.filter { f -> f.isFile && !f.name.startsWith(".") } ?: emptyList() }
        .sortedByDescending { it.lastModified() }
        .take(8)
}

private fun openFile(context: Context, f: File) {
    // No FileProvider configured; keep it non-fatal — surface the file instead of
    // crashing on a raw file:// Uri (blocked since Android 7).
    Toast.makeText(context, "${f.name} · ${humanSize(f.length())}", Toast.LENGTH_SHORT).show()
}

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

private fun humanSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024L * 1024 -> "%.0f KB".format(bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / 1024.0 / 1024)
    else -> "%.1f GB".format(bytes / 1024.0 / 1024 / 1024)
}

private fun shortDate(millis: Long): String =
    SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(millis))
