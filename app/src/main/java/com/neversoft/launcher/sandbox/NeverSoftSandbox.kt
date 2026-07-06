package com.neversoft.launcher.sandbox

import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

/**
 * Self-contained Alpine-on-proot Linux shell for NeverSoft 11, ported from the
 * MorsVitaEst sandbox. First use downloads a ~3 MB Alpine rootfs; after that it
 * runs offline. State is Compose-observable so the terminal UI reacts to setup
 * progress. Commands run one-per-invocation with a tracked working directory
 * (a persistent PTY is a future upgrade — this covers `cd` + line output).
 */
object NeverSoftSandbox {

    var state by mutableStateOf<SandboxState>(SandboxState.NotInstalled)
        private set

    private var appContext: Context? = null
    private val downloader = RootfsDownloader()

    @Volatile
    var cwd: String = "/root"
        private set

    @Volatile
    private var setupThread: Thread? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            refreshState()
        }
    }

    private val ctx: Context get() = appContext ?: error("NeverSoftSandbox not initialized")

    private val sandboxDir: File get() = File(ctx.filesDir, "linux-sandbox")
    private val rootfsPath: String get() = File(sandboxDir, "rootfs").absolutePath
    private val homePath: String
        get() {
            val ext = ctx.getExternalFilesDir(null)
            val target = if (ext != null) File(ext, "sandbox-home") else File(sandboxDir, "home")
            target.mkdirs()
            return target.absolutePath
        }
    private val tmpPath: String get() = File(sandboxDir, "tmp").absolutePath
    private val prootPath: String get() = File(ctx.applicationInfo.nativeLibraryDir, "libproot.so").absolutePath
    private val nativeLibDir: String get() = ctx.applicationInfo.nativeLibraryDir

    private fun refreshState() {
        val rootfs = File(sandboxDir, "rootfs")
        val proot = File(prootPath)
        state = if (rootfs.isDirectory && proot.exists() && proot.canExecute()) {
            SandboxState.Ready
        } else {
            SandboxState.NotInstalled
        }
    }

    private fun linuxArch(): String {
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        return when {
            abi.startsWith("arm64") -> "aarch64"
            abi.startsWith("armeabi") -> "armhf"
            abi.startsWith("x86_64") -> "x86_64"
            abi.startsWith("x86") -> "x86"
            else -> "aarch64"
        }
    }

    /** Kick off (or resume) the one-time install on a background thread. */
    fun setup() {
        if (setupThread?.isAlive == true) return
        if (state is SandboxState.Ready) return
        setupThread = Thread {
            try {
                setupInternal()
            } catch (e: Exception) {
                state = SandboxState.Error(e.message ?: "Setup failed")
            }
        }.apply { isDaemon = true; start() }
    }

    private fun setupInternal() {
        val arch = linuxArch()
        val proot = File(prootPath)
        if (!proot.exists()) {
            throw IllegalStateException(
                "proot not found at $prootPath (nativeLibDir: " +
                    "${File(nativeLibDir).listFiles()?.map { it.name } ?: "empty"})",
            )
        }

        sandboxDir.mkdirs()
        File(sandboxDir, "tmp").mkdirs()
        copyLibtalloc()

        val rootfsDir = File(sandboxDir, "rootfs")
        if (!rootfsDir.isDirectory) {
            val tarGz = File(sandboxDir, "rootfs.tar.gz")
            try {
                state = SandboxState.Downloading(0f)
                downloader.download(arch, tarGz) { p -> state = SandboxState.Downloading(p) }
                state = SandboxState.Extracting
                downloader.extractTarGz(tarGz, rootfsDir)
            } finally {
                tarGz.delete()
            }
        }

        state = SandboxState.Configuring("Configuring…")
        downloader.makeWritable(rootfsDir)
        downloader.writeResolvConf(rootfsDir)

        val executor = createExecutor()
        var updated = false
        for (mirror in downloader.mirrors) {
            downloader.writeRepositories(rootfsDir, mirror)
            val result = executor.execute("apk update", timeoutSeconds = 60)
            if (result["success"] as? Boolean == true) { updated = true; break }
        }
        // apk update is a nice-to-have (lets `apk add` work); a raw shell is
        // usable without it, so don't hard-fail the whole install if it can't
        // reach a mirror — just land Ready.
        state = SandboxState.Ready
        if (!updated) {
            // leave a breadcrumb; not fatal
        }
    }

    private fun copyLibtalloc() {
        val target = File(sandboxDir, "libtalloc.so.2")
        if (target.exists()) return
        val source = File(nativeLibDir, "libtalloc.so")
        if (source.exists()) source.copyTo(target, overwrite = true)
    }

    private fun createExecutor(): ProotExecutor = ProotExecutor(
        prootPath = prootPath,
        libDir = sandboxDir.absolutePath,
        rootfsPath = rootfsPath,
        homePath = homePath,
        tmpPath = tmpPath,
    )

    /**
     * Run one interactive command, streaming stdout/stderr line-by-line.
     * `cd` is handled here so the working directory persists between commands.
     * Returns a handle to cancel, or null if it was a handled `cd`.
     */
    fun run(
        command: String,
        onStdout: (String) -> Unit,
        onStderr: (String) -> Unit,
        onExit: (Int) -> Unit,
    ): ProotHandle? {
        val trimmed = command.trim()
        if (trimmed == "cd" || trimmed.startsWith("cd ")) {
            handleCd(trimmed, onStderr)
            onExit(0)
            return null
        }
        val executor = createExecutor()
        val handle = executor.executeStreaming(
            command = command,
            workingDir = cwd,
            onStdout = onStdout,
            onStderr = onStderr,
        )
        Thread {
            val code = handle.awaitExit()
            onExit(code)
        }.apply { isDaemon = true; start() }
        return handle
    }

    private fun handleCd(command: String, onStderr: (String) -> Unit) {
        val arg = command.removePrefix("cd").trim().ifEmpty { "/root" }
        // Resolve the target canonically inside the sandbox from the current cwd.
        val probe = createExecutor().execute(
            "cd ${shellQuote(cwd)} && cd ${shellQuote(arg)} && pwd",
            timeoutSeconds = 15,
        )
        val ok = probe["success"] as? Boolean == true
        val out = (probe["stdout"] as? String).orEmpty().trim().lineSequence().lastOrNull().orEmpty()
        if (ok && out.startsWith("/")) {
            cwd = out
        } else {
            val err = (probe["stderr"] as? String).orEmpty().trim()
            onStderr(if (err.isNotEmpty()) err else "cd: $arg: No such file or directory")
        }
    }

    private fun shellQuote(s: String): String = "'" + s.replace("'", "'\\''") + "'"

    fun reset() {
        setupThread?.interrupt()
        setupThread = null
        Thread {
            runCatching { sandboxDir.deleteRecursively() }
            cwd = "/root"
            state = SandboxState.NotInstalled
        }.apply { isDaemon = true; start() }
    }
}
