package com.yanfangxiong.assistivetouch

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.provider.Settings
import android.support.annotation.RequiresApi

class MainActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        checkCompat()
    }

    private fun checkCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                openFloating()
            } else {
                toSettingActivity()
            }
        } else {
            openFloating()
        }
    }

    private fun openFloating() {
        val intent = Intent(this, AssistiveTouchService::class.java)
        startService(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun toSettingActivity() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

}
