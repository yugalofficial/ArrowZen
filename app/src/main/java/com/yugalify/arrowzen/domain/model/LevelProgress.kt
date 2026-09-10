package com.yugalify.arrowzen.domain.model

/** UI/domain-facing view of a level's best-ever result (Section 17). */
data class LevelProgress(
    val levelId: String,
    val bestStars: Int = 0,
    val bestTimeMillis: Long? = null,
    val fewestMistakes: Int? = null,
    val hintsUsedOnBest: Int = 0,
    val completionCount: Int = 0,
    val isUnlocked: Boolean = false
) {
    val isCompleted: Boolean get() = completionCount > 0
}
