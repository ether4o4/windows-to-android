package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.modifier.acrylic
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

/** Task View (Win+Tab): cards for currently-open in-launcher windows. */
@Composable
fun TaskView(
    windows: List<LauncherApp>,
    onSelect: (LauncherApp) -> Unit,
    onClose: (LauncherApp) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Scrim)
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = LauncherState.taskbarHeight + 24.dp)
                .pointerInput(Unit) { detectTapGestures { } },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Task view", color = NsColor.Text, fontSize = 15.sp)
            Spacer(Modifier.height(16.dp))
            if (windows.isEmpty()) {
                Text("No open windows", color = NsColor.TextTertiary, fontSize = 13.sp)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    windows.forEach { app ->
                        WindowCard(app, onSelect = { onSelect(app) }, onClose = { onClose(app) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WindowCard(app: LauncherApp, onSelect: () -> Unit, onClose: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 124.dp)
                .clip(RoundedCornerShape(NsDim.RadiusOverlay))
                .acrylic(tint = NsColor.AcrylicStart, radius = NsDim.RadiusOverlay)
                .clickable(onClick = onSelect),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, "Close", tint = NsColor.TextSecondary, modifier = Modifier.size(14.dp))
            }
            Text(
                app.title,
                color = NsColor.Text,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(app.title, color = NsColor.TextSecondary, fontSize = 12.sp, modifier = Modifier.width(200.dp).padding(start = 4.dp))
    }
}
