package com.example.foregroundwarning

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this).apply {
            text = "前景離開語音警告測試\n\n請保持本程式開啟，再按 Home 或切換到其他 App。離開本程式畫面後，會連續播放語音警告；回到本程式畫面就會停止。\n\n注意：語音使用手機已安裝的中文語音引擎；若手機沒有男聲，可能會使用系統預設聲音。Android 或手機廠商也可能限制背景播放。"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val test = Button(this).apply {
            text = "測試語音警告"
            setOnClickListener {
                startService(Intent(this@MainActivity, WarningService::class.java).setAction(WarningService.ACTION_ALERT))
            }
        }

        val stop = Button(this).apply {
            text = "停止測試語音"
            setOnClickListener {
                startService(Intent(this@MainActivity, WarningService::class.java).setAction(WarningService.ACTION_STOP))
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            addView(title)
            addView(test)
            addView(stop)
        }
        setContentView(layout)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }

        val service = Intent(this, WarningService::class.java).setAction(WarningService.ACTION_START)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(service) else startService(service)
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, WarningService::class.java).setAction(WarningService.ACTION_STOP))
    }

    override fun onStop() {
        super.onStop()
        startService(Intent(this, WarningService::class.java).setAction(WarningService.ACTION_ALERT))
    }
}
