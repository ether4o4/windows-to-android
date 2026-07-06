package com.neversoft.launcher.ui.apps

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

/** One audio track resolved from MediaStore. */
private data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val uri: Uri,
)

@Composable
fun MediaPlayerApp() {
    val context = LocalContext.current
    val tracks = remember { queryTracks(context) }

    // Mock transport state. -1 means nothing selected yet.
    var currentIndex by remember { mutableIntStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }

    val current = tracks.getOrNull(currentIndex)

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
                Icons.Filled.MusicNote,
                null,
                tint = LauncherState.accent,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "NeverSoft Media Player",
                color = NsColor.Text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            Text(
                if (tracks.isEmpty()) "" else "${tracks.size} track${if (tracks.size == 1) "" else "s"}",
                color = NsColor.TextSecondary,
                fontSize = 13.sp,
            )
        }

        // Track list.
        Box(Modifier.fillMaxWidth().weight(1f)) {
            if (tracks.isEmpty()) {
                EmptyLibrary()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(tracks) { track ->
                        val index = tracks.indexOf(track)
                        TrackRow(
                            track = track,
                            selected = index == currentIndex,
                            onClick = {
                                currentIndex = index
                                isPlaying = true
                            },
                        )
                    }
                }
            }
        }

        // Now-playing bar.
        NowPlayingBar(
            current = current,
            isPlaying = isPlaying,
            onPrevious = {
                if (tracks.isNotEmpty()) {
                    currentIndex = if (currentIndex <= 0) tracks.lastIndex else currentIndex - 1
                    isPlaying = true
                }
            },
            onPlayPause = {
                if (current == null && tracks.isNotEmpty()) {
                    currentIndex = 0
                    isPlaying = true
                } else {
                    isPlaying = !isPlaying
                }
            },
            onNext = {
                if (tracks.isNotEmpty()) {
                    currentIndex = if (currentIndex >= tracks.lastIndex) 0 else currentIndex + 1
                    isPlaying = true
                }
            },
        )
    }
}

@Composable
private fun TrackRow(track: Track, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(if (selected) NsColor.ControlSelected else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(NsDim.RadiusControl))
                .background(NsColor.ControlActive),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.MusicNote,
                null,
                tint = if (selected) LauncherState.accent else NsColor.TextTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                color = if (selected) LauncherState.accent else NsColor.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                track.artist,
                color = NsColor.TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            formatDuration(track.durationMs),
            color = NsColor.TextTertiary,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun NowPlayingBar(
    current: Track?,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(NsColor.Mica),
    ) {
        // Thin progress line. Mock: full accent when playing, faint when idle.
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(NsColor.Stroke),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(if (current != null && isPlaying) 0.4f else 0f)
                    .height(2.dp)
                    .background(LauncherState.accent),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    current?.title ?: "Nothing playing",
                    color = if (current != null) NsColor.Text else NsColor.TextTertiary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    current?.artist ?: "Select a track to begin",
                    color = NsColor.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))

            TransportButton(Icons.Filled.SkipPrevious, "Previous", onPrevious)
            Spacer(Modifier.width(8.dp))

            // Play / pause uses the accent fill.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LauncherState.accent)
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(8.dp))

            TransportButton(Icons.Filled.SkipNext, "Next", onNext)
        }
    }
}

@Composable
private fun TransportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(NsColor.ControlActive)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = NsColor.Text,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun EmptyLibrary() {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.MusicNote,
            null,
            tint = NsColor.TextTertiary,
            modifier = Modifier.size(44.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "No music to play",
            color = NsColor.Text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "NeverSoft Media Player couldn't find any audio, or it doesn't have permission to read your music library.",
            color = NsColor.TextTertiary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------- data ----------------

private fun queryTracks(context: Context): List<Track> = runCatching {
    val out = ArrayList<Track>()
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
    )
    context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Audio.Media.IS_MUSIC} != 0",
        null,
        "${MediaStore.Audio.Media.TITLE} ASC",
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        while (cursor.moveToNext() && out.size < 500) {
            val id = cursor.getLong(idCol)
            val title = cursor.getString(titleCol) ?: "Unknown title"
            val artist = cursor.getString(artistCol) ?: "Unknown artist"
            val duration = cursor.getLong(durationCol)
            val uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id,
            )
            out.add(Track(id, title, artist, duration, uri))
        }
    }
    out
}.getOrDefault(emptyList())

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
