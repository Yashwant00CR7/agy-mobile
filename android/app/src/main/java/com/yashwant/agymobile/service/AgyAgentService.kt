package com.yashwant.agymobile.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.yashwant.agymobile.MainActivity
import com.yashwant.agymobile.core.AgyProcessEngine
import com.yashwant.agymobile.model.AgentStatus
import com.yashwant.agymobile.model.AgyEvent
import kotlinx.coroutines.*

class AgyAgentService : Service() {

    companion object {
        const val CHANNEL_ID = "agy_agent_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.yashwant.agymobile.ACTION_START"
        const val ACTION_STOP = "com.yashwant.agymobile.ACTION_STOP"
        const val ACTION_APPROVE = "com.yashwant.agymobile.ACTION_APPROVE"
        const val ACTION_DENY = "com.yashwant.agymobile.ACTION_DENY"
        const val EXTRA_TOOL_ID = "extra_tool_id"

        fun startService(context: Context) {
            val intent = Intent(context, AgyAgentService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AgyAgentService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): AgyAgentService = this@AgyAgentService
    }

    private val binder = LocalBinder()
    val engine = AgyProcessEngine()

    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        observeEngineEvents()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, buildNotification("Ready", "Antigravity daemon active", null))
            }
            ACTION_STOP -> {
                engine.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_APPROVE -> {
                val toolId = intent.getStringExtra(EXTRA_TOOL_ID)
                if (!toolId.isNullOrBlank()) {
                    engine.resolveApproval(toolId, true)
                }
            }
            ACTION_DENY -> {
                val toolId = intent.getStringExtra(EXTRA_TOOL_ID)
                if (!toolId.isNullOrBlank()) {
                    engine.resolveApproval(toolId, false)
                }
            }
        }
        return START_STICKY
    }

    private fun observeEngineEvents() {
        serviceScope.launch {
            engine.events.collect { event ->
                when (event) {
                    is AgyEvent.ApprovalRequired -> {
                        val notif = buildApprovalNotification(event.id, event.name, event.description)
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.notify(NOTIFICATION_ID, notif)
                    }
                    is AgyEvent.Status -> {
                        val title = when (event.status) {
                            AgentStatus.RUNNING -> "Executing Turn..."
                            AgentStatus.WAITING_APPROVAL -> "Approval Required ⚠️"
                            AgentStatus.IDLE -> "Agent Idle"
                            AgentStatus.ERROR -> "Error Occurred"
                        }
                        val notif = buildNotification(title, event.message ?: "", null)
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.notify(NOTIFICATION_ID, notif)
                    }
                    else -> {}
                }
            }
        }
    }

    fun buildNotification(title: String, text: String, toolId: String?): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingTap = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, AgyAgentService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingCancel = PendingIntent.getService(
            this, 1, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Antigravity: $title")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingTap)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", pendingCancel)

        return builder.build()
    }

    fun buildApprovalNotification(toolId: String, toolName: String, desc: String): Notification {
        val tapIntent = Intent(this, MainActivity::class.java)
        val pendingTap = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val approveIntent = Intent(this, AgyAgentService::class.java).apply {
            action = ACTION_APPROVE
            putExtra(EXTRA_TOOL_ID, toolId)
        }
        val pendingApprove = PendingIntent.getService(
            this, 2, approveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val denyIntent = Intent(this, AgyAgentService::class.java).apply {
            action = ACTION_DENY
            putExtra(EXTRA_TOOL_ID, toolId)
        }
        val pendingDeny = PendingIntent.getService(
            this, 3, denyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Approve: $toolName")
            .setContentText(desc)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingTap)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.checkbox_on_background, "Approve", pendingApprove)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Deny", pendingDeny)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Antigravity Agent Service"
            val descriptionText = "Persistent background execution and tool approvals"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "agy:agent_wakelock"
            )
            wakeLock?.acquire(24 * 60 * 60 * 1000L) // 24 hours max
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }
}
