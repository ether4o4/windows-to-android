package com.neversoft.launcher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.theme.NsColor
import kotlinx.coroutines.delay

/**
 * A split-second "NeverSoft Services" branded splash shown once at boot, above
 * everything else. Fades itself out after a brief hold and then calls [onDone].
 * Purely cosmetic — it never blocks the shell from composing underneath.
 */
@Composable
fun NeverSoftSplash(onDone: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    val fade by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "splashFade",
    )

    LaunchedEffect(Unit) {
        delay(900)          // "pops up for a split second"
        visible = false
        delay(340)          // let the fade finish
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(fade)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0F1C), Color(0xFF060A14)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF39E7FF), Color(0xFF0078D4)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("N", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "NeverSoft Services",
                color = Color(0xFFEAF6FF),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            // Subtle indeterminate bar for the "loading" beat.
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x22FFFFFF)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                        .background(NsColor.Accent),
                )
            }
        }
    }
}
