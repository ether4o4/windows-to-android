package com.neversoft.launcher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.Brand
import com.neversoft.launcher.ui.AccentOptions
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.Wallpapers
import com.neversoft.launcher.ui.theme.NsColor

/** Settings → Personalization, with working wallpaper / accent / taskbar controls. */
@Composable
fun SettingsApp() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("Personalization", color = NsColor.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))

        SectionLabel("Background")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Wallpapers.forEachIndexed { i, brush ->
                WallpaperThumb(brush, selected = i == LauncherState.wallpaperIndex) {
                    LauncherState.wallpaperIndex = i
                }
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
            AccentOptions.forEach { c ->
                Swatch(c, selected = c == LauncherState.accent) { LauncherState.accent = c }
            }
        }
        Spacer(Modifier.height(20.dp))

        SectionLabel("Taskbar")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SegButton("Small", selected = LauncherState.taskbarSmall) { LauncherState.taskbarSmall = true }
            SegButton("Large", selected = !LauncherState.taskbarSmall) { LauncherState.taskbarSmall = false }
        }
        Spacer(Modifier.height(24.dp))

        HorizontalDivider(color = NsColor.Stroke)
        Spacer(Modifier.height(16.dp))
        AboutSection()
    }
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
        Text(label, color = NsColor.TextTertiary, fontSize = 13.sp, modifier = Modifier.size(width = 120.dp, height = 18.dp))
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
