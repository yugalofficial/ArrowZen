package com.yugalify.arrowzen.domain.repository

import com.yugalify.arrowzen.domain.model.LevelProgress
import kotlinx.coroutines.flow.Flow

interface LevelRepository {
    fun observeAllProgress(): Flow<List<LevelProgress>>
    suspend fun getProgress(levelId: String): LevelProgress

    /**
     * Records a completed attempt, updating best-stars/best-time/fewest-mistakes
     * only where the new attempt actually improves on the stored record
     * (Section 16: restart must never overwrite historical best records).
     */
    suspend fun recordCompletion(
        levelId: String,
        stars: Int,
        timeMillis: Long,
        mistakes: Int,
        hintsUsed: Int
    ): LevelProgress
}
