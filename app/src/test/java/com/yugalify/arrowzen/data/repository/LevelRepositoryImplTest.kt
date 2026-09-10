package com.yugalify.arrowzen.data.repository

import com.yugalify.arrowzen.data.database.dao.LevelProgressDao
import com.yugalify.arrowzen.data.database.entity.LevelProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/** In-memory fake so this test runs as a plain JVM test with no Android/Room instance needed. */
private class FakeLevelProgressDao : LevelProgressDao {
    private val store = MutableStateFlow<Map<String, LevelProgressEntity>>(emptyMap())

    override suspend fun getProgress(levelId: String): LevelProgressEntity? = store.value[levelId]

    override fun observeAllProgress(): Flow<List<LevelProgressEntity>> =
        store.map { it.values.toList() }

    override suspend fun upsert(progress: LevelProgressEntity) {
        store.value = store.value + (progress.levelId to progress)
    }

    override suspend fun completedLevelCount(): Int =
        store.value.values.count { it.completionCount > 0 }
}

class LevelRepositoryImplTest {

    @Test
    fun `first completion is recorded as the best result`() = runBlocking {
        val repository = LevelRepositoryImpl(FakeLevelProgressDao())

        val progress = repository.recordCompletion(
            levelId = "level_001", stars = 3, timeMillis = 10_000L, mistakes = 0, hintsUsed = 0
        )

        assertEquals(3, progress.bestStars)
        assertEquals(10_000L, progress.bestTimeMillis)
        assertEquals(0, progress.fewestMistakes)
        assertEquals(1, progress.completionCount)
    }

    @Test
    fun `a worse replay does not overwrite the existing best stars or time`() = runBlocking {
        val repository = LevelRepositoryImpl(FakeLevelProgressDao())
        repository.recordCompletion("level_001", stars = 3, timeMillis = 10_000L, mistakes = 0, hintsUsed = 0)

        val afterWorseRun = repository.recordCompletion(
            "level_001", stars = 1, timeMillis = 60_000L, mistakes = 5, hintsUsed = 2
        )

        assertEquals(3, afterWorseRun.bestStars)
        assertEquals(10_000L, afterWorseRun.bestTimeMillis)
        assertEquals(0, afterWorseRun.fewestMistakes)
        assertEquals(2, afterWorseRun.completionCount)
    }

    @Test
    fun `a better replay improves the stored best record`() = runBlocking {
        val repository = LevelRepositoryImpl(FakeLevelProgressDao())
        repository.recordCompletion("level_001", stars = 1, timeMillis = 60_000L, mistakes = 4, hintsUsed = 2)

        val afterBetterRun = repository.recordCompletion(
            "level_001", stars = 3, timeMillis = 12_000L, mistakes = 0, hintsUsed = 0
        )

        assertEquals(3, afterBetterRun.bestStars)
        assertEquals(12_000L, afterBetterRun.bestTimeMillis)
        assertEquals(0, afterBetterRun.fewestMistakes)
        assertEquals(2, afterBetterRun.completionCount)
    }

    @Test
    fun `completion count increments every attempt regardless of quality`() = runBlocking {
        val repository = LevelRepositoryImpl(FakeLevelProgressDao())

        repeat(3) {
            repository.recordCompletion("level_001", stars = 1, timeMillis = 50_000L, mistakes = 3, hintsUsed = 1)
        }
        val progress = repository.getProgress("level_001")

        assertEquals(3, progress.completionCount)
    }

    @Test
    fun `an unattempted level reports zero completions and no best record`() = runBlocking {
        val repository = LevelRepositoryImpl(FakeLevelProgressDao())

        val progress = repository.getProgress("level_never_played")

        assertEquals(0, progress.completionCount)
        assertEquals(null, progress.bestTimeMillis)
        assertEquals(false, progress.isCompleted)
    }
}
