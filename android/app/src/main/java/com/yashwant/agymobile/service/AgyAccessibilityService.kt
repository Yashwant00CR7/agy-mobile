package com.yashwant.agymobile.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONObject

data class ScreenElement(
    val text: String?,
    val contentDescription: String?,
    val className: String,
    val boundsLeft: Int,
    val boundsTop: Int,
    val boundsRight: Int,
    val boundsBottom: Int,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isScrollable: Boolean
) {
    val centerX: Int get() = (boundsLeft + boundsRight) / 2
    val centerY: Int get() = (boundsTop + boundsBottom) / 2

    fun toJson(): JSONObject = JSONObject().apply {
        put("text", text ?: "")
        put("contentDescription", contentDescription ?: "")
        put("className", className)
        put("bounds", JSONObject().apply {
            put("left", boundsLeft)
            put("top", boundsTop)
            put("right", boundsRight)
            put("bottom", boundsBottom)
            put("centerX", centerX)
            put("centerY", centerY)
        })
        put("isClickable", isClickable)
        put("isEditable", isEditable)
        put("isScrollable", isScrollable)
    }
}

class AgyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Keeps tracking latest active package and window state if needed
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    fun click(x: Float, y: Float, callback: ((Boolean) -> Unit)? = null): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val builder = GestureDescription.Builder().apply { addStroke(stroke) }
        return dispatchGesture(builder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                callback?.invoke(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                callback?.invoke(false)
            }
        }, null)
    }

    fun scroll(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 300,
        callback: ((Boolean) -> Unit)? = null
    ): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs.coerceIn(50, 2000))
        val builder = GestureDescription.Builder().apply { addStroke(stroke) }
        return dispatchGesture(builder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                callback?.invoke(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                callback?.invoke(false)
            }
        }, null)
    }

    fun typeText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focusedNode = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
        if (focusedNode != null && focusedNode.isEditable) {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        return false
    }

    fun pressKey(key: String): Boolean {
        val action = when (key.lowercase().trim()) {
            "home" -> GLOBAL_ACTION_HOME
            "back" -> GLOBAL_ACTION_BACK
            "recents", "recent", "app_switch" -> GLOBAL_ACTION_RECENTS
            "notifications", "notification" -> GLOBAL_ACTION_NOTIFICATIONS
            "quick_settings", "settings" -> GLOBAL_ACTION_QUICK_SETTINGS
            "lock", "power" -> GLOBAL_ACTION_LOCK_SCREEN
            "screenshot" -> GLOBAL_ACTION_TAKE_SCREENSHOT
            else -> return false
        }
        return performGlobalAction(action)
    }

    fun readScreen(maxNodes: Int = 80): List<ScreenElement> {
        val root = rootInActiveWindow ?: return emptyList()
        val elements = mutableListOf<ScreenElement>()
        traverseNode(root, elements, maxNodes)
        return elements
    }

    private fun traverseNode(node: AccessibilityNodeInfo?, elements: MutableList<ScreenElement>, maxNodes: Int) {
        if (node == null || elements.size >= maxNodes) return

        val text = node.text?.toString()
        val desc = node.contentDescription?.toString()
        val isClickable = node.isClickable
        val isEditable = node.isEditable
        val isScrollable = node.isScrollable

        if (!text.isNullOrBlank() || !desc.isNullOrBlank() || isClickable || isEditable) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (rect.width() > 0 && rect.height() > 0) {
                elements.add(
                    ScreenElement(
                        text = text,
                        contentDescription = desc,
                        className = node.className?.toString() ?: "",
                        boundsLeft = rect.left,
                        boundsTop = rect.top,
                        boundsRight = rect.right,
                        boundsBottom = rect.bottom,
                        isClickable = isClickable,
                        isEditable = isEditable,
                        isScrollable = isScrollable
                    )
                )
            }
        }

        for (i in 0 until node.childCount) {
            if (elements.size >= maxNodes) break
            val child = node.getChild(i)
            if (child != null) {
                traverseNode(child, elements, maxNodes)
            }
        }
    }

    companion object {
        @Volatile
        var instance: AgyAccessibilityService? = null
            private set

        fun isServiceActive(): Boolean = instance != null

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            return enabledServices.contains(context.packageName) &&
                    enabledServices.contains(AgyAccessibilityService::class.java.simpleName)
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        fun executeRemoteAction(action: String, args: JSONObject): JSONObject {
            val service = instance ?: return JSONObject().apply {
                put("success", false)
                put("error", "Accessibility Service is not active. Please enable Antigravity in Android Accessibility Settings.")
            }

            return try {
                when (action.lowercase()) {
                    "device_click" -> {
                        val x = args.optDouble("x", -1.0).toFloat()
                        val y = args.optDouble("y", -1.0).toFloat()
                        if (x < 0 || y < 0) {
                            JSONObject().apply { put("success", false); put("error", "Invalid coordinates (x, y)") }
                        } else {
                            val latch = java.util.concurrent.CountDownLatch(1)
                            var gestureCompleted = false
                            val dispatched = service.click(x, y) { completed ->
                                gestureCompleted = completed
                                latch.countDown()
                            }
                            if (!dispatched) {
                                JSONObject().apply { put("success", false); put("error", "Click gesture dispatch rejected by system") }
                            } else {
                                latch.await(600, java.util.concurrent.TimeUnit.MILLISECONDS)
                                JSONObject().apply { put("success", gestureCompleted || dispatched); put("message", "Clicked at ($x, $y)") }
                            }
                        }
                    }
                    "device_scroll" -> {
                        val startX = args.optDouble("startX", -1.0).toFloat()
                        val startY = args.optDouble("startY", -1.0).toFloat()
                        val endX = args.optDouble("endX", -1.0).toFloat()
                        val endY = args.optDouble("endY", -1.0).toFloat()
                        val duration = args.optLong("durationMs", 300L)
                        val latch = java.util.concurrent.CountDownLatch(1)
                        var gestureCompleted = false
                        val dispatched = service.scroll(startX, startY, endX, endY, duration) { completed ->
                            gestureCompleted = completed
                            latch.countDown()
                        }
                        if (!dispatched) {
                            JSONObject().apply { put("success", false); put("error", "Scroll gesture dispatch rejected by system") }
                        } else {
                            latch.await(duration + 400, java.util.concurrent.TimeUnit.MILLISECONDS)
                            JSONObject().apply { put("success", gestureCompleted || dispatched); put("message", "Scrolled ($startX, $startY) -> ($endX, $endY)") }
                        }
                    }
                    "device_type_text" -> {
                        val text = args.optString("text", "")
                        val typed = service.typeText(text)
                        JSONObject().apply { put("success", typed); put("message", if (typed) "Typed text into focused element" else "No focused editable element found") }
                    }
                    "device_press_key" -> {
                        val key = args.optString("key", "")
                        val isSensitive = key.equals("lock", true) || key.equals("power", true)
                        if (isSensitive && !args.optBoolean("confirmed", false)) {
                            JSONObject().apply {
                                put("success", false)
                                put("error", "Safety Guardrail: Pressing '$key' requires explicit confirmation (confirmed: true)")
                                put("requiresConfirmation", true)
                            }
                        } else {
                            val pressed = service.pressKey(key)
                            JSONObject().apply { put("success", pressed); put("message", "Pressed key: $key") }
                        }
                    }
                    "device_read_screen" -> {
                        val elements = service.readScreen()
                        val array = JSONArray()
                        elements.forEach { array.put(it.toJson()) }
                        JSONObject().apply {
                            put("success", true)
                            put("elementCount", elements.size)
                            put("elements", array)
                        }
                    }
                    else -> {
                        JSONObject().apply { put("success", false); put("error", "Unknown action: $action") }
                    }
                }
            } catch (e: Exception) {
                JSONObject().apply { put("success", false); put("error", e.message ?: "Failed to execute action") }
            }
        }
    }
}
