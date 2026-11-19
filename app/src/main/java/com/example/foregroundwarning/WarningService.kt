package com.example.foregroundwarning

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder

class WarningService : Service() {
    private var tone: ToneGenerator? = null

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "前景監測", NotificationManager.IMPORTANCE_LOW)
            )
        }

        val notification: Notification = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL)
                .setContentTitle("前景離開監測中")
                .setContentText("切換離開測試程式時會要求播放警告音")
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("前景離開監測中")
                .setContentText("切換離開測試程式時會要求播放警告音")
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .build()
        }

        startForeground(NOTIFICATION_ID, notification)
        tone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_BEEP) {
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 1200)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        tone?.release()
        tone = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.foregroundwarning.START"
        const val ACTION_BEEP = "com.example.foregroundwarning.BEEP"
        private const val CHANNEL = "foreground_monitor"
        private const val NOTIFICATION_ID = 1
    }
}
