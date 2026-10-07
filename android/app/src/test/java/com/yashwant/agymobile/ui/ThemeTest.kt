package com.yashwant.agymobile.ui

import com.yashwant.agymobile.ui.theme.ElectricCyan
import com.yashwant.agymobile.ui.theme.ObsidianBlack
import com.yashwant.agymobile.ui.theme.AntigravityViolet
import org.junit.Assert.*
import org.junit.Test

class ThemeTest {

    @Test
    fun testPaletteConstants() {
        assertNotNull(ObsidianBlack)
        assertNotNull(ElectricCyan)
        assertNotNull(AntigravityViolet)

        // Verify RGB channels for ObsidianBlack (0x08, 0x0C, 0x14)
        assertEquals(0x08, (ObsidianBlack.red * 255).toInt())
        assertEquals(0x0C, (ObsidianBlack.green * 255).toInt())
        assertEquals(0x14, (ObsidianBlack.blue * 255).toInt())

        // Verify ElectricCyan (0x00, 0xE5, 0xFF)
        assertEquals(0x00, (ElectricCyan.red * 255).toInt())
        assertEquals(0xE5, (ElectricCyan.green * 255).toInt())
        assertEquals(0xFF, (ElectricCyan.blue * 255).toInt())
    }

    @Test
    fun testAgyIcons() {
        assertNotNull(com.yashwant.agymobile.ui.theme.AgyIcons.Logo)
        assertNotNull(com.yashwant.agymobile.ui.theme.AgyIcons.TerminalSpark)
        assertNotNull(com.yashwant.agymobile.ui.theme.AgyIcons.SendPlane)
        assertNotNull(com.yashwant.agymobile.ui.theme.AgyIcons.CyberKey)
    }
}
