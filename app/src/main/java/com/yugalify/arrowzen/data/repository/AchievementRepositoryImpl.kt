package com.yugalify.arrowzen.data.repository

import com.yugalify.arrowzen.data.database.dao.AchievementDao
import com.yugalify.arrowzen.data.database.entity.AchievementCatalog
import com.yugalify.arrowzen.data.database.entity.AchievementEntity
import com.yugalify.arrowzen.domain.model.Achievement
import com.yugalify.arrowzen.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AchievementRepositoryImpl(private val dao: AchievementDao) : AchievementRepository {

    override fun observeAll(): Flow<List<Achievement>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun seedIfNeeded() {
        dao.insertAllIfAbsent(AchievementCatalog.seedList())
    }

    override suspend fun incrementProgress(achievementId: String, amount: Int) {
        val existing = dao.getById(achievementId) ?: return
        if (existing.isUnlocked) return

        val newProgress = (existing.progress + amount).coerceAtMost(existing.target)
        val justUnlocked = newProgress >= existing.target

        dao.update(
            existing.copy(
                progress = newProgress,
                isUnlocked = justUnlocked,
                unlockedAtMillis = if (justUnlocked) System.currentTimeMillis() else existing.unlockedAtMillis
            )
        )
    }

    private fun AchievementEntity.toDomain() = Achievement(
        id = id,
        title = title,
        description = description,
        iconReference = iconReference,
        progress = progress,
        target = target,
        isUnlocked = isUnlocked
    )
}
