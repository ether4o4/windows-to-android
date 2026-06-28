package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.neversoft.launcher.apps.AppRepository
import com.neversoft.launcher.ui.apps.AboutApp
import com.neversoft.launcher.ui.apps.CalculatorApp
import com.neversoft.launcher.ui.apps.CalendarApp
import com.neversoft.launcher.ui.apps.ClockApp
import com.neversoft.launcher.ui.apps.FileExplorerApp
import com.neversoft.launcher.ui.apps.MediaPlayerApp
import com.neversoft.launcher.ui.apps.NotepadApp
import com.neversoft.launcher.ui.apps.PaintApp
import com.neversoft.launcher.ui.apps.PhotosApp
import com.neversoft.launcher.ui.apps.SettingsApp
import com.neversoft.launcher.ui.apps.StoreApp
import com.neversoft.launcher.ui.apps.TaskManagerApp
import com.neversoft.launcher.ui.apps.WeatherApp
import com.neversoft.launcher.ui.window.AppWindow

enum class Overlay { None, Start, QuickSettings, Notifications, TaskView, Widgets }

/** Root composition: wallpaper → desktop → app windows → overlays → taskbar (top). */
@Composable
fun Shell() {
    val context = LocalContext.current
    val apps = remember { AppRepository.loadApps(context) }
    var overlay by remember { mutableStateOf(Overlay.None) }
    val windows = remember { mutableStateListOf<LauncherApp>() }

    fun openApp(app: LauncherApp) {
        windows.remove(app) // re-add to bring to front (top of z-order)
        windows.add(app)
        overlay = Overlay.None
    }

    fun toggle(target: Overlay) {
        overlay = if (overlay == target) Overlay.None else target
    }

    val wallpaper = Wallpapers[LauncherState.wallpaperIndex.coerceIn(0, Wallpapers.size - 1)]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(wallpaper),
    ) {
        Desktop(
            onOpenSettings = { openApp(LauncherApp.Settings) },
            onOpenAbout = { openApp(LauncherApp.About) },
            onOpenFiles = { openApp(LauncherApp.FileExplorer) },
            onCommandPrompt = { AppRepository.launchTermux(context) },
        )

        // Our own in-launcher app windows, drawn in z-order.
        windows.forEach { app ->
            key(app) {
                AppWindow(title = app.title, onClose = { windows.remove(app) }) {
                    when (app) {
                        LauncherApp.Settings -> SettingsApp()
                        LauncherApp.About -> AboutApp()
                        LauncherApp.FileExplorer -> FileExplorerApp()
                        LauncherApp.Notepad -> NotepadApp()
                        LauncherApp.Calculator -> CalculatorApp()
                        LauncherApp.Clock -> ClockApp()
                        LauncherApp.Photos -> PhotosApp()
                        LauncherApp.Calendar -> CalendarApp()
                        LauncherApp.Weather -> WeatherApp()
                        LauncherApp.Paint -> PaintApp()
                        LauncherApp.TaskManager -> TaskManagerApp()
                        LauncherApp.Store -> StoreApp()
                        LauncherApp.MediaPlayer -> MediaPlayerApp()
                    }
                }
            }
        }

        when (overlay) {
            Overlay.Start -> StartMenu(
                apps = apps,
                onLaunch = { overlay = Overlay.None; AppRepository.launch(context, it) },
                onCommandPrompt = { overlay = Overlay.None; AppRepository.launchTermux(context) },
                onOpenApp = { openApp(it) },
                onDismiss = { overlay = Overlay.None },
            )

            Overlay.QuickSettings -> QuickSettingsFlyout(onDismiss = { overlay = Overlay.None })
            Overlay.Notifications -> NotificationFlyout(onDismiss = { overlay = Overlay.None })
            Overlay.TaskView -> TaskView(
                windows = windows.toList(),
                onSelect = { openApp(it) },
                onClose = { windows.remove(it) },
                onDismiss = { overlay = Overlay.None },
            )

            Overlay.Widgets -> WidgetsBoard(onDismiss = { overlay = Overlay.None })
            Overlay.None -> Unit
        }

        Taskbar(
            startActive = overlay == Overlay.Start,
            quickActive = overlay == Overlay.QuickSettings,
            notifActive = overlay == Overlay.Notifications,
            taskViewActive = overlay == Overlay.TaskView,
            widgetsActive = overlay == Overlay.Widgets,
            onToggleStart = { toggle(Overlay.Start) },
            onToggleQuick = { toggle(Overlay.QuickSettings) },
            onToggleNotif = { toggle(Overlay.Notifications) },
            onToggleTaskView = { toggle(Overlay.TaskView) },
            onToggleWidgets = { toggle(Overlay.Widgets) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
