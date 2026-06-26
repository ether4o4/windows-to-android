package com.neversoft.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.modifier.acrylic
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

data class MenuItem(
    val label: String,
    val icon: ImageVector? = null,
    val danger: Boolean = false,
    val separatorAfter: Boolean = false,
    val onClick: () -> Unit,
)

/** Fluent rounded acrylic context menu, positioned at [position] (px). */
@Composable
fun ContextMenu(position: IntOffset, items: List<MenuItem>, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
    ) {
        Column(
            modifier = Modifier
                .offset { position }
                .width(240.dp)
                .acrylic(tint = NsColor.AcrylicFlyout, radius = NsDim.RadiusOverlay)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(vertical = 6.dp, horizontal = 6.dp),
        ) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NsDim.RadiusControl))
                        .clickable { item.onClick(); onDismiss() }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.icon != null) {
                        Icon(
                            item.icon, null,
                            tint = if (item.danger) NsColor.Danger else NsColor.TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                    } else {
                        Spacer(Modifier.width(28.dp))
                    }
                    Text(
                        item.label,
                        color = if (item.danger) NsColor.Danger else NsColor.Text,
                        fontSize = 13.sp,
                    )
                }
                if (item.separatorAfter) {
                    HorizontalDivider(
                        color = NsColor.Stroke,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    )
                }
            }
        }
    }
}
