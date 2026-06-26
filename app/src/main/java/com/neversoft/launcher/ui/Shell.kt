package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.neversoft.launcher.apps.AppRepository

/** Windows 11 "bloom" wallpaper, ship-your-own per licensing note. */
val WindowsWallpaper: Brush = Brush.radialGradient(
    colors = listOf(
        Color(0xFF2A7DE1),
        Color(0xFF143C77),
        Color(0xFF081226),
    ),
)

enum class Overlay { None, Start, QuickSettings, Notifications }

/** Root desktop composition: wallpaper → desktop → overlays → taskbar (top-most). */
@Composable
fun Shell() {
    val context = LocalContext.current
    val apps = remember { AppRepository.loadApps(context) }
    var overlay by remember { mutableStateOf(Overlay.None) }

    fun toggle(target: Overlay) {
        overlay = if (overlay == target) Overlay.None else target
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WindowsWallpaper),
    ) {
        Desktop()

        // Overlays sit above the desktop but below the taskbar, so the taskbar
        // stays bright and interactive (Windows behaviour).
        when (overlay) {
            Overlay.Start -> StartMenu(
                apps = apps,
                onLaunch = { overlay = Overlay.None; AppRepository.launch(context, it) },
                onCommandPrompt = { overlay = Overlay.None; AppRepository.launchTermux(context) },
                onDismiss = { overlay = Overlay.None },
            )

            Overlay.QuickSettings -> QuickSettingsFlyout(onDismiss = { overlay = Overlay.None })
            Overlay.Notifications -> NotificationFlyout(onDismiss = { overlay = Overlay.None })
            Overlay.None -> Unit
        }

        Taskbar(
            apps = apps,
            startActive = overlay == Overlay.Start,
            quickActive = overlay == Overlay.QuickSettings,
            notifActive = overlay == Overlay.Notifications,
            onToggleStart = { toggle(Overlay.Start) },
            onToggleQuick = { toggle(Overlay.QuickSettings) },
            onToggleNotif = { toggle(Overlay.Notifications) },
            onLaunch = { AppRepository.launch(context, it) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
