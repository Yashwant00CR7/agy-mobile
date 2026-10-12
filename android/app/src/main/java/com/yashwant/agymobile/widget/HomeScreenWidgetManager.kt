package com.yashwant.agymobile.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast

enum class WidgetType(
    val title: String,
    val description: String,
    val standardDimensions: String,
    val compactProportion: String,
    val expandedProportion: String
) {
    PROMPT_PILL(
        title = "Prompt Pill",
        description = "Compact voice & text prompt bar for home screen",
        standardDimensions = "3x1 / 2x1",
        compactProportion = "Standard Pill (3x1)",
        expandedProportion = "Compact Pill (2x1)"
    ),
    ACTIONS_GRID(
        title = "Actions Grid",
        description = "One-tap buttons for Chat, Workspace, Voice & Prompt",
        standardDimensions = "4x1 / 3x2",
        compactProportion = "Single Row (4x1)",
        expandedProportion = "Expanded Grid (3x2)"
    ),
    STATUS_CARD(
        title = "Status Card",
        description = "Live folder status, engine readiness & quick launch",
        standardDimensions = "4x2 / 3x2",
        compactProportion = "Compact (3x2)",
        expandedProportion = "Full Card (4x2)"
    )
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
