package com.neversoft.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.apps.AppRepository
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

/** Desktop surface: wallpaper (drawn by Shell) + top-left shortcut grid. */
@Composable
fun Desktop() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, top = 36.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        DesktopIcon(Icons.Filled.Computer, "This PC") {}
        DesktopIcon(Icons.Filled.Delete, "Recycle Bin") {}
        DesktopIcon(Icons.Filled.Terminal, "Command Prompt") {
            AppRepository.launchTermux(context)
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
