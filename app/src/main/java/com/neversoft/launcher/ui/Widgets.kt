package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.modifier.acrylic
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Widgets board (Win+W): slides from the left with weather/clock/calendar/news. */
@Composable
fun WidgetsBoard(onDismiss: () -> Unit) {
    val now = remember { LocalDateTime.now() }
    val time = remember { now.format(DateTimeFormatter.ofPattern("h:mm a")) }
    val date = remember { now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Scrim)
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .padding(8.dp)
                .width(320.dp)
                .acrylic(tint = NsColor.AcrylicStart, radius = NsDim.RadiusOverlay)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WidgetCard("Weather", "72°  Sunny", "Seattle  ·  H:75°  L:58°")
            WidgetCard("Clock", time, date)
            WidgetCard("Calendar", date, "No events today")
            WidgetCard("News", "Top stories", "Your personalized feed")
        }
    }
}

@Composable
private fun WidgetCard(title: String, big: String, sub: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusOverlay))
            .background(NsColor.ControlActive)
            .padding(16.dp),
    ) {
        Text(title, color = NsColor.TextTertiary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Text(big, color = NsColor.Text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(sub, color = NsColor.TextSecondary, fontSize = 12.sp)
    }
}
