package com.music.bitchord

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class LyricsEngine(private val ctx: Context) {
    var title = ""; var artist = ""
    private var key = ""
    @Volatile private var lines = listOf<Pair<Long, String>>()

    // null = tiada lagu sedang main
    fun line(): String? {
        val msm = ctx.getSystemService(MediaSessionManager::class.java)
        val sessions = try {
            msm.getActiveSessions(ComponentName(ctx, LyricsService::class.java))
        } catch (e: SecurityException) { return null }
        val c = sessions.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: return null
        val m = c.metadata ?: return null
        title = m.getString("android.media.metadata.TITLE") ?: return null
        artist = m.getString("android.media.metadata.ARTIST") ?: ""
        val dur = m.getLong("android.media.metadata.DURATION") / 1000
        val k = "$artist|$title"
        if (k != key) { key = k; lines = emptyList(); fetch(title, artist, dur) }
        val ps = c.playbackState ?: return null
        val pos = ps.position +
            ((SystemClock.elapsedRealtime() - ps.lastPositionUpdateTime) * ps.playbackSpeed).toLong()
        return lines.lastOrNull { it.first <= pos }?.second ?: ""
    }

    private fun fetch(t: String, a: String, d: Long) = Thread {
        try {
            fun e(s: String) = URLEncoder.encode(s, "UTF-8")
            val u = "https://lrclib.net/api/get?track_name=${e(t)}&artist_name=${e(a)}" +
                    (if (d > 0) "&duration=$d" else "")
            val j = JSONObject(URL(u).readText())
            val re = Regex("""\[(\d+):(\d+\.?\d*)](.*)""")
            lines = j.optString("syncedLyrics").lines().mapNotNull {
                re.matchEntire(it.trim())?.let { m ->
                    ((m.groupValues[1].toLong() * 60 + m.groupValues[2].toDouble()) * 1000).toLong() to
                        m.groupValues[3].trim()
                }
            }
        } catch (_: Exception) {}
    }.start()

    companion object { @Volatile var dreaming = false }
}
