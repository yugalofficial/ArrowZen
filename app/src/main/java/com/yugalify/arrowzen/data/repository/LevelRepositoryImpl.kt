package com.yugalify.arrowzen.data.repository

import com.yugalify.arrowzen.data.database.dao.LevelProgressDao
import com.yugalify.arrowzen.data.database.entity.LevelProgressEntity
import com.yugalify.arrowzen.domain.model.LevelProgress
import com.yugalify.arrowzen.domain.repository.LevelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LevelRepositoryImpl(private val dao: LevelProgressDao) : LevelRepository {

    override fun observeAllProgress(): Flow<List<LevelProgress>> =
        dao.observeAllProgress().map { list -> list.map { it.toDomain() } }

    override suspend fun getProgress(levelId: String): LevelProgress =
        dao.getProgress(levelId)?.toDomain() ?: LevelProgress(levelId = levelId, isUnlocked = true)

    override suspend fun recordCompletion(
        levelId: String,
        stars: Int,
        timeMillis: Long,
        mistakes: Int,
        hintsUsed: Int
    ): LevelProgress {
        val existing = dao.getProgress(levelId)

        val updated = LevelProgressEntity(
            levelId = levelId,
            bestStars = maxOf(stars, existing?.bestStars ?: 0),
            bestTimeMillis = minOfNullable(timeMillis, existing?.bestTimeMillis),
            fewestMistakes = minOfNullable(mistakes, existing?.fewestMistakes),
            hintsUsedOnBest = if ((existing?.bestStars ?: 0) <= stars) hintsUsed else existing?.hintsUsedOnBest ?: hintsUsed,
            completionCount = (existing?.completionCount ?: 0) + 1,
            isUnlocked = true
        )

        dao.upsert(updated)
        return updated.toDomain()
    }

    private fun minOfNullable(new: Long, existing: Long?): Long =
        if (existing == null) new else minOf(new, existing)

    private fun minOfNullable(new: Int, existing: Int?): Int =
        if (existing == null) new else minOf(new, existing)

    private fun LevelProgressEntity.toDomain() = LevelProgress(
        levelId = levelId,
        bestStars = bestStars,
        bestTimeMillis = bestTimeMillis,
        fewestMistakes = fewestMistakes,
        hintsUsedOnBest = hintsUsedOnBest,
        completionCount = completionCount,
        isUnlocked = isUnlocked
    )
}
