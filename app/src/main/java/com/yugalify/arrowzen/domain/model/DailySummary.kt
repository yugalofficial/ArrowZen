package com.yugalify.arrowzen.domain.model

/** UI-facing snapshot of Daily Challenge status (Section 9). */
data class DailySummary(
    val dateKey: String,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val bestTimeMillis: Long?
)
