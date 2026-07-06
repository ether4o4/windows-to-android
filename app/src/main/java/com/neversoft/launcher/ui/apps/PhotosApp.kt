package com.neversoft.launcher.ui.apps

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One gallery item resolved from MediaStore. */
private data class PhotoItem(val uri: Uri, val name: String)

@Composable
fun PhotosApp() {
    val context = LocalContext.current
    val photos = remember { queryImages(context) }

    Column(Modifier.fillMaxSize().background(NsColor.Solid)) {
        // Header.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NsColor.Mica)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Photo,
                null,
                tint = LauncherState.accent,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text("Photos", color = NsColor.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(
                if (photos.isEmpty()) "" else "${photos.size} item${if (photos.size == 1) "" else "s"}",
                color = NsColor.TextSecondary,
                fontSize = 13.sp,
            )
        }

        if (photos.isEmpty()) {
            EmptyPhotos()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(photos.size) { i ->
                    PhotoTile(photos[i]) { openImage(context, photos[i].uri) }
                }
            }
        }
    }
}

@Composable
private fun PhotoTile(item: PhotoItem, onClick: () -> Unit) {
    val context = LocalContext.current
    // Lazily load each thumbnail off the main thread, keyed by uri so the grid stays responsive.
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = item.uri) {
        value = withContext(Dispatchers.IO) { loadThumbnail(context, item.uri) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(NsColor.ControlActive)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Filled.Image,
                    null,
                    tint = NsColor.TextTertiary,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    item.name,
                    color = NsColor.TextTertiary,
                    fontSize = 10.sp,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmptyPhotos() {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Photo,
            null,
            tint = NsColor.TextTertiary,
            modifier = Modifier.size(44.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("No photos to show", color = NsColor.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(
            "NeverSoft Photos couldn't find any images, or it doesn't have permission to read your media library.",
            color = NsColor.TextTertiary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------- data ----------------

private fun queryImages(context: Context): List<PhotoItem> = runCatching {
    val out = ArrayList<PhotoItem>()
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
    )
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC",
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        while (cursor.moveToNext() && out.size < 200) {
            val id = cursor.getLong(idCol)
            val name = cursor.getString(nameCol) ?: "Image"
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            out.add(PhotoItem(uri, name))
        }
    }
    out
}.getOrDefault(emptyList())

private fun loadThumbnail(context: Context, uri: Uri): ImageBitmap? = runCatching {
    context.contentResolver.loadThumbnail(uri, Size(200, 200), null).asImageBitmap()
}.getOrNull()

private fun openImage(context: Context, uri: Uri) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
