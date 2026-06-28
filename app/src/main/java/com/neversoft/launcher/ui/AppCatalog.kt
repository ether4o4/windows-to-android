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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** A built-in (in-launcher) app shown in Start / All apps. */
data class CatalogApp(val app: LauncherApp, val icon: ImageVector, val color: Color)

/** Single source of truth for the built-in apps surfaced in Start. */
val InLauncherApps: List<CatalogApp> = listOf(
    CatalogApp(LauncherApp.FileExplorer, Icons.Filled.FolderOpen, Color(0xFFFFB300)),
    CatalogApp(LauncherApp.Notepad, Icons.Filled.EditNote, Color(0xFF1E88E5)),
    CatalogApp(LauncherApp.Calculator, Icons.Filled.Calculate, Color(0xFF00897B)),
    CatalogApp(LauncherApp.Clock, Icons.Filled.AccessTime, Color(0xFF3949AB)),
    CatalogApp(LauncherApp.Photos, Icons.Filled.Photo, Color(0xFFD81B60)),
    CatalogApp(LauncherApp.Calendar, Icons.Filled.CalendarMonth, Color(0xFFE53935)),
    CatalogApp(LauncherApp.Weather, Icons.Filled.WbSunny, Color(0xFF039BE5)),
    CatalogApp(LauncherApp.Paint, Icons.Filled.Brush, Color(0xFFFB8C00)),
    CatalogApp(LauncherApp.MediaPlayer, Icons.Filled.MusicNote, Color(0xFF8E24AA)),
    CatalogApp(LauncherApp.TaskManager, Icons.Filled.Speed, Color(0xFF43A047)),
    CatalogApp(LauncherApp.Store, Icons.Filled.Storefront, Color(0xFF1976D2)),
    CatalogApp(LauncherApp.Settings, Icons.Filled.Settings, Color(0xFF546E7A)),
    CatalogApp(LauncherApp.About, Icons.Filled.Info, Color(0xFF607D8B)),
)

private fun entryFor(app: LauncherApp): CatalogApp? = InLauncherApps.firstOrNull { it.app == app }

/** Icon for any built-in app (taskbar running-window buttons). */
fun iconForApp(app: LauncherApp): ImageVector = entryFor(app)?.icon ?: Icons.Filled.Info

/** Tile colour for any built-in app. */
fun colorForApp(app: LauncherApp): Color = entryFor(app)?.color ?: Color(0xFF607D8B)
