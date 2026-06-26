package com.neversoft.launcher.apps

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

/** One launchable app on the device. */
data class AppEntry(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap?,
)

/** Reads installed launchable apps and launches them (doctrine §2.1 step 6). */
object AppRepository {

    const val TERMUX_PACKAGE = "com.termux"

    fun loadApps(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val resolved = pm.queryIntentActivities(intent, 0)
        return resolved.asSequence()
            .mapNotNull { ri ->
                val info = ri.activityInfo ?: return@mapNotNull null
                val pkg = info.packageName ?: return@mapNotNull null
                if (pkg == context.packageName) return@mapNotNull null // hide ourselves
                val label = ri.loadLabel(pm)?.toString().orEmpty().ifBlank { pkg }
                val icon = runCatching {
                    ri.loadIcon(pm).toBitmap(width = 96, height = 96).asImageBitmap()
                }.getOrNull()
                AppEntry(label, pkg, icon)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun launch(context: Context, packageName: String) {
        val launch = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
        } else {
            Toast.makeText(context, "Can't open this app", Toast.LENGTH_SHORT).show()
        }
    }

    /** Opens the Termux "Command Prompt"; returns false if Termux isn't installed. */
    fun launchTermux(context: Context): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
        if (intent == null) {
            Toast.makeText(
                context,
                "Install Termux to use the Command Prompt",
                Toast.LENGTH_LONG,
            ).show()
            return false
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }
}
