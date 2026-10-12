package com.yashwant.agymobile.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.yashwant.agymobile.R
import com.yashwant.agymobile.core.HomeScreenShortcutManager
import com.yashwant.agymobile.core.ShortcutTarget

class AgyPromptWidgetReceiver : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.widget_prompt_pill)

            // Intent for clicking prompt bar -> opens Quick Prompt in MainActivity
            val promptIntent = HomeScreenShortcutManager.createTargetIntent(
                context,
                ShortcutTarget.QuickPrompt("")
            )
            val promptPendingIntent = PendingIntent.getActivity(
                context,
                1101,
                promptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_prompt_root, promptPendingIntent)

            // Intent for clicking microphone -> launches Voice Dictation in MainActivity
            val voiceIntent = HomeScreenShortcutManager.createTargetIntent(
                context,
                ShortcutTarget.VoiceDictation
            )
            val voicePendingIntent = PendingIntent.getActivity(
                context,
                1102,
                voiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_voice, voicePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
