package com.yanfangxiong.assistivetouch

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager

/**
 * @author fxYan
 */
class AssistiveTouchWindowManager(
        private val context: Context
) {

    private val wm: WindowManager? by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager?
    }
    val windowWidth: Int
    val windowHeight: Int
    val statusBarHeight: Int
    val surplusHeight: Int

    init {
        val metrics = DisplayMetrics()
        wm?.defaultDisplay?.getMetrics(metrics)
        windowWidth = Math.floor(metrics.widthPixels.toDouble()).toInt()
        windowHeight = Math.floor(metrics.heightPixels.toDouble()).toInt()
        statusBarHeight = calculateStatusBarHeight()
        surplusHeight = windowHeight - statusBarHeight
    }

    fun addAssistiveTouch(assistiveTouch: View, lp: WindowManager.LayoutParams) {
        wm?.addView(assistiveTouch, lp)
    }

    fun updateAssistiveTouch(assistiveTouch: View?, lp: WindowManager.LayoutParams) {
        wm?.updateViewLayout(assistiveTouch, lp)
    }

    fun removeAssistiveTouch(assistiveTouch: View) {
        wm?.removeView(assistiveTouch)
    }

    private fun calculateStatusBarHeight(): Int {
        val statusBarResourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (statusBarResourceId <= 0) {
            return 0
        }
        val statusBarHeight = context.resources.getDimensionPixelSize(statusBarResourceId)
        return Math.floor(statusBarHeight.toDouble()).toInt()
    }

}