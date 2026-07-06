package com.neversoft.launcher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.sandbox.NeverSoftSandbox
import com.neversoft.launcher.sandbox.ProotHandle
import com.neversoft.launcher.sandbox.SandboxState
import com.neversoft.launcher.sandbox.TerminalLine
import com.neversoft.launcher.ui.theme.NsColor

private const val PROMPT = "NeverSoft%"
private const val MAX_LINES = 600

/**
 * The holographic NeverSoft shell — a see-through terminal (wallpaper glows
 * through the translucent body) backed by the ported Alpine-on-proot engine.
 * Colors read [NsColor] holo tokens, so it tracks the taskbar theme.
 */
@Composable
fun TerminalApp() {
    val context = LocalContext.current
    LaunchedEffect(Unit) { NeverSoftSandbox.init(context) }

    when (val s = NeverSoftSandbox.state) {
        is SandboxState.Ready -> TerminalConsole()
        else -> SetupPanel(s)
    }
}

@Composable
private fun SetupPanel(state: SandboxState) {
    val glow = TextStyle(
        fontFamily = FontFamily.Monospace,
        shadow = Shadow(color = NsColor.HoloAccent, blurRadius = 14f, offset = Offset.Zero),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.HoloBody)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("◇ NeverSoft Shell", color = NsColor.HoloAccent, fontSize = 18.sp, style = glow)
        Spacer(Modifier.height(10.dp))
        when (state) {
            is SandboxState.NotInstalled -> {
                Text(
                    "First run installs a small Linux image (~3 MB) so the shell " +
                        "works offline afterward.",
                    color = NsColor.HoloDim, fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.height(16.dp))
                HoloButton("Initialize") { NeverSoftSandbox.setup() }
            }
            is SandboxState.Downloading -> {
                Text("Downloading Linux image…", color = NsColor.HoloText, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { state.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = NsColor.HoloAccent,
                    trackColor = NsColor.HoloDim,
                )
                Spacer(Modifier.height(6.dp))
                Text("${(state.progress * 100).toInt()}%", color = NsColor.HoloDim, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            is SandboxState.Extracting -> ProgressText("Extracting…")
            is SandboxState.Configuring -> ProgressText(state.message)
            is SandboxState.Error -> {
                Text("Setup failed", color = NsColor.HoloError, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(6.dp))
                Text(state.message, color = NsColor.HoloDim, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(16.dp))
                HoloButton("Retry") { NeverSoftSandbox.setup() }
            }
            is SandboxState.Ready -> Unit
        }
    }
}

@Composable
private fun ProgressText(msg: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            color = NsColor.HoloAccent,
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.width(10.dp))
        Text(msg, color = NsColor.HoloText, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun HoloButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NsColor.HoloBody)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
    ) {
        Text(label, color = NsColor.HoloAccent, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun TerminalConsole() {
    val lines = remember { mutableStateListOf<TerminalLine>() }
    var input by remember { mutableStateOf("") }
    var running by remember { mutableStateOf(false) }
    var handle by remember { mutableStateOf<ProotHandle?>(null) }
    val listState = rememberLazyListState()

    fun append(line: TerminalLine) {
        lines.add(line)
        while (lines.size > MAX_LINES) lines.removeAt(0)
    }

    fun submit() {
        val cmd = input.trim()
        if (cmd.isEmpty() || running) return
        input = ""
        append(TerminalLine.Command(cmd))
        running = true
        handle = NeverSoftSandbox.run(
            command = cmd,
            onStdout = { append(TerminalLine.Output(stripAnsi(it))) },
            onStderr = { append(TerminalLine.Error(stripAnsi(it))) },
            onExit = { running = false; handle = null },
        )
    }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) runCatching { listState.animateScrollToItem(lines.size - 1) }
    }

    val monoGlow = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        shadow = Shadow(color = NsColor.HoloAccent, blurRadius = 8f, offset = Offset.Zero),
    )

    Column(Modifier.fillMaxSize().background(NsColor.HoloBody).imePadding()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (lines.isEmpty()) {
                item {
                    Text(
                        "NeverSoft Shell · type a command",
                        color = NsColor.HoloDim, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    )
                }
            }
            items(lines.size) { i ->
                when (val line = lines[i]) {
                    is TerminalLine.Command -> Row {
                        Text("$PROMPT ", color = NsColor.HoloAccent, style = monoGlow)
                        Text(line.text, color = NsColor.HoloText, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }
                    is TerminalLine.Output -> Text(
                        line.text, color = NsColor.HoloText, fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                    )
                    is TerminalLine.Error -> Text(
                        line.text, color = NsColor.HoloError, fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                    )
                }
            }
            if (running) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.size(13.dp).padding(top = 4.dp),
                        color = NsColor.HoloAccent, strokeWidth = 2.dp,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(NsColor.HoloBody, NsColor.HoloFrameBottom)))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$PROMPT ", color = NsColor.HoloAccent, style = monoGlow)
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(color = NsColor.HoloText, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                cursorBrush = SolidColor(NsColor.HoloAccent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { submit() }),
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        if (running) { handle?.cancel(); running = false; handle = null } else submit()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (running) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (running) "Stop" else "Run",
                    tint = NsColor.HoloAccent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/** Strip ANSI CSI/OSC escape sequences to plain text. */
private val ANSI_ESCAPE = Regex("\u001B\\[[0-9;?]*[ -/]*[@-~]|\u001B\\][^\u0007\u001B]*[\u0007\u001B]?")

private fun stripAnsi(s: String): String = s.replace(ANSI_ESCAPE, "").replace("\r", "")
