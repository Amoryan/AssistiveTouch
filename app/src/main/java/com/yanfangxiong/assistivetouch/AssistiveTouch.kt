package com.yanfangxiong.assistivetouch

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_assistive_touch.view.*

/**
 * @author fxYan
 */
class AssistiveTouch(
        private val context: Context
) : View.OnClickListener {

    private val assistiveTouch = LayoutInflater.from(context).inflate(R.layout.layout_assistive_touch, null)
    private val assistiveTouchWindowManager by lazy {
        AssistiveTouchWindowManager(context)
    }
    private val lp: WindowManager.LayoutParams by lazy {
        val temp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getCompatWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.RGBA_8888
        )
        temp.gravity = Gravity.START or Gravity.TOP
        temp
    }

    private var downX: Float = 0F
    private var downY: Float = 0F

    private var menuWidth: Int = 0
    private var menuHeight: Int = 0

    private var isMoving = false

    init {
        assistiveTouch.menuView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
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
                    adjustLayoutParamsPosition()
                    assistiveTouchWindowManager.updateAssistiveTouch(assistiveTouch, lp)
                    isMoving = true
                }
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
        assistiveTouch.menuView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    assistiveTouch.menuView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                menuWidth = assistiveTouch.menuView.width
                menuHeight = assistiveTouch.menuView.height
            }

        })

        assistiveTouch.volumeTv.setOnClickListener(this)
        assistiveTouch.searchTv.setOnClickListener(this)
        assistiveTouch.favoriteTv.setOnClickListener(this)
        assistiveTouch.lockTv.setOnClickListener(this)
        assistiveTouch.captureTv.setOnClickListener(this)
        assistiveTouch.homeTv.setOnClickListener(this)
    }

    private fun adjustLayoutParamsPosition() {
        if (lp.x < 0) {
            lp.x = 0
        }
        if (lp.x + menuWidth > assistiveTouchWindowManager.windowWidth) {
            lp.x = assistiveTouchWindowManager.windowWidth - menuWidth
        }

        if (lp.y < 0) {
            lp.y = 0
        }
        if (lp.y + menuHeight > assistiveTouchWindowManager.surplusHeight) {
            lp.y = assistiveTouchWindowManager.surplusHeight - menuHeight
        }
    }

    private fun getCompatWindowType(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.volumeTv -> Toast.makeText(context, "音量", Toast.LENGTH_SHORT).show()
            R.id.searchTv -> Toast.makeText(context, "搜索", Toast.LENGTH_SHORT).show()
            R.id.favoriteTv -> Toast.makeText(context, "收藏", Toast.LENGTH_SHORT).show()
            R.id.lockTv -> Toast.makeText(context, "锁屏", Toast.LENGTH_SHORT).show()
            R.id.captureTv -> Toast.makeText(context, "截图", Toast.LENGTH_SHORT).show()
            R.id.homeTv -> Toast.makeText(context, "首页", Toast.LENGTH_SHORT).show()
        }
        assistiveTouch.menuCl.visibility = View.GONE
        assistiveTouch.menuView.visibility = View.VISIBLE
    }

    fun addAssistiveTouch() {
        assistiveTouchWindowManager.addAssistiveTouch(assistiveTouch, lp)
    }

    fun removeAssistiveTouch() {
        assistiveTouchWindowManager.removeAssistiveTouch(assistiveTouch)
    }

}