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
            text = "前景離開警告音效測試\n\n請保持本程式開啟，再按 Home 或切換到其他 App。當本程式完全進入背景時，系統會要求播放警告音。\n\n注意：Android 或手機廠商可能限制背景音效；本測試不能保證每台手機都一定聽得到。"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val test = Button(this).apply {
            text = "測試警告音"
            setOnClickListener {
                startService(Intent(this@MainActivity, WarningService::class.java).setAction(WarningService.ACTION_BEEP))
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            addView(title)
            addView(test)
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

    override fun onStop() {
        super.onStop()
        // onStop runs when this Activity is no longer visible; unlike onPause,
        // it avoids beeping for brief interruptions such as permission dialogs.
        startService(Intent(this, WarningService::class.java).setAction(WarningService.ACTION_BEEP))
    }
}
