package com.yugalify.arrowzen.domain.repository

import com.yugalify.arrowzen.domain.model.GameStatistics
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun observeStatistics(): Flow<GameStatistics>

    /** Called once per completed puzzle (any mode) to roll counters forward. */
    suspend fun recordPuzzleCompleted(arrowsEscaped: Int, hintsUsed: Int, playTimeMillis: Long)
}
