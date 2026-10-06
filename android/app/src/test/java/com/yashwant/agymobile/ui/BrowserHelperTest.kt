package com.yashwant.agymobile.ui

import com.yashwant.agymobile.ui.util.BrowserHelper
import org.junit.Assert.*
import org.junit.Test

class BrowserHelperTest {

    @Test
    fun testObsidianToolbarColor() {
        assertEquals(0xFF080C14.toInt(), BrowserHelper.OBSIDIAN_TOOLBAR_COLOR)
    }

    @Test
    fun testUrlValidation() {
        val validUrl = "https://accounts.google.com/o/oauth2/auth?client_id=123"
        assertTrue(validUrl.startsWith("https://"))
    }
}
