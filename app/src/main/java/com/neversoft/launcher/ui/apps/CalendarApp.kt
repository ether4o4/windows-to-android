package com.neversoft.launcher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarApp() {
    val today = remember { LocalDate.now() }
    var visibleMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selected by remember { mutableStateOf(today) }

    val accent = LauncherState.accent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Solid)
            .padding(20.dp)
    ) {
        // Header: month + year with prev/next chevrons.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val monthName = visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            Text(
                text = "$monthName ${visibleMonth.year}",
                color = NsColor.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            ChevronButton(icon = Icons.Filled.KeyboardArrowLeft, contentDesc = "Previous month") {
                visibleMonth = visibleMonth.minusMonths(1)
            }
            Spacer(Modifier.size(8.dp))
            ChevronButton(icon = Icons.Filled.KeyboardArrowRight, contentDesc = "Next month") {
                visibleMonth = visibleMonth.plusMonths(1)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Weekday header row: S M T W T F S.
        val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { wd ->
                Text(
                    text = wd,
                    color = NsColor.TextTertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Compute leading blanks. DayOfWeek: MONDAY=1..SUNDAY=7. Grid starts Sunday.
        val firstOfMonth = visibleMonth.atDay(1)
        val leadingBlanks = firstOfMonth.dayOfWeek.value % 7 // SUNDAY(7)%7=0, MONDAY(1)=1, ...
        val daysInMonth = visibleMonth.lengthOfMonth()

        // 6x7 grid of day cells.
        Column(modifier = Modifier.fillMaxWidth()) {
            for (row in 0 until 6) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - leadingBlanks + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNumber in 1..daysInMonth) {
                                val date = visibleMonth.atDay(dayNumber)
                                val isToday = date == today
                                val isSelected = date == selected

                                val cellModifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .then(
                                        if (isToday) Modifier.background(accent)
                                        else Modifier
                                    )
                                    .then(
                                        if (isSelected && !isToday)
                                            Modifier.border(1.5.dp, accent, CircleShape)
                                        else if (isSelected && isToday)
                                            Modifier.border(1.5.dp, NsColor.Text, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selected = date }

                                Box(
                                    modifier = cellModifier,
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = if (isToday) NsColor.Text else NsColor.TextSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isToday || isSelected) FontWeight.SemiBold
                                        else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Selected date long-form.
        val longForm = formatLongDate(selected)
        Text(
            text = longForm,
            color = NsColor.Text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        // "No events" placeholder card.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NsDim.RadiusOverlay))
                .background(NsColor.ControlActive)
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "No events",
                color = NsColor.TextTertiary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ChevronButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(NsColor.ControlActive)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = NsColor.Text,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun formatLongDate(date: LocalDate): String {
    val locale = Locale.getDefault()
    val dow = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val month = date.month.getDisplayName(TextStyle.FULL, locale)
    return "$dow, $month ${date.dayOfMonth}, ${date.year}"
}
