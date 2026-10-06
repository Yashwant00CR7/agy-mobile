package com.yashwant.agymobile.ui.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

object BrowserHelper {
    const val OBSIDIAN_TOOLBAR_COLOR: Int = 0xFF080C14.toInt()

    fun buildCustomTabsIntent(): CustomTabsIntent {
        val colorParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(OBSIDIAN_TOOLBAR_COLOR)
            .build()

        return CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorParams)
            .setShowTitle(true)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .setInstantAppsEnabled(true)
            .build()
    }

    fun openUrl(context: Context, url: String): Boolean {
        val uri = try {
            Uri.parse(url)
        } catch (_: Exception) {
            return false
        }

        return try {
            val customTabsIntent = buildCustomTabsIntent()
            if (context !is Activity) {
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            customTabsIntent.launchUrl(context, uri)
            true
        } catch (_: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_VIEW, uri)
                if (context !is Activity) {
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}
