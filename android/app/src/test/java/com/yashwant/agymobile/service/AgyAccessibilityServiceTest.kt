package com.yashwant.agymobile.service

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AgyAccessibilityServiceTest {

    @Test
    fun testScreenElementToJson() {
        val element = ScreenElement(
            text = "Submit Button",
            contentDescription = "Tap to submit form",
            className = "android.widget.Button",
            boundsLeft = 100,
            boundsTop = 200,
            boundsRight = 300,
            boundsBottom = 400,
            isClickable = true,
            isEditable = false,
            isScrollable = false
        )

        val json = element.toJson()
        assertEquals("Submit Button", json.getString("text"))
        assertEquals("Tap to submit form", json.getString("contentDescription"))
        assertEquals("android.widget.Button", json.getString("className"))
        assertTrue(json.getBoolean("isClickable"))
        assertFalse(json.getBoolean("isEditable"))

        val boundsObj = json.getJSONObject("bounds")
        assertEquals(100, boundsObj.getInt("left"))
        assertEquals(200, boundsObj.getInt("top"))
        assertEquals(300, boundsObj.getInt("right"))
        assertEquals(400, boundsObj.getInt("bottom"))
        assertEquals(200, boundsObj.getInt("centerX"))
        assertEquals(300, boundsObj.getInt("centerY"))
    }

    @Test
    fun testExecuteRemoteActionWhenServiceInactive() {
        // When service is not running / inactive, it must safely return error message
        val args = JSONObject().apply {
            put("x", 500)
            put("y", 1000)
        }
        val result = AgyAccessibilityService.executeRemoteAction("device_click", args)
        assertFalse("Action should fail safely when service is inactive", result.getBoolean("success"))
        assertTrue("Error should guide user to accessibility settings", result.getString("error").contains("Accessibility Settings"))
    }

    @Test
    fun testExecuteRemoteActionUnknownAction() {
        val args = JSONObject()
        val result = AgyAccessibilityService.executeRemoteAction("invalid_action", args)
        assertFalse(result.getBoolean("success"))
    }
}
