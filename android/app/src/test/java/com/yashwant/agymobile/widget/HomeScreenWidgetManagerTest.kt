package com.yashwant.agymobile.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenWidgetManagerTest {

    @Test
    fun testWidgetTypeProperties() {
        val types = WidgetType.values()
        assertEquals(3, types.size)

        for (type in types) {
            assertTrue("Title should not be blank for $type", type.title.isNotBlank())
            assertTrue("Description should not be blank for $type", type.description.isNotBlank())
        }

        assertEquals("AI Prompt Pill", WidgetType.PROMPT_PILL.title)
        assertEquals("Quick Actions Grid", WidgetType.ACTIONS_GRID.title)
        assertEquals("Workspace Status Card", WidgetType.STATUS_CARD.title)
    }

    @Test
    fun testWidgetReceiversExist() {
        assertNotNull(AgyPromptWidgetReceiver())
        assertNotNull(AgyActionsWidgetReceiver())
        assertNotNull(AgyStatusWidgetReceiver())
    }
}
