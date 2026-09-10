package com.yugalify.arrowzen.data.repository

import com.yugalify.arrowzen.data.database.dao.GameStatisticsDao
import com.yugalify.arrowzen.data.database.entity.GameStatisticsEntity
import com.yugalify.arrowzen.domain.model.GameStatistics
import com.yugalify.arrowzen.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StatisticsRepositoryImpl(private val dao: GameStatisticsDao) : StatisticsRepository {

    override fun observeStatistics(): Flow<GameStatistics> =
        dao.observeStatistics().map { it?.toDomain() ?: GameStatistics() }

    override suspend fun recordPuzzleCompleted(arrowsEscaped: Int, hintsUsed: Int, playTimeMillis: Long) {
        val current = dao.getStatistics() ?: GameStatisticsEntity()
        val updated = current.copy(
            totalArrowsEscaped = current.totalArrowsEscaped + arrowsEscaped,
            totalHintsUsed = current.totalHintsUsed + hintsUsed,
            totalPlayTimeMillis = current.totalPlayTimeMillis + playTimeMillis
        )
        dao.upsert(updated)
    }

    private fun GameStatisticsEntity.toDomain() = GameStatistics(
        totalArrowsEscaped = totalArrowsEscaped,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        totalHintsUsed = totalHintsUsed,
        totalPlayTimeMillis = totalPlayTimeMillis
    )
}
