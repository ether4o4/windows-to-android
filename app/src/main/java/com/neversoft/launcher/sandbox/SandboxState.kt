package com.neversoft.launcher.sandbox

/** Lifecycle of the Linux sandbox install. */
sealed interface SandboxState {
    data object NotInstalled : SandboxState
    data class Downloading(val progress: Float) : SandboxState
    data object Extracting : SandboxState
    data class Configuring(val message: String) : SandboxState
    data object Ready : SandboxState
    data class Error(val message: String) : SandboxState
}

/** One rendered line in the terminal scrollback. */
sealed interface TerminalLine {
    data class Command(val text: String) : TerminalLine
    data class Output(val text: String) : TerminalLine
    data class Error(val text: String) : TerminalLine
}
