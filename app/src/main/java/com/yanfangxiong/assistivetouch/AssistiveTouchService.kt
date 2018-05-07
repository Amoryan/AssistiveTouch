package com.yanfangxiong.assistivetouch

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * @author fxYan
 */
class AssistiveTouchService : Service() {

    private val assistiveTouch by lazy { AssistiveTouch(this) }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        assistiveTouch.addAssistiveTouch()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        assistiveTouch.removeAssistiveTouch()
        super.onDestroy()
    }

}