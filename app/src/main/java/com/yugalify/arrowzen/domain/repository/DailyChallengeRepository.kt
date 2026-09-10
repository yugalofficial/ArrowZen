package com.yugalify.arrowzen.domain.repository

import com.yugalify.arrowzen.domain.model.DailySummary

interface DailyChallengeRepository {
    suspend fun getTodaySummary(): DailySummary
    suspend fun recordCompletion(mistakes: Int, hintsUsed: Int, elapsedMillis: Long)
}
