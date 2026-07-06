package com.neversoft.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.neversoft.launcher.ui.Shell
import com.neversoft.launcher.ui.theme.NeverSoftTheme

/**
 * The launcher entry point. Declared as HOME in the manifest, so this is the
 * Android home screen. Draws edge-to-edge and hides the system bars so our own
 * taskbar/tray own the chrome (doctrine §4.1, §5 gap workaround).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            NeverSoftTheme {
                Shell()
            }
        }
    }
}
