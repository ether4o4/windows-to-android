package com.neversoft.launcher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

private data class HourSlot(val label: String, val temp: Int, val icon: ImageVector)
private data class DayForecast(
    val day: String,
    val icon: ImageVector,
    val hi: Int,
    val lo: Int,
)

@Composable
fun WeatherApp() {
    val accent = LauncherState.accent

    val hours = listOf(
        HourSlot("Now", 72, Icons.Filled.WbSunny),
        HourSlot("1 PM", 73, Icons.Filled.WbSunny),
        HourSlot("2 PM", 75, Icons.Filled.WbSunny),
        HourSlot("3 PM", 74, Icons.Filled.WbSunny),
        HourSlot("4 PM", 71, Icons.Filled.WbSunny),
        HourSlot("5 PM", 68, Icons.Filled.Star),
        HourSlot("6 PM", 64, Icons.Filled.Star),
        HourSlot("7 PM", 61, Icons.Filled.Star),
    )

    val days = listOf(
        DayForecast("Today", Icons.Filled.WbSunny, 75, 58),
        DayForecast("Sat", Icons.Filled.WbSunny, 78, 60),
        DayForecast("Sun", Icons.Filled.Star, 71, 55),
        DayForecast("Mon", Icons.Filled.WbSunny, 69, 53),
        DayForecast("Tue", Icons.Filled.WbSunny, 73, 56),
    )

    // Range over the whole week for the forecast bars.
    val weekLow = days.minOf { it.lo }
    val weekHigh = days.maxOf { it.hi }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Solid)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeroCard(accent = accent)
        HourlyStrip(hours = hours, accent = accent)
        ForecastList(
            days = days,
            weekLow = weekLow,
            weekHigh = weekHigh,
            accent = accent,
        )
    }
}

@Composable
private fun HeroCard(accent: Color) {
    val gradient = Brush.linearGradient(
        listOf(
            accent.copy(alpha = 0.95f),
            lighten(accent, 0.22f).copy(alpha = 0.92f),
            darken(accent, 0.35f),
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .padding(24.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = "Seattle, WA",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Sunny",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "72°",
                color = Color.White,
                fontSize = 88.sp,
                fontWeight = FontWeight.Light,
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "H:75°",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "L:58°",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun HourlyStrip(hours: List<HourSlot>, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusOverlay))
            .background(NsColor.ControlActive)
            .padding(vertical = 14.dp),
    ) {
        Text(
            text = "Hourly forecast",
            color = NsColor.TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            hours.forEach { slot ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NsDim.RadiusControl))
                        .background(NsColor.ControlSelected)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = slot.label,
                        color = NsColor.TextSecondary,
                        fontSize = 12.sp,
                    )
                    Icon(
                        imageVector = slot.icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = "${slot.temp}°",
                        color = NsColor.Text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastList(
    days: List<DayForecast>,
    weekLow: Int,
    weekHigh: Int,
    accent: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusOverlay))
            .background(NsColor.ControlActive)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "5-day forecast",
                color = NsColor.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = NsColor.TextTertiary,
                modifier = Modifier.size(18.dp),
            )
        }

        days.forEach { day ->
            ForecastRow(
                day = day,
                weekLow = weekLow,
                weekHigh = weekHigh,
                accent = accent,
            )
        }
    }
}

@Composable
private fun ForecastRow(
    day: DayForecast,
    weekLow: Int,
    weekHigh: Int,
    accent: Color,
) {
    val span = (weekHigh - weekLow).coerceAtLeast(1).toFloat()
    val startFrac = ((day.lo - weekLow).toFloat() / span).coerceIn(0f, 1f)
    val endFrac = ((day.hi - weekLow).toFloat() / span).coerceIn(0f, 1f)
    val barFrac = (endFrac - startFrac).coerceIn(0.06f, 1f)
    val leadFrac = startFrac.coerceIn(0f, 0.94f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = day.day,
            color = NsColor.Text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(56.dp),
        )
        Icon(
            imageVector = day.icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "${day.lo}°",
            color = NsColor.TextTertiary,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(34.dp),
        )
        Spacer(Modifier.width(10.dp))

        // Range bar track.
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(NsColor.StrokeStrong),
        ) {
            // Lead spacer + filled segment expressed via two weighted boxes.
            Row(modifier = Modifier.fillMaxSize()) {
                if (leadFrac > 0f) {
                    Spacer(Modifier.weight(leadFrac))
                }
                Box(
                    modifier = Modifier
                        .weight(barFrac)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(lighten(accent, 0.18f), accent),
                            ),
                        ),
                )
                val tail = (1f - leadFrac - barFrac).coerceAtLeast(0f)
                if (tail > 0f) {
                    Spacer(Modifier.weight(tail))
                }
            }
        }

        Spacer(Modifier.width(10.dp))
        Text(
            text = "${day.hi}°",
            color = NsColor.Text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(34.dp),
        )
    }
}

private fun lighten(c: Color, amount: Float): Color = Color(
    red = c.red + (1f - c.red) * amount,
    green = c.green + (1f - c.green) * amount,
    blue = c.blue + (1f - c.blue) * amount,
    alpha = c.alpha,
)

private fun darken(c: Color, amount: Float): Color = Color(
    red = c.red * (1f - amount),
    green = c.green * (1f - amount),
    blue = c.blue * (1f - amount),
    alpha = c.alpha,
)
