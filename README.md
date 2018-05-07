# 前言
　　前段时间学习了Window和WindowManager，但是并没有实际的用到它，只是简单的了解了其基本流程和功能，于是想写一个Demo来加深对Window以及WindowManager的理解，就有了这篇文章，写的比较粗糙。
# 项目地址
　　[AssistiveTouch](https://github.com/Amoryan/AssistiveTouch)
　　简陋的效果图![001](/images/001.gif)
# 实现
## type的选择
　　为了让其悬浮在桌面上，它必须有一个System Window的type类型。
```kotlin
private fun getCompatWindowType(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
} else {
    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
}
```
　　这里会遇到一个问题，因为在Android6.0之后系统权限的限制，我们需要在6.0之后请求**<font color="#1b8fe6">SYSTEM_ALERT_WINDOW</font>**权限。这里**<font color="#1b8fe6">通过Settings的canDrawOverlays</font>**来做兼容处理。
```kotlin
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
```
　　如果没有权限的情况下，通过Intent跳转到设置界面，让用户进行设置。
## service
　　编写一个service，在service中对View进行添加，更新和删除操作，这里不和具体的Activity产生关联，所以onBind()返回null即可。
```kotlin
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
```
　　记得在manifest.xml文件中进行配置。
## 拖拽和点击
　　因为要同时设置onTouchListener和onClickListener，我们知道onTouchListener是先于onClickListener的。所以onTouchListener需要返回false，但是这样每次移动之后都会触发onClickListener，所以我添加了一个isMoving的标记。
```kotlin
assistiveTouch.menuView.setOnTouchListener { _, event ->
    when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            scrollAnimator?.cancel()
            downX = event.rawX
            downY = event.rawY
        }
        MotionEvent.ACTION_MOVE -> {
            val diffX = event.rawX - downX
            val diffY = event.rawY - downY
            downX = event.rawX
            downY = event.rawY
            lp.x = Math.floor(lp.x + diffX + 0.5).toInt()
            lp.y = Math.floor(lp.y + diffY + 0.5).toInt()
            adjustPositionWhenMove()
            assistiveTouchWindowManager.updateAssistiveTouch(assistiveTouch, lp)
            isMoving = true
        }
        MotionEvent.ACTION_UP -> adjustPositionWhenActionUp()
    }
    false
}
assistiveTouch.menuView.setOnClickListener {
    if (!isMoving) {
        assistiveTouch.menuView.visibility = View.GONE
        assistiveTouch.menuCl.visibility = View.VISIBLE
    } else {
        isMoving = false
    }
}
```
## 弹性恢复
　　当拖拽到屏幕中央的时候取消拖拽，需要将View恢复到贴边状态，考虑到柔和，这里使用属性动画的方式进行处理。
```kotlin

private fun adjustPositionWhenActionUp() {
    val left = lp.x
    val top = lp.y
    val right = assistiveTouchWindowManager.windowWidth - menuWidth - left
    val bottom = assistiveTouchWindowManager.windowHeight - menuHeight - top
    scrollAnimator = ObjectAnimator()
    when (mapOf(left to 0, top to 1, right to 2, bottom to 3).minBy { it.key }?.value) {
        0 -> {
            scrollAnimator?.propertyName = "layoutParamsX"
            scrollAnimator?.duration = left.toLong()
            scrollAnimator?.setIntValues(left, 0)
        }
        1 -> {
            scrollAnimator?.propertyName = "layoutParamsY"
            scrollAnimator?.duration = top.toLong()
            scrollAnimator?.setIntValues(top, 0)
        }
        2 -> {
            scrollAnimator?.propertyName = "layoutParamsX"
            scrollAnimator?.duration = right.toLong()
            scrollAnimator?.setIntValues(left, assistiveTouchWindowManager.windowWidth - menuWidth)
        }
        3 -> {
            scrollAnimator?.propertyName = "layoutParamsY"
            scrollAnimator?.duration = bottom.toLong()
            scrollAnimator?.setIntValues(top, assistiveTouchWindowManager.windowHeight - menuHeight)
        }
    }
    scrollAnimator?.target = this
    scrollAnimator?.interpolator = LinearInterpolator()
    scrollAnimator?.start()
}
```

