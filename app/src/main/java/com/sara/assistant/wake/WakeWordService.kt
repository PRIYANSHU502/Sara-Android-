package com.sara.assistant.wake

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sara.assistant.R

/**
 * Starter wake-word service skeleton.
 * Real wake-word detection should use a dedicated engine (e.g., Porcupine).
 * This service shows lifecycle, foreground behavior, and where detection hooks in.
 */
class WakeWordService : Service() {

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotification("Sara sun rahi hai..."))
        // TODO: Initialize wake-word engine here and start microphone detection loop.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: On wake word detected, notify UI/activity to start VoiceInput flow.
        return START_STICKY
    }

    override fun onDestroy() {
        // TODO: Release wake-word engine/mic resources.
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Sara Wake Word", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sara Assistant")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "sara_wake_channel"
        private const val NOTIF_ID = 101
    }
}
