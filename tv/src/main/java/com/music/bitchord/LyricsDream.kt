package com.music.bitchord

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView

class LyricsDream : DreamService() {
    private val h = Handler(Looper.getMainLooper())
    private lateinit var engine: LyricsEngine
    private lateinit var lyric: TextView
    private lateinit var info: TextView

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        engine = LyricsEngine(this)
        val clock = TextClock(this).apply {
            format12Hour = "h:mm"; format24Hour = "HH:mm"
            textSize = 80f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
        }
        lyric = TextView(this).apply {
            textSize = 36f; setTextColor(Color.WHITE)
            gravity = Gravity.CENTER; setPadding(96, 48, 96, 16)
        }
        info = TextView(this).apply {
            textSize = 18f; setTextColor(Color.GRAY); gravity = Gravity.CENTER
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            addView(clock); addView(lyric); addView(info)
        })
    }

    private val tick = object : Runnable {
        override fun run() {
            val l = engine.line()
            lyric.text = l ?: ""
            info.text = if (l != null) "${engine.title} · ${engine.artist}" else ""
            h.postDelayed(this, 300)
        }
    }

    override fun onDreamingStarted() { LyricsEngine.dreaming = true; h.post(tick) }
    override fun onDreamingStopped() { LyricsEngine.dreaming = false; h.removeCallbacks(tick) }
}
