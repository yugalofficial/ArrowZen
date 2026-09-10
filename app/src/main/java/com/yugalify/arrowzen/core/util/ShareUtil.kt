package com.yugalify.arrowzen.core.util

import android.content.Context
import android.content.Intent
import com.yugalify.arrowzen.core.constants.AppConstants

/**
 * Section 31: share results via the standard Android Sharesheet. No server
 * infrastructure, no generated image card -- just well-formed original
 * share text, which is what the spec calls out as the required baseline
 * (an image card is explicitly optional).
 */
object ShareUtil {

    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share your result"))
    }

    fun dailyChallengeShareText(streak: Int, elapsedSeconds: Long, wasPerfect: Boolean): String = buildString {
        appendLine("🏹 I completed today's ${AppConstants.APP_NAME} Daily Arrow!")
        appendLine("⏱ Time: ${formatSeconds(elapsedSeconds)}")
        if (wasPerfect) appendLine("⭐ Perfect Run")
        appendLine("🔥 $streak Day Streak")
        appendLine()
        append("Can you beat me? #${AppConstants.APP_NAME}")
    }

    fun levelCompleteShareText(levelId: String, stars: Int, elapsedSeconds: Long): String = buildString {
        appendLine("🏹 I just cleared $levelId in ${AppConstants.APP_NAME}!")
        appendLine("${"⭐".repeat(stars)}")
        appendLine("⏱ Time: ${formatSeconds(elapsedSeconds)}")
        append("#${AppConstants.APP_NAME}")
    }

    fun achievementShareText(title: String): String =
        "🏆 Just unlocked \"$title\" in ${AppConstants.APP_NAME}! #${AppConstants.APP_NAME}"

    private fun formatSeconds(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return if (m > 0) "${m}m ${s}s" else "${s}s"
    }
}
