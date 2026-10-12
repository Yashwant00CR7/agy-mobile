package com.yashwant.agymobile.util

import com.yashwant.agymobile.core.HomeScreenShortcutManager
import com.yashwant.agymobile.core.ShortcutTarget
import com.yashwant.agymobile.ui.components.ShortcutPreset
import com.yashwant.agymobile.widget.WidgetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenStudioStateTest {

    @Test
    fun testShortcutPresets() {
        val preset = ShortcutPreset(
            name = "New Chat",
            target = ShortcutTarget.NewChat,
            emoji = "💬",
            defaultLabel = "AGY Chat"
        )

        assertEquals("New Chat", preset.name)
        assertEquals("💬", preset.emoji)
        assertEquals("AGY Chat", preset.defaultLabel)
        assertTrue(preset.target is ShortcutTarget.NewChat)
    }

    @Test
    fun testShortcutIntentConstants() {
        assertEquals("com.yashwant.agymobile.ACTION_OPEN_WORKSPACE", HomeScreenShortcutManager.ACTION_OPEN_WORKSPACE)
        assertEquals("EXTRA_WORKSPACE_PATH", HomeScreenShortcutManager.EXTRA_WORKSPACE_PATH)
        assertEquals("com.yashwant.agymobile.ACTION_QUICK_PROMPT", HomeScreenShortcutManager.ACTION_QUICK_PROMPT)
        assertEquals("EXTRA_PROMPT_TEXT", HomeScreenShortcutManager.EXTRA_PROMPT_TEXT)
        assertEquals("com.yashwant.agymobile.ACTION_NEW_CHAT", HomeScreenShortcutManager.ACTION_NEW_CHAT)
        assertEquals("com.yashwant.agymobile.ACTION_START_VOICE", HomeScreenShortcutManager.ACTION_START_VOICE)
    }

    @Test
    fun testWidgetTypeProportionsAndDimensions() {
        for (type in WidgetType.values()) {
            assertNotNull(type.standardDimensions)
            assertTrue(type.compactProportion.isNotBlank())
            assertTrue(type.expandedProportion.isNotBlank())
        }

        assertEquals("3x1 / 2x1", WidgetType.PROMPT_PILL.standardDimensions)
        assertEquals("Standard Pill (3x1)", WidgetType.PROMPT_PILL.compactProportion)
        assertEquals("Compact Pill (2x1)", WidgetType.PROMPT_PILL.expandedProportion)

        assertEquals("4x1 / 3x2", WidgetType.ACTIONS_GRID.standardDimensions)
        assertEquals("Single Row (4x1)", WidgetType.ACTIONS_GRID.compactProportion)
        assertEquals("Expanded Grid (3x2)", WidgetType.ACTIONS_GRID.expandedProportion)

        assertEquals("4x2 / 3x2", WidgetType.STATUS_CARD.standardDimensions)
        assertEquals("Compact (3x2)", WidgetType.STATUS_CARD.compactProportion)
        assertEquals("Full Card (4x2)", WidgetType.STATUS_CARD.expandedProportion)
    }

    @Test
    fun testIconShapesEnum() {
        val shapes = IconShape.values()
        assertEquals(4, shapes.size)
        assertTrue(shapes.contains(IconShape.CIRCLE))
        assertTrue(shapes.contains(IconShape.SQUIRCLE))
        assertTrue(shapes.contains(IconShape.ROUNDED_SQUARE))
        assertTrue(shapes.contains(IconShape.TEARDROP))
    }
}
