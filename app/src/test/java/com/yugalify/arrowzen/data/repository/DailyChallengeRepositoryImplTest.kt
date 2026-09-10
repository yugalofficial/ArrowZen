package com.yugalify.arrowzen.data.repository

import com.yugalify.arrowzen.data.database.dao.DailyChallengeDao
import com.yugalify.arrowzen.data.database.entity.DailyChallengeEntity
import com.yugalify.arrowzen.game.generator.DailyPuzzleProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

private class FakeDailyChallengeDao : DailyChallengeDao {
    private val store = mutableMapOf<String, DailyChallengeEntity>()

    override suspend fun getByDate(dateKey: String): DailyChallengeEntity? = store[dateKey]

    override suspend fun getCompletedDatesDescending(): List<DailyChallengeEntity> =
        store.values.filter { it.isCompleted }.sortedByDescending { it.dateKey }

    override suspend fun upsert(entity: DailyChallengeEntity) {
        store[entity.dateKey] = entity
    }
}

class DailyChallengeRepositoryImplTest {

    @Test
    fun `no completions ever means zero streak`() = runBlocking {
        val repository = DailyChallengeRepositoryImpl(FakeDailyChallengeDao())

        val summary = repository.getTodaySummary()

        assertEquals(0, summary.currentStreak)
        assertEquals(false, summary.isCompletedToday)
    }

    @Test
    fun `completing today only gives a streak of one`() = runBlocking {
        val repository = DailyChallengeRepositoryImpl(FakeDailyChallengeDao())

        repository.recordCompletion(mistakes = 0, hintsUsed = 0, elapsedMillis = 20_000L)
        val summary = repository.getTodaySummary()

        assertEquals(1, summary.currentStreak)
        assertTrue(summary.isCompletedToday)
    }

    @Test
    fun `consecutive prior days extend the streak`() = runBlocking {
        val dao = FakeDailyChallengeDao()
        val today = LocalDate.now()
        for (offset in 1..4) {
            val dateKey = DailyPuzzleProvider.dateKeyFor(today.minusDays(offset.toLong()))
            dao.upsert(DailyChallengeEntity(dateKey = dateKey, isCompleted = true, completionTimeMillis = 10_000L))
        }
        val repository = DailyChallengeRepositoryImpl(dao)

        repository.recordCompletion(mistakes = 0, hintsUsed = 0, elapsedMillis = 15_000L)
        val summary = repository.getTodaySummary()

        assertEquals(5, summary.currentStreak)
    }

    @Test
    fun `a gap in prior days stops the streak count there`() = runBlocking {
        val dao = FakeDailyChallengeDao()
        val today = LocalDate.now()
        // Yesterday completed, but 2 days ago missing -> streak should be 2 (today + yesterday).
        val yesterdayKey = DailyPuzzleProvider.dateKeyFor(today.minusDays(1))
        dao.upsert(DailyChallengeEntity(dateKey = yesterdayKey, isCompleted = true, completionTimeMillis = 10_000L))
        val threeDaysAgoKey = DailyPuzzleProvider.dateKeyFor(today.minusDays(3))
        dao.upsert(DailyChallengeEntity(dateKey = threeDaysAgoKey, isCompleted = true, completionTimeMillis = 10_000L))

        val repository = DailyChallengeRepositoryImpl(dao)
        repository.recordCompletion(mistakes = 0, hintsUsed = 0, elapsedMillis = 15_000L)
        val summary = repository.getTodaySummary()

        assertEquals(2, summary.currentStreak)
    }

    @Test
    fun `recording completion twice in one day does not overwrite the first result`() = runBlocking {
        val repository = DailyChallengeRepositoryImpl(FakeDailyChallengeDao())

        repository.recordCompletion(mistakes = 0, hintsUsed = 0, elapsedMillis = 10_000L)
        repository.recordCompletion(mistakes = 5, hintsUsed = 3, elapsedMillis = 90_000L)
        val summary = repository.getTodaySummary()

        assertEquals(10_000L, summary.bestTimeMillis)
    }
}
