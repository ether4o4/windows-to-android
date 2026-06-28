package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.Brand
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * NeverSoft lock / boot screen: wallpaper + big clock + brand, unlock by tap or
 * swipe up. Shown on launch (boot) and whenever the user locks from Start.
 */
@Composable
fun LockScreen(onUnlock: () -> Unit) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }
    val time = remember(now.minute) { now.format(DateTimeFormatter.ofPattern("h:mm")) }
    val date = remember(now.dayOfYear) { now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")) }

    val wallpaper = Wallpapers[LauncherState.wallpaperIndex.coerceIn(0, Wallpapers.size - 1)]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(wallpaper)
            .pointerInput(Unit) { detectTapGestures { onUnlock() } }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -8f) onUnlock()
                }
            },
    ) {
        // Legibility scrim.
        Box(Modifier.fillMaxSize().background(Color(0x55000000)))

        // Clock cluster, upper third (Windows 11 lock style).
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(time, color = Color.White, fontSize = 84.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(4.dp))
            Text(date, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Normal)
        }

        // Brand + unlock hint, bottom.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(Brand.NAME, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(
                "Swipe up or tap to unlock",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
        }
    }
}
