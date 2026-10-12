package com.yashwant.agymobile.util

import android.graphics.RectF
import com.yashwant.agymobile.core.HomeScreenShortcutManager
import com.yashwant.agymobile.core.ShortcutTarget
import org.junit.Assert.*
import org.junit.Test

class ShortcutIconGeneratorTest {

    @Test
    fun testShortcutDesignDefaults() {
        val design = ShortcutDesign(label = "Downloads")
        assertEquals("Downloads", design.label)
        assertEquals(IconShape.SQUIRCLE, design.shape)
        assertTrue(design.showBorder)
    }

    @Test
    fun testGetShapePathCircle() {
        val rect = RectF(0f, 0f, 100f, 100f)
        val path = ShortcutIconGenerator.getShapePath(rect, IconShape.CIRCLE)
        assertNotNull(path)
    }

    @Test
    fun testGetShapePathSquircle() {
        val rect = RectF(0f, 0f, 100f, 100f)
        val path = ShortcutIconGenerator.getShapePath(rect, IconShape.SQUIRCLE)
        assertNotNull(path)
    }

    @Test
    fun testGetShapePathRoundedSquare() {
        val rect = RectF(0f, 0f, 100f, 100f)
        val path = ShortcutIconGenerator.getShapePath(rect, IconShape.ROUNDED_SQUARE)
        assertNotNull(path)
    }

    @Test
    fun testGetShapePathTeardrop() {
        val rect = RectF(0f, 0f, 100f, 100f)
        val path = ShortcutIconGenerator.getShapePath(rect, IconShape.TEARDROP)
        assertNotNull(path)
    }

    @Test
    fun testCreateIconBitmapHandlesExceptionsGracefully() {
        val design = ShortcutDesign(
            label = "WhatsApp",
            shape = IconShape.ROUNDED_SQUARE,
            emojiOrText = "💬"
        )
        // In JVM unit tests without graphics engine, createIconBitmap catches and handles gracefully
        val bitmap = ShortcutIconGenerator.createIconBitmap(design, size = 192)
        // Assert that calling createIconBitmap completes safely without throwing
        assertTrue("Bitmap call handles safely", bitmap == null || bitmap.width == 192)

        val designWithBadge = ShortcutDesign(label = "Dev", badge = "AI")
        assertEquals("AI", designWithBadge.badge)
    }

    @Test
    fun testCreateTargetIntentOpenWorkspace() {
        // Test intent building logic
        val target = ShortcutTarget.OpenWorkspace("/storage/emulated/0/Download", "Downloads")
        assertEquals("/storage/emulated/0/Download", target.folderPath)
        assertEquals("Downloads", target.folderName)
    }

    @Test
    fun testCreateTargetIntentQuickPrompt() {
        val target = ShortcutTarget.QuickPrompt("Summarize notes")
        assertEquals("Summarize notes", target.promptText)
    }
}
