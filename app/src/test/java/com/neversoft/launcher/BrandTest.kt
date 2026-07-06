package com.neversoft.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure-JVM checks for the single-source-of-truth rebrand (doctrine §1.6). */
class BrandTest {

    @Test
    fun bannerHasNoMicrosoftBranding() {
        val banner = "${Brand.cmdBannerLine1}\n${Brand.cmdBannerLine2}"
        assertFalse("must not mention Microsoft", banner.contains("Microsoft", ignoreCase = true))
        assertTrue(banner.contains(Brand.NAME))
        assertTrue(banner.contains(Brand.VERSION))
    }

    @Test
    fun bannerDropsWindowsWordByDefault() {
        // Default doctrine choice: KEEP_WINDOWS_WORD = false.
        assertEquals("NeverSoft Services [Version ${Brand.VERSION}]", Brand.cmdBannerLine1)
    }

    @Test
    fun copyrightIsRebranded() {
        assertEquals("(c) NeverSoft Services. All rights reserved.", Brand.COPYRIGHT)
    }
}
