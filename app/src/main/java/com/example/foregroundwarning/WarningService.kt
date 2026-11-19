package com.example.foregroundwarning

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class WarningService : Service() {
    private var tts: TextToSpeech? = null
    @Volatile private var warningActive = false
    @Volatile private var ttsReady = false

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
                .setContentText("離開考試畫面時會持續播放語音警告")
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("前景離開監測中")
                .setContentText("離開考試畫面時會持續播放語音警告")
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .build()
        }

        startForeground(NOTIFICATION_ID, notification)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                engine.setLanguage(Locale.TRADITIONAL_CHINESE)

                // Prefer an installed Chinese voice explicitly labelled as male.
                // If the phone has no such voice, Android's default Chinese voice is used.
                val maleVoice = engine.voices
                    ?.filter { it.locale.language == "zh" }
                    ?.firstOrNull {
                        it.name.contains("male", ignoreCase = true) ||
                            it.features.any { feature -> feature.contains("male", ignoreCase = true) }
                    }
                if (maleVoice != null) engine.voice = maleVoice

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit

                    override fun onDone(utteranceId: String?) {
                        if (warningActive) speakWarning()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (warningActive) speakWarning()
                    }
                })
                ttsReady = true
                if (warningActive) speakWarning()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ALERT -> {
                if (!warningActive) {
                    warningActive = true
                    if (ttsReady) speakWarning()
                }
            }
            ACTION_STOP -> {
                warningActive = false
                tts?.stop()
            }
        }
        return START_STICKY
    }

    private fun speakWarning() {
        if (!warningActive || !ttsReady) return
        tts?.speak(
            "請勿離開考試畫面，請立即返回考試。",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "foreground_warning"
        )
    }

    override fun onDestroy() {
        warningActive = false
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.foregroundwarning.START"
        const val ACTION_ALERT = "com.example.foregroundwarning.ALERT"
        const val ACTION_STOP = "com.example.foregroundwarning.STOP"
        private const val CHANNEL = "foreground_monitor"
        private const val NOTIFICATION_ID = 1
    }
}
