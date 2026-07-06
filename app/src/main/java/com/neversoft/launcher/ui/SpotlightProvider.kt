package com.neversoft.launcher.ui

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import com.neversoft.launcher.apps.AppEntry
import com.neversoft.launcher.apps.AppRepository
import java.io.File

/**
 * Spotlight search providers, matching the Spotlight app's spec: aggregate
 * results from installed apps (PackageManager), contacts (ContactsContract),
 * media (MediaStore), and the filesystem (time-boxed traversal); rank by
 * relevance (exact > prefix > substring) with apps and contacts elevated.
 */
enum class SpotFilter(val label: String) {
    All("All"), Media("Media"), Files("Files"), Hidden("Hidden")
}

enum class MediaSub(val label: String) { Photos("Photos"), Videos("Videos"), Audio("Audio") }

enum class SpotKind { App, Contact, Photo, Video, Audio, FileItem }

class SpotItem(
    val title: String,
    val subtitle: String,
    val kind: SpotKind,
    val rank: Int,
    val appIcon: ImageBitmap?,
    val onOpen: (Context) -> Unit,
)

/** Run the aggregated search. Call off the main thread. */
fun runSpotlight(
    context: Context,
    query: String,
    apps: List<AppEntry>,
    filter: SpotFilter,
    sub: MediaSub?,
): List<SpotItem> {
    val q = query.trim()
    if (q.isBlank()) return emptyList()
    val out = ArrayList<SpotItem>()

    when (filter) {
        SpotFilter.All -> {
            out += appResults(apps, q)
            out += contactResults(context, q)
            out += mediaResults(context, q, SpotKind.Photo)
            out += mediaResults(context, q, SpotKind.Video)
            out += mediaResults(context, q, SpotKind.Audio)
            out += fileResults(context, q, hiddenOnly = false)
        }
        SpotFilter.Media -> {
            val kinds = when (sub) {
                MediaSub.Photos -> listOf(SpotKind.Photo)
                MediaSub.Videos -> listOf(SpotKind.Video)
                MediaSub.Audio -> listOf(SpotKind.Audio)
                null -> listOf(SpotKind.Photo, SpotKind.Video, SpotKind.Audio)
            }
            kinds.forEach { out += mediaResults(context, q, it) }
        }
        SpotFilter.Files -> out += fileResults(context, q, hiddenOnly = false)
        SpotFilter.Hidden -> out += fileResults(context, q, hiddenOnly = true)
    }

    // Rank, then elevate apps & contacts, then alphabetical.
    return out
        .sortedWith(compareBy({ it.rank }, { kindPriority(it.kind) }, { it.title.lowercase() }))
        .take(60)
}

private fun kindPriority(kind: SpotKind): Int = when (kind) {
    SpotKind.App -> 0
    SpotKind.Contact -> 1
    else -> 2
}

fun matchRank(name: String, query: String): Int {
    val n = name.lowercase()
    val s = query.lowercase()
    return when {
        n == s -> 0
        n.startsWith(s) -> 1
        n.contains(s) -> 2
        else -> 3
    }
}

// ---------------- providers ----------------

private fun appResults(apps: List<AppEntry>, q: String): List<SpotItem> =
    apps.mapNotNull { app ->
        val rank = matchRank(app.label, q)
        if (rank > 2) return@mapNotNull null
        SpotItem(app.label, "App", SpotKind.App, rank, app.icon) { ctx ->
            AppRepository.launch(ctx, app.packageName)
        }
    }

private fun contactResults(context: Context, q: String): List<SpotItem> = runCatching {
    val out = ArrayList<SpotItem>()
    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    val nameCol = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
    val numCol = ContactsContract.CommonDataKinds.Phone.NUMBER
    context.contentResolver.query(
        uri, arrayOf(nameCol, numCol), "$nameCol LIKE ?", arrayOf("%$q%"), null,
    )?.use { c ->
        val ni = c.getColumnIndexOrThrow(nameCol)
        val pi = c.getColumnIndexOrThrow(numCol)
        val seen = HashSet<String>()
        while (c.moveToNext() && out.size < 25) {
            val name = c.getString(ni) ?: continue
            if (!seen.add(name)) continue
            val rank = matchRank(name, q)
            if (rank > 2) continue
            val number = c.getString(pi).orEmpty()
            out += SpotItem(name, number.ifBlank { "Contact" }, SpotKind.Contact, rank, null) { ctx ->
                runCatching {
                    ctx.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number)))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
        }
    }
    out
}.getOrDefault(emptyList())

private fun mediaResults(context: Context, q: String, kind: SpotKind): List<SpotItem> = runCatching {
    val (uri, type) = when (kind) {
        SpotKind.Photo -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI to "image/*"
        SpotKind.Video -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI to "video/*"
        else -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI to "audio/*"
    }
    val idCol = MediaStore.MediaColumns._ID
    val nameCol = MediaStore.MediaColumns.DISPLAY_NAME
    val out = ArrayList<SpotItem>()
    context.contentResolver.query(
        uri, arrayOf(idCol, nameCol), "$nameCol LIKE ?", arrayOf("%$q%"), "$nameCol ASC",
    )?.use { c ->
        val ii = c.getColumnIndexOrThrow(idCol)
        val di = c.getColumnIndexOrThrow(nameCol)
        while (c.moveToNext() && out.size < 25) {
            val name = c.getString(di) ?: continue
            val rank = matchRank(name, q)
            if (rank > 2) continue
            val content = ContentUris.withAppendedId(uri, c.getLong(ii))
            out += SpotItem(name, kindLabel(kind), kind, rank, null) { ctx ->
                runCatching {
                    ctx.startActivity(
                        Intent(Intent.ACTION_VIEW)
                            .setDataAndType(content, type)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
        }
    }
    out
}.getOrDefault(emptyList())

private fun fileResults(context: Context, q: String, hiddenOnly: Boolean): List<SpotItem> = runCatching {
    val root = Environment.getExternalStorageDirectory() ?: return emptyList()
    val deadline = System.currentTimeMillis() + 300 // time-boxed, per Spotlight spec
    val cap = 40
    val out = ArrayList<SpotItem>()
    val stack = ArrayDeque<File>()
    stack.addLast(root)
    while (stack.isNotEmpty() && out.size < cap && System.currentTimeMillis() < deadline) {
        val dir = stack.removeLast()
        val children = dir.listFiles() ?: continue
        for (f in children) {
            if (out.size >= cap || System.currentTimeMillis() >= deadline) break
            val name = f.name
            val isHidden = name.startsWith(".")
            if (hiddenOnly && !isHidden) {
                if (f.isDirectory) stack.addLast(f)
                continue
            }
            val rank = matchRank(name, q)
            if (rank <= 2 && (!hiddenOnly || isHidden)) {
                out += SpotItem(name, f.parentFile?.name ?: "/", SpotKind.FileItem, rank, null) { ctx ->
                    Toast.makeText(ctx, f.absolutePath, Toast.LENGTH_LONG).show()
                }
            }
            if (f.isDirectory) stack.addLast(f)
        }
    }
    out
}.getOrDefault(emptyList())

private fun kindLabel(kind: SpotKind): String = when (kind) {
    SpotKind.App -> "App"
    SpotKind.Contact -> "Contact"
    SpotKind.Photo -> "Photo"
    SpotKind.Video -> "Video"
    SpotKind.Audio -> "Audio"
    SpotKind.FileItem -> "File"
}
