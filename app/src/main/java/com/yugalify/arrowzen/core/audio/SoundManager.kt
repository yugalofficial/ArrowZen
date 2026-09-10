package com.yugalify.arrowzen.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.yugalify.arrowzen.R

enum class SfxEvent { TAP, VALID_MOVE, BLOCKED_MOVE, HINT, ACHIEVEMENT, LEVEL_COMPLETE }

/**
 * Lightweight SoundPool-backed effects player (Section 27). All six clips
 * together are well under 100KB, loaded once at construction and reused for
 * the app's lifetime -- no per-play disk I/O.
 *
 * Every public method is defensive: if a clip somehow fails to load (OEM
 * quirk, corrupted resource, low-memory device) playback is silently
 * skipped rather than crashing the game (Section 42 error handling / Section
 * 33's "never block gameplay" principle applies equally to audio).
 */
class SoundManager(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds: Map<SfxEvent, Int> = runCatching {
        mapOf(
            SfxEvent.TAP to soundPool.load(context, R.raw.sfx_tap, 1),
            SfxEvent.VALID_MOVE to soundPool.load(context, R.raw.sfx_valid_move, 1),
            SfxEvent.BLOCKED_MOVE to soundPool.load(context, R.raw.sfx_blocked_move, 1),
            SfxEvent.HINT to soundPool.load(context, R.raw.sfx_hint, 1),
            SfxEvent.ACHIEVEMENT to soundPool.load(context, R.raw.sfx_achievement, 1),
            SfxEvent.LEVEL_COMPLETE to soundPool.load(context, R.raw.sfx_level_complete, 1)
        )
    }.getOrElse { emptyMap() }

    fun play(event: SfxEvent, enabled: Boolean) {
        if (!enabled) return
        val id = soundIds[event] ?: return
        runCatching { soundPool.play(id, 1f, 1f, 1, 0, 1f) }
    }

    fun release() {
        soundPool.release()
    }
}
