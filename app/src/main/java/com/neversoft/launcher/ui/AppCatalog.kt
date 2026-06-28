package com.neversoft.launcher.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/** A built-in (in-launcher) app shown in Start / All apps. */
data class CatalogApp(val app: LauncherApp, val icon: ImageVector)

/** Single source of truth for the built-in apps surfaced in Start. */
val InLauncherApps: List<CatalogApp> = listOf(
    CatalogApp(LauncherApp.FileExplorer, Icons.Filled.FolderOpen),
    CatalogApp(LauncherApp.Notepad, Icons.Filled.EditNote),
    CatalogApp(LauncherApp.Calculator, Icons.Filled.Calculate),
    CatalogApp(LauncherApp.Clock, Icons.Filled.AccessTime),
    CatalogApp(LauncherApp.Photos, Icons.Filled.Photo),
    CatalogApp(LauncherApp.Calendar, Icons.Filled.CalendarMonth),
    CatalogApp(LauncherApp.Weather, Icons.Filled.WbSunny),
    CatalogApp(LauncherApp.Paint, Icons.Filled.Brush),
    CatalogApp(LauncherApp.MediaPlayer, Icons.Filled.MusicNote),
    CatalogApp(LauncherApp.TaskManager, Icons.Filled.Speed),
    CatalogApp(LauncherApp.Store, Icons.Filled.Storefront),
    CatalogApp(LauncherApp.Settings, Icons.Filled.Settings),
    CatalogApp(LauncherApp.About, Icons.Filled.Info),
)

/** Icon for any built-in app (used by the taskbar's running-window buttons). */
fun iconForApp(app: LauncherApp): ImageVector =
    InLauncherApps.firstOrNull { it.app == app }?.icon ?: Icons.Filled.Info

