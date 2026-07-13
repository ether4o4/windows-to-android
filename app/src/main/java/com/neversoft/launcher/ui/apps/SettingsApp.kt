package com.neversoft.launcher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.Brand
import com.neversoft.launcher.ui.AccentOptions
import com.neversoft.launcher.ui.InLauncherApps
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.Wallpapers
import com.neversoft.launcher.ui.components.AppIconTile
import com.neversoft.launcher.ui.theme.LauncherTheme
import com.neversoft.launcher.ui.theme.LauncherThemes
import com.neversoft.launcher.ui.theme.NsColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class SettingsPage(val title: String, val icon: ImageVector) {
    System("System", Icons.Filled.Tune),
    Personalization("Personalization", Icons.Filled.Palette),
    Apps("Apps", Icons.Filled.GridView),
    Accounts("Accounts", Icons.Filled.AccountCircle),
    Time("Time & language", Icons.Filled.Schedule),
    About("About", Icons.Filled.Info),
}

/** Windows 11-style Settings: left navigation rail + scrollable content pane. */
@Composable
fun SettingsApp() {
    var page by remember { mutableStateOf(SettingsPage.Personalization) }
    Row(Modifier.fillMaxSize()) {
        // Navigation rail.
        Column(
            modifier = Modifier
                .width(116.dp)
                .fillMaxHeight()
                .background(NsColor.Mica)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
        ) {
            Text(
                "Settings",
                color = NsColor.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 10.dp),
            )
            SettingsPage.entries.forEach { p ->
                NavItem(p, selected = page == p) { page = p }
            }
        }

        // Content pane.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
        ) {
            when (page) {
                SettingsPage.Personalization -> PersonalizationPage()
                SettingsPage.System -> SystemPage()
                SettingsPage.Apps -> AppsPage()
                SettingsPage.Accounts -> AccountsPage()
                SettingsPage.Time -> TimePage()
                SettingsPage.About -> AboutSection()
            }
        }
    }
}

@Composable
private fun NavItem(page: SettingsPage, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) NsColor.ControlSelected else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp))
                .background(if (selected) LauncherState.accent else Color.Transparent),
        )
        Spacer(Modifier.width(6.dp))
        Icon(page.icon, null, tint = NsColor.Text, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(8.dp))
        Text(page.title, color = NsColor.Text, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// ---------------- pages ----------------

@Composable
private fun PersonalizationPage() {
    PageTitle("Personalization")
    SectionLabel("Theme")
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LauncherThemes.forEach { t ->
            ThemeSwatch(t, selected = t.id == LauncherState.launcherThemeId) {
                LauncherState.launcherThemeId = t.id
                // Flip content mode so text stays readable on the chosen surface.
                NsColor.isDark = !t.isLight
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    SectionLabel("Background")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Wallpapers.forEachIndexed { i, brush ->
            WallpaperThumb(brush, selected = i == LauncherState.wallpaperIndex) { LauncherState.wallpaperIndex = i }
        }
    }
    Spacer(Modifier.height(20.dp))
    SectionLabel("Mode")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SegButton("Dark", selected = NsColor.isDark) { NsColor.isDark = true }
        SegButton("Light", selected = !NsColor.isDark) { NsColor.isDark = false }
    }
    Spacer(Modifier.height(20.dp))
    SectionLabel("Accent color")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AccentOptions.forEach { c -> Swatch(c, selected = c == LauncherState.accent) { LauncherState.accent = c } }
    }
    Spacer(Modifier.height(20.dp))
    SectionLabel("Taskbar")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SegButton("Small", selected = LauncherState.taskbarSmall) { LauncherState.taskbarSmall = true }
        SegButton("Large", selected = !LauncherState.taskbarSmall) { LauncherState.taskbarSmall = false }
    }
}

@Composable
private fun SystemPage() {
    PageTitle("System")
    SectionLabel("Display brightness")
    SliderRow(0.8f)
    Spacer(Modifier.height(16.dp))
    SectionLabel("Volume")
    SliderRow(0.5f)
    Spacer(Modifier.height(16.dp))
    InfoCard("Power & battery", "Balanced power mode · battery reported by the system tray.")
    Spacer(Modifier.height(10.dp))
    InfoCard("Storage", "Manage files in File Explorer (Ghost Key).")
}

@Composable
private fun AppsPage() {
    PageTitle("Apps")
    SectionLabel("Built-in apps (${InLauncherApps.size})")
    InLauncherApps.forEach { cat ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconTile(cat.icon, cat.color, 28.dp)
            Spacer(Modifier.width(12.dp))
            Text(cat.app.title, color = NsColor.Text, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AccountsPage() {
    PageTitle("Accounts")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(LauncherState.accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.AccountCircle, null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(Brand.USER, color = NsColor.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("${Brand.NAME} account", color = NsColor.TextTertiary, fontSize = 12.sp)
        }
    }
    Spacer(Modifier.height(16.dp))
    InfoCard("Sign-in options", "PIN, fingerprint, and face are managed by Android.")
}

@Composable
private fun TimePage() {
    PageTitle("Time & language")
    val now = remember { LocalDateTime.now() }
    SpecRow("Date", now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")))
    SpecRow("Time", now.format(DateTimeFormatter.ofPattern("h:mm a")))
    SpecRow("Time zone", java.util.TimeZone.getDefault().id)
    SpecRow("Language", java.util.Locale.getDefault().displayLanguage)
    Spacer(Modifier.height(12.dp))
    InfoCard("Set time automatically", "On — synced with the network.")
}

@Composable
private fun PageTitle(text: String) {
    Text(text, color = NsColor.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun InfoCard(title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(NsColor.ControlActive).padding(14.dp),
    ) {
        Text(title, color = NsColor.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(body, color = NsColor.TextTertiary, fontSize = 12.sp)
    }
}

@Composable
private fun SliderRow(initial: Float) {
    var v by remember { mutableFloatStateOf(initial) }
    Slider(
        value = v,
        onValueChange = { v = it },
        colors = SliderDefaults.colors(
            thumbColor = LauncherState.accent,
            activeTrackColor = LauncherState.accent,
            inactiveTrackColor = NsColor.StrokeStrong,
        ),
    )
}

/** Standalone "About this PC" window content. */
@Composable
fun AboutApp() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        AboutSection()
    }
}

@Composable
private fun AboutSection() {
    Text("About this PC", color = NsColor.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(12.dp))
    SpecRow("Device name", "NEVERSOFT-PC")
    SpecRow("Edition", "${Brand.SHORT} Pro")
    SpecRow("Version", Brand.VERSION)
    SpecRow("Processor", "Android (${Brand.NAME})")
    SpecRow("System type", "64-bit operating system")
    Spacer(Modifier.height(12.dp))
    Text(Brand.COPYRIGHT, color = NsColor.TextTertiary, fontSize = 11.sp)
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, color = NsColor.TextTertiary, fontSize = 13.sp, modifier = Modifier.width(118.dp))
        Text(value, color = NsColor.Text, fontSize = 13.sp)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = NsColor.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun WallpaperThumb(brush: Brush, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(brush)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) LauncherState.accent else NsColor.Stroke,
                shape = RoundedCornerShape(6.dp),
            )
            .clickable(onClick = onClick),
    )
}

@Composable
private fun ThemeSwatch(theme: LauncherTheme, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val preview = if (theme.glass) {
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.10f)))
        } else {
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), theme.panel))
        }
        Box(
            modifier = Modifier
                .size(width = 54.dp, height = 36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(preview)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) LauncherState.accent else NsColor.Stroke,
                    shape = RoundedCornerShape(6.dp),
                )
                .clickable(onClick = onClick),
        )
        Spacer(Modifier.height(4.dp))
        Text(theme.label, color = NsColor.TextSecondary, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) NsColor.Text else Color.Transparent,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}

@Composable
private fun SegButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) LauncherState.accent else NsColor.ControlActive)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(label, color = NsColor.Text, fontSize = 13.sp)
    }
}
