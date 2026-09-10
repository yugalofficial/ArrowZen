package com.yugalify.arrowzen.core.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService

enum class HapticEvent { VALID_MOVE, BLOCKED_MOVE, ACHIEVEMENT, LEVEL_COMPLETE }

/**
 * Optional haptic feedback (Section 26). Handles the Android 12+
 * VibratorManager split transparently and degrades to a no-op on devices
 * with no vibrator hardware -- never crashes, never requires a permission
 * beyond the standard VIBRATE (declared in the manifest, no runtime prompt).
 */
class HapticsManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService<VibratorManager>()?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService<Vibrator>()
    }

    fun perform(event: HapticEvent, enabled: Boolean) {
        if (!enabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        val pattern: LongArray = when (event) {
            HapticEvent.VALID_MOVE -> longArrayOf(0, 15)
            HapticEvent.BLOCKED_MOVE -> longArrayOf(0, 20, 40, 20)
            HapticEvent.ACHIEVEMENT -> longArrayOf(0, 15, 60, 25)
            HapticEvent.LEVEL_COMPLETE -> longArrayOf(0, 20, 50, 20, 50, 30)
        }

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        }
    }
}
