package com.yashwant.agymobile.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.yashwant.agymobile.R
import com.yashwant.agymobile.core.HomeScreenShortcutManager
import com.yashwant.agymobile.core.ShortcutTarget
import com.yashwant.agymobile.core.WorkspaceManager
import java.io.File

class AgyStatusWidgetReceiver : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.widget_status_card)

            val workspaceManager = WorkspaceManager(File(context.filesDir, "workspace"))
            val activeDir = workspaceManager.getInitialWorkspace()
            val folderName = WorkspaceManager.getDisplayName(activeDir)
            val folderPath = activeDir.absolutePath
            val wsInfo = workspaceManager.getWorkspaceInfo(activeDir)

            val engineDetails = if (wsInfo.gitBranch != null) {
                "Daemon Ready • branch: ${wsInfo.gitBranch}"
            } else {
                "Daemon Ready • ARM64 Native"
            }

            views.setTextViewText(R.id.tv_widget_folder_name, folderName)
            views.setTextViewText(R.id.tv_widget_folder_path, folderPath)
            views.setTextViewText(R.id.tv_widget_engine_details, engineDetails)

            // Intent to open workspace
            val openIntent = HomeScreenShortcutManager.createTargetIntent(
                context,
                ShortcutTarget.OpenWorkspace(folderPath, folderName)
            )
            val openPendingIntent = PendingIntent.getActivity(
                context,
                1301,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_status_root, openPendingIntent)
            views.setOnClickPendingIntent(R.id.btn_widget_open_action, openPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, AgyStatusWidgetReceiver::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds != null && widgetIds.isNotEmpty()) {
                for (id in widgetIds) {
                    updateWidget(context, appWidgetManager, id)
                }
            }
        }
    }
}
