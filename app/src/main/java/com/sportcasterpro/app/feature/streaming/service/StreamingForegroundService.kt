package com.sportcasterpro.app.feature.streaming.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sportcasterpro.app.MainActivity
import com.sportcasterpro.app.R

/**
 * Keeps the process alive at foreground priority while a broadcast or local recording is active,
 * so Android doesn't kill the camera/RTMP pipeline when the screen locks or the app backgrounds.
 * Started/stopped by [com.sportcasterpro.app.feature.livestream.presentation.LiveStreamViewModel];
 * it does not own the camera or encoder itself.
 */
class StreamingForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): android.app.Notification {
        ensureChannel()
        val contentIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            android.app.PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_live)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.live_stream_title))
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(CHANNEL_ID, "Live Broadcast", NotificationManager.IMPORTANCE_LOW)
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "streaming_channel"
        const val NOTIFICATION_ID = 42
    }
}
