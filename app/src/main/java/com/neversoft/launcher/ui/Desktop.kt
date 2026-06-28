package com.neversoft.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewModule
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import com.neversoft.launcher.Brand
import kotlin.math.roundToInt

/** Desktop surface: top-left shortcuts + Fluent right-click (long-press) menu. */
@Composable
fun Desktop(
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenFiles: () -> Unit,
    onCommandPrompt: () -> Unit,
) {
    var menuAt by remember { mutableStateOf<IntOffset?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { menuAt = IntOffset(it.x.roundToInt(), it.y.roundToInt()) },
                )
            },
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, top = 36.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            DesktopIcon(Icons.Filled.Computer, "This PC", onClick = onOpenAbout)
            DesktopIcon(Icons.Filled.FolderOpen, "File Explorer", onClick = onOpenFiles)
            DesktopIcon(Icons.Filled.Settings, "Settings", onClick = onOpenSettings)
            DesktopIcon(Icons.Filled.Delete, "Recycle Bin") {}
            DesktopIcon(Icons.Filled.Terminal, "Command Prompt", onClick = onCommandPrompt)
        }

        // Windows-style activation watermark, rebranded.
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = LauncherState.taskbarHeight + 18.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                Brand.NAME,
                color = NsColor.Text.copy(alpha = 0.62f),
                fontSize = 15.sp,
                lineHeight = 18.sp,
            )
            Text(
                "${Brand.SHORT} Pro  ·  Build ${Brand.VERSION}",
                color = NsColor.Text.copy(alpha = 0.42f),
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )
        }

        menuAt?.let { pos ->
            ContextMenu(
                position = pos,
                items = listOf(
                    MenuItem("View", Icons.Filled.ViewModule) {},
                    MenuItem("Sort by", Icons.Filled.Sort) {},
                    MenuItem("Refresh", Icons.Filled.Refresh, separatorAfter = true) {},
                    MenuItem("Open File Explorer", Icons.Filled.FolderOpen, onClick = onOpenFiles),
                    MenuItem("Personalize", Icons.Filled.Palette, onClick = onOpenSettings),
                    MenuItem("Open Command Prompt", Icons.Filled.Terminal, separatorAfter = true, onClick = onCommandPrompt),
                    MenuItem("Show more options", Icons.Filled.MoreHoriz) {},
                ),
                onDismiss = { menuAt = null },
            )
        }
    }
}

@Composable
private fun DesktopIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(84.dp)
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = NsColor.AccentLight,
            modifier = Modifier.height(NsDim.DesktopIcon),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = NsColor.Text,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
    }
}
