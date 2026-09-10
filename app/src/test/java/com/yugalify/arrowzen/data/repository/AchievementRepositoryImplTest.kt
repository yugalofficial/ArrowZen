package com.yugalify.arrowzen.data.repository

import com.yugalify.arrowzen.data.database.dao.AchievementDao
import com.yugalify.arrowzen.data.database.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeAchievementDao : AchievementDao {
    private val store = MutableStateFlow<Map<String, AchievementEntity>>(emptyMap())

    override fun observeAll(): Flow<List<AchievementEntity>> = store.map { it.values.toList() }

    override suspend fun getById(id: String): AchievementEntity? = store.value[id]

    override suspend fun insertAllIfAbsent(achievements: List<AchievementEntity>) {
        val next = store.value.toMutableMap()
        for (achievement in achievements) {
            next.putIfAbsent(achievement.id, achievement)
        }
        store.value = next
    }

    override suspend fun update(achievement: AchievementEntity) {
        store.value = store.value + (achievement.id to achievement)
    }
}

class AchievementRepositoryImplTest {

    @Test
    fun `incrementing progress below target does not unlock`() = runBlocking {
        val dao = FakeAchievementDao()
        dao.insertAllIfAbsent(listOf(AchievementEntity(id = "escape_artist", title = "t", description = "d", iconReference = "i", target = 100)))
        val repository = AchievementRepositoryImpl(dao)

        repository.incrementProgress("escape_artist", amount = 5)
        val result = dao.getById("escape_artist")

        assertEquals(5, result?.progress)
        assertEquals(false, result?.isUnlocked)
    }

    @Test
    fun `reaching target unlocks the achievement`() = runBlocking {
        val dao = FakeAchievementDao()
        dao.insertAllIfAbsent(listOf(AchievementEntity(id = "first_escape", title = "t", description = "d", iconReference = "i", target = 1)))
        val repository = AchievementRepositoryImpl(dao)

        repository.incrementProgress("first_escape")
        val result = dao.getById("first_escape")

        assertTrue(result?.isUnlocked == true)
        assertEquals(1, result?.progress)
    }

    @Test
    fun `progress never exceeds target even with a large increment`() = runBlocking {
        val dao = FakeAchievementDao()
        dao.insertAllIfAbsent(listOf(AchievementEntity(id = "escape_artist", title = "t", description = "d", iconReference = "i", target = 100)))
        val repository = AchievementRepositoryImpl(dao)

        repository.incrementProgress("escape_artist", amount = 500)
        val result = dao.getById("escape_artist")

        assertEquals(100, result?.progress)
    }

    @Test
    fun `already unlocked achievements are not modified by further increments`() = runBlocking {
        val dao = FakeAchievementDao()
        dao.insertAllIfAbsent(listOf(AchievementEntity(id = "first_escape", title = "t", description = "d", iconReference = "i", target = 1)))
        val repository = AchievementRepositoryImpl(dao)
        repository.incrementProgress("first_escape")
        val unlockedAt = dao.getById("first_escape")?.unlockedAtMillis

        repository.incrementProgress("first_escape")
        val stillUnlockedAt = dao.getById("first_escape")?.unlockedAtMillis

        assertEquals(unlockedAt, stillUnlockedAt)
    }

    @Test
    fun `seedIfNeeded does not overwrite an already-progressed achievement`() = runBlocking {
        val dao = FakeAchievementDao()
        val repository = AchievementRepositoryImpl(dao)
        repository.seedIfNeeded()
        repository.incrementProgress("first_escape")

        repository.seedIfNeeded()
        val result = dao.getById("first_escape")

        assertEquals(1, result?.progress)
    }
}
