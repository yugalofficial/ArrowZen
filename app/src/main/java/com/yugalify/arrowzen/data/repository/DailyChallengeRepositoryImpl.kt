package com.yugalify.arrowzen.data.repository

import com.yugalify.arrowzen.data.database.dao.DailyChallengeDao
import com.yugalify.arrowzen.data.database.entity.DailyChallengeEntity
import com.yugalify.arrowzen.domain.model.DailySummary
import com.yugalify.arrowzen.domain.repository.DailyChallengeRepository
import com.yugalify.arrowzen.game.generator.DailyPuzzleProvider
import java.time.LocalDate

/**
 * Streak logic (Section 65): walks backward one calendar day at a time from
 * today, counting consecutive completed days, and stops at the first gap.
 * This intentionally recomputes from the stored history rather than caching
 * a running counter, so it self-heals if a write is ever missed or the
 * device date changes unexpectedly -- there's no separate "streak" value
 * that can drift out of sync with the actual completion records.
 */
class DailyChallengeRepositoryImpl(private val dao: DailyChallengeDao) : DailyChallengeRepository {

    override suspend fun getTodaySummary(): DailySummary {
        val todayKey = DailyPuzzleProvider.todayDateKey()
        val today = dao.getByDate(todayKey)
        val completedEntries = dao.getCompletedDatesDescending()

        return DailySummary(
            dateKey = todayKey,
            isCompletedToday = today?.isCompleted == true,
            currentStreak = computeStreak(completedEntries.map { it.dateKey }.toSet()),
            bestTimeMillis = completedEntries.mapNotNull { it.completionTimeMillis }.minOrNull()
        )
    }

    override suspend fun recordCompletion(mistakes: Int, hintsUsed: Int, elapsedMillis: Long) {
        val todayKey = DailyPuzzleProvider.todayDateKey()
        val existing = dao.getByDate(todayKey)

        // Never overwrite an already-completed day with a worse replay.
        if (existing?.isCompleted == true) return

        dao.upsert(
            DailyChallengeEntity(
                dateKey = todayKey,
                isCompleted = true,
                completionTimeMillis = elapsedMillis,
                mistakes = mistakes,
                hintsUsed = hintsUsed
            )
        )
    }

    private fun computeStreak(completedDateKeys: Set<String>): Int {
        var streak = 0
        var cursor = LocalDate.now()
        while (DailyPuzzleProvider.dateKeyFor(cursor) in completedDateKeys) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
