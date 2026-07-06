package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.Brand
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class LockStage { Lock, SignIn }

/**
 * Windows 11-style two-stage lock: the lock screen (big clock bottom-left over
 * the wallpaper, tray status, swipe-up / tap to advance) → the sign-in screen
 * (circular avatar, user name, PIN dots + numeric keypad). Uses the user's
 * selected wallpaper. Unlock is cosmetic — any PIN signs in.
 */
@Composable
fun LockScreen(onUnlock: () -> Unit) {
    var stage by remember { mutableStateOf(LockStage.Lock) }
    val wallpaper = Wallpapers[LauncherState.wallpaperIndex.coerceIn(0, Wallpapers.size - 1)]

    Box(Modifier.fillMaxSize().background(wallpaper)) {
        when (stage) {
            LockStage.Lock -> LockStageView(onAdvance = { stage = LockStage.SignIn })
            LockStage.SignIn -> SignInView(onUnlock = onUnlock, onBack = { stage = LockStage.Lock })
        }
    }
}

@Composable
private fun LockStageView(onAdvance: () -> Unit) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); delay(1000) } }
    val time = remember(now.minute) { now.format(DateTimeFormatter.ofPattern("h:mm")) }
    val date = remember(now.dayOfYear) { now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x33000000))
            .pointerInput(Unit) { detectTapGestures { onAdvance() } }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dy -> if (dy < -6f) onAdvance() }
            },
    ) {
        // Tray status, top-right.
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Filled.Wifi, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Icon(Icons.Filled.BatteryFull, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }

        // Clock, bottom-left (Windows 11 placement).
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 36.dp, bottom = 72.dp)) {
            Text(time, color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.Light)
            Text(date, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Normal)
        }

        // Swipe-up hint, bottom-center.
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Filled.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
            Text("Swipe up to unlock", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun SignInView(onUnlock: () -> Unit, onBack: () -> Unit) {
    var pin by remember { mutableStateOf("") }

    fun digit(d: String) {
        if (pin.length < 4) {
            pin += d
            if (pin.length == 4) onUnlock()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)) // sign-in dims the wallpaper
            .pointerInput(Unit) { detectVerticalDragGestures { _, dy -> if (dy > 8f) onBack() } },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Avatar.
            Box(
                modifier = Modifier.size(96.dp).clip(CircleShape).background(LauncherState.accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(Brand.USER, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(20.dp))

            // PIN dots.
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (i < pin.length) Color.White else Color.White.copy(alpha = 0.28f)),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Enter PIN", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(Modifier.height(20.dp))

            // Numeric keypad.
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        row.forEach { d -> KeypadKey(label = d) { digit(d) } }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    KeypadKey(icon = Icons.Filled.Backspace, enabled = pin.isNotEmpty()) { pin = pin.dropLast(1) }
                    KeypadKey(label = "0") { digit("0") }
                    KeypadKey(icon = Icons.Filled.ArrowForward, accent = true) { onUnlock() }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    label: String? = null,
    icon: ImageVector? = null,
    accent: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(if (accent) LauncherState.accent else Color.White.copy(alpha = 0.12f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            icon != null -> Icon(
                icon, null,
                tint = if (enabled) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp),
            )
            label != null -> Text(label, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
        }
    }
}
