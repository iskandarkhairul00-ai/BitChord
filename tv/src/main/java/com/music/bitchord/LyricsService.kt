package com.music.bitchord

import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class LyricsService : NotificationListenerService() {
    private val h = Handler(Looper.getMainLooper())
    private lateinit var tv: TextView
    private lateinit var engine: LyricsEngine

    override fun onListenerConnected() {
        engine = LyricsEngine(this)
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val dm = resources.displayMetrics
        tv = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.END
            maxLines = 2
            setTextColor(Color.WHITE)
            setShadowLayer(8f, 0f, 2f, Color.BLACK)
        }
        val lp = WindowManager.LayoutParams(
            (dm.widthPixels * 0.42).toInt(), -2,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = (dm.widthPixels * 0.04).toInt()
            y = (dm.heightPixels * 0.62).toInt()
        }
        wm.addView(tv, lp)
        h.post(tick)
    }

    private val tick = object : Runnable {
        override fun run() {
            tv.visibility = if (LyricsEngine.dreaming) View.GONE else View.VISIBLE
            tv.text = engine.line() ?: ""
            h.postDelayed(this, 300)
        }
    }
}
