package com.yashwant.agymobile.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast

enum class WidgetType(val title: String, val description: String) {
    PROMPT_PILL("AI Prompt Pill", "Compact voice & text prompt bar for home screen"),
    ACTIONS_GRID("Quick Actions Grid", "One-tap buttons for Chat, Workspace, Voice & Prompt"),
    STATUS_CARD("Workspace Status Card", "Live folder status, engine readiness & quick launch")
}

object HomeScreenWidgetManager {

    fun isPinWidgetSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager?.isRequestPinAppWidgetSupported == true
        }
        return false
    }

    fun pinWidget(context: Context, widgetType: WidgetType): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(context, "Pinning widgets requires Android 8.0+", Toast.LENGTH_SHORT).show()
            return false
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (appWidgetManager == null || !appWidgetManager.isRequestPinAppWidgetSupported) {
            Toast.makeText(context, "Your launcher does not support pinned widgets", Toast.LENGTH_SHORT).show()
            return false
        }

        val receiverClass = when (widgetType) {
            WidgetType.PROMPT_PILL -> AgyPromptWidgetReceiver::class.java
            WidgetType.ACTIONS_GRID -> AgyActionsWidgetReceiver::class.java
            WidgetType.STATUS_CARD -> AgyStatusWidgetReceiver::class.java
        }

        val provider = ComponentName(context, receiverClass)

        return try {
            val success = appWidgetManager.requestPinAppWidget(provider, null, null)
            if (success) {
                Toast.makeText(context, "Added to Home Screen: ${widgetType.title}", Toast.LENGTH_SHORT).show()
            }
            success
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to pin widget: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
