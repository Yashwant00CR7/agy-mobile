package com.yashwant.agymobile.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.yashwant.agymobile.R
import com.yashwant.agymobile.core.HomeScreenShortcutManager
import com.yashwant.agymobile.core.ShortcutTarget

class AgyActionsWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_actions_grid)

            // 1. New Chat Button
            val newChatIntent = HomeScreenShortcutManager.createTargetIntent(
                context,
                ShortcutTarget.NewChat
            )
            views.setOnClickPendingIntent(
                R.id.btn_action_new_chat,
                PendingIntent.getActivity(
                    context,
                    1201,
                    newChatIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // 2. Folder / Workspace Button
            val folderIntent = HomeScreenShortcutManager.createTargetIntent(
                context,
                ShortcutTarget.OpenWorkspace("", "")
            )
            views.setOnClickPendingIntent(
                R.id.btn_action_folder,
                PendingIntent.getActivity(
                    context,
                    1202,
                    folderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // 3. Voice Button
            val voiceIntent = HomeScreenShortcutManager.createTargetIntent(
                context,
                ShortcutTarget.VoiceDictation
            )
            views.setOnClickPendingIntent(
                R.id.btn_action_voice,
                PendingIntent.getActivity(
                    context,
                    1203,
                    voiceIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // 4. Prompt Button
            val promptIntent = HomeScreenShortcutManager.createTargetIntent(
                context,
                ShortcutTarget.QuickPrompt("")
            )
            views.setOnClickPendingIntent(
                R.id.btn_action_prompt,
                PendingIntent.getActivity(
                    context,
                    1204,
                    promptIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
