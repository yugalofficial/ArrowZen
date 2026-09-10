package com.yugalify.arrowzen.domain.model

/** Aggregate counters shown on the Statistics screen (Section 30). */
data class GameStatistics(
    val totalArrowsEscaped: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalHintsUsed: Long = 0,
    val totalPlayTimeMillis: Long = 0
)
