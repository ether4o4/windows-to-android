package com.neversoft.launcher

/**
 * Single source of truth for all NeverSoft Services branding (doctrine §1.6).
 * Change a string here and it propagates everywhere — Start, CMD banner, About.
 */
object Brand {
    const val NAME = "NeverSoft Services"
    const val SHORT = "NeverSoft 11"
    const val ORG = "NeverSoft Services"
    const val VERSION = "11.0.22631.0"
    const val COPYRIGHT = "(c) NeverSoft Services. All rights reserved."

    /** Default account name, also used to build the C:\Users\<user> CMD prompt. */
    const val USER = "redma"

    /**
     * Keep the literal word "Windows" for CMD cadence? Doctrine default: drop it
     * (cleaner, removes trademark exposure). Flip in one place if desired.
     */
    const val KEEP_WINDOWS_WORD = false

    /** CMD banner line 1, e.g. `NeverSoft Services [Version 11.0.22631.0]`. */
    val cmdBannerLine1: String
        get() = if (KEEP_WINDOWS_WORD) {
            "$NAME Windows [Version $VERSION]"
        } else {
            "$NAME [Version $VERSION]"
        }

    const val cmdBannerLine2: String = COPYRIGHT
}
