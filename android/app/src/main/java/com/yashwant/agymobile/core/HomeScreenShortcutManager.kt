package com.yashwant.agymobile.core

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import com.yashwant.agymobile.MainActivity
import com.yashwant.agymobile.util.ShortcutDesign
import com.yashwant.agymobile.util.ShortcutIconGenerator
import java.util.UUID

sealed class ShortcutTarget {
    data class LaunchApp(val packageName: String, val appName: String) : ShortcutTarget()
    data class OpenWorkspace(val folderPath: String, val folderName: String) : ShortcutTarget()
    data class QuickPrompt(val promptText: String) : ShortcutTarget()
    object NewChat : ShortcutTarget()
    object VoiceDictation : ShortcutTarget()
}

object HomeScreenShortcutManager {

    fun isPinShortcutSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            return shortcutManager?.isRequestPinShortcutSupported == true
        }
        return false
    }

    fun pinShortcut(
        context: Context,
        target: ShortcutTarget,
        design: ShortcutDesign
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(context, "Pinning shortcuts requires Android 8.0+", Toast.LENGTH_SHORT).show()
            return false
        }

        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported) {
            Toast.makeText(context, "Your launcher does not support pinned shortcuts", Toast.LENGTH_SHORT).show()
            return false
        }

        val launchIntent = createTargetIntent(context, target)
        val bitmap = ShortcutIconGenerator.createIconBitmap(design)
        val icon = if (bitmap != null) {
            Icon.createWithBitmap(bitmap)
        } else {
            Icon.createWithResource(context, android.R.drawable.sym_def_app_icon)
        }
        val shortcutId = "agy_sc_${UUID.randomUUID()}"

        val shortcutInfo = ShortcutInfo.Builder(context, shortcutId)
            .setShortLabel(design.label.ifBlank { "Antigravity" })
            .setLongLabel(design.label.ifBlank { "Antigravity Shortcut" })
            .setIcon(icon)
            .setIntent(launchIntent)
            .build()

        return try {
            val success = shortcutManager.requestPinShortcut(shortcutInfo, null)
            if (success) {
                Toast.makeText(context, "Pinned: ${design.label}", Toast.LENGTH_SHORT).show()
            }
            success
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to pin shortcut: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun createTargetIntent(context: Context, target: ShortcutTarget): Intent {
        return when (target) {
            is ShortcutTarget.LaunchApp -> {
                val pm = context.packageManager
                val launchIntent = pm?.getLaunchIntentForPackage(target.packageName)
                launchIntent?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                } ?: Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            is ShortcutTarget.OpenWorkspace -> {
                Intent(context, MainActivity::class.java).apply {
                    action = "com.yashwant.agymobile.ACTION_OPEN_WORKSPACE"
                    putExtra("EXTRA_WORKSPACE_PATH", target.folderPath)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
            is ShortcutTarget.QuickPrompt -> {
                Intent(context, MainActivity::class.java).apply {
                    action = "com.yashwant.agymobile.ACTION_QUICK_PROMPT"
                    putExtra("EXTRA_PROMPT_TEXT", target.promptText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
            is ShortcutTarget.NewChat -> {
                Intent(context, MainActivity::class.java).apply {
                    action = "com.yashwant.agymobile.ACTION_NEW_CHAT"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
            is ShortcutTarget.VoiceDictation -> {
                Intent(context, MainActivity::class.java).apply {
                    action = "com.yashwant.agymobile.ACTION_START_VOICE"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
        }
    }
}
