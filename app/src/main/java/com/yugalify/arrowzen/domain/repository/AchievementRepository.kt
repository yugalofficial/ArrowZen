package com.yugalify.arrowzen.domain.repository

import com.yugalify.arrowzen.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun observeAll(): Flow<List<Achievement>>

    /** Ensures the fixed achievement catalog exists; safe to call on every launch. */
    suspend fun seedIfNeeded()

    /**
     * Advances [achievementId]'s progress toward its target, unlocking it the
     * moment progress reaches target. No-ops silently if already unlocked.
     */
    suspend fun incrementProgress(achievementId: String, amount: Int = 1)
}
