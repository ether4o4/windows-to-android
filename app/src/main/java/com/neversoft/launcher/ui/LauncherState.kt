package com.neversoft.launcher.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

/** Our own in-launcher apps that open as draggable windows. */
enum class LauncherApp(val title: String) {
    Settings("Settings"),
    About("About this PC"),
    FileExplorer("File Explorer"),
    Notepad("Notepad"),
    Calculator("Calculator"),
    Clock("Clock"),
    Photos("Photos"),
    Calendar("Calendar"),
    Weather("Weather"),
    Paint("Paint"),
    TaskManager("Task Manager"),
    Store("NeverSoft Store"),
    MediaPlayer("Media Player"),
}

/**
 * Compose-observable launcher personalization (doctrine §4.1 single source).
 * In-memory for now; persistence to SharedPreferences is a refinement.
 */
object LauncherState {
    var accent by mutableStateOf(NsColor.Accent)
    var wallpaperIndex by mutableStateOf(0)
    var taskbarSmall by mutableStateOf(false)

    val taskbarHeight: Dp
        get() = if (taskbarSmall) NsDim.TaskbarHeightSmall else NsDim.TaskbarHeight
}

/** Accent options (NeverSoft blue first, then an "Aero Nova" luxury set, §4.3). */
val AccentOptions: List<Color> = listOf(
    NsColor.Accent, // #0078D4 canonical
    Color(0xFF4CC2FF),
    Color(0xFF9D5CFF),
    Color(0xFFFF5C8A),
    Color(0xFFFF8D28),
    Color(0xFF3FD07A),
    Color(0xFFFFD60A),
    Color(0xFF61D6D6),
)

/** Ship-your-own wallpapers (licensing note); Win11-style blooms. */
val Wallpapers: List<Brush> = listOf(
    Brush.radialGradient(listOf(Color(0xFF2A7DE1), Color(0xFF143C77), Color(0xFF081226))),
    Brush.radialGradient(listOf(Color(0xFF5B2A86), Color(0xFF2A1450), Color(0xFF0B0717))),
    Brush.radialGradient(listOf(Color(0xFF0F7A6B), Color(0xFF0A3D4D), Color(0xFF061826))),
    Brush.linearGradient(listOf(Color(0xFFFF7E5F), Color(0xFFB14A8E), Color(0xFF3B2667))),
)
