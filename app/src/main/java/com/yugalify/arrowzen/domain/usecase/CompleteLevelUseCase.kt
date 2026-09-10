package com.yugalify.arrowzen.domain.usecase

import com.yugalify.arrowzen.domain.model.LevelProgress
import com.yugalify.arrowzen.domain.repository.AchievementRepository
import com.yugalify.arrowzen.domain.repository.LevelRepository
import com.yugalify.arrowzen.domain.repository.StatisticsRepository
import com.yugalify.arrowzen.game.generator.LevelCatalog
import com.yugalify.arrowzen.game.rating.StarRatingCalculator

/**
 * Everything that should happen exactly once when a puzzle is solved:
 * persist the level's best record, roll aggregate statistics forward, and
 * advance any achievements that care about this result. Kept as a single
 * use case so the ViewModel doesn't need to know about three repositories.
 */
class CompleteLevelUseCase(
    private val levelRepository: LevelRepository,
    private val statisticsRepository: StatisticsRepository,
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(
        levelId: String,
        arrowsEscaped: Int,
        mistakes: Int,
        hintsUsed: Int,
        elapsedMillis: Long
    ): LevelProgress {
        val parTime = LevelCatalog.parTimeFor(levelId)
        val stars = StarRatingCalculator.calculateStars(
            mistakes = mistakes,
            hintsUsed = hintsUsed,
            elapsedMillis = elapsedMillis,
            parTimeMillis = parTime
        )

        val progress = levelRepository.recordCompletion(
            levelId = levelId,
            stars = stars,
            timeMillis = elapsedMillis,
            mistakes = mistakes,
            hintsUsed = hintsUsed
        )

        statisticsRepository.recordPuzzleCompleted(
            arrowsEscaped = arrowsEscaped,
            hintsUsed = hintsUsed,
            playTimeMillis = elapsedMillis
        )

        if (progress.completionCount == 1) {
            achievementRepository.incrementProgress("first_escape")
        }
        achievementRepository.incrementProgress("escape_artist")
        achievementRepository.incrementProgress("arrow_master")

        if (elapsedMillis < 15_000L) {
            achievementRepository.incrementProgress("speed_arrow")
        }
        if (hintsUsed == 0) {
            achievementRepository.incrementProgress("no_help_needed")
        }
        if (stars == 3) {
            achievementRepository.incrementProgress("perfect_mind")
        }

        return progress
    }
}
