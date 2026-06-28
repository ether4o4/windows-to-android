package com.neversoft.launcher.ui.apps

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import java.io.File

@Composable
fun NotepadApp() {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Solid)
    ) {
        // Top toolbar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NsColor.Mica)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            ToolbarButton(
                label = "New",
                icon = Icons.Filled.Add,
                onClick = { text = "" }
            )
            Spacer(Modifier.width(8.dp))
            ToolbarButton(
                label = "Save",
                icon = Icons.Filled.Send,
                onClick = {
                    val ok = runCatching {
                        val dir = context.getExternalFilesDir(null)
                        val file = File(dir, "NeverSoft-Notepad.txt")
                        file.writeText(text)
                        true
                    }.getOrDefault(false)
                    val msg = if (ok) "Saved to NeverSoft-Notepad.txt" else "Save failed"
                    runCatching {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Main editor.
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(12.dp),
            textStyle = TextStyle(
                color = NsColor.Text,
                fontSize = 14.sp
            ),
            cursorBrush = SolidColor(LauncherState.accent)
        )

        // Bottom status bar.
        val charCount = text.length
        val wordCount = text.split(Regex("\\s+")).count { it.isNotBlank() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NsColor.Mica)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            androidx.compose.material3.Text(
                text = "Words: $wordCount",
                color = NsColor.TextSecondary,
                fontSize = 12.sp
            )
            Spacer(Modifier.width(16.dp))
            androidx.compose.material3.Text(
                text = "Characters: $charCount",
                color = NsColor.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(NsColor.ControlActive)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = label,
            tint = NsColor.Text,
            modifier = Modifier.width(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        androidx.compose.material3.Text(
            text = label,
            color = NsColor.Text,
            fontSize = 13.sp
        )
    }
}
