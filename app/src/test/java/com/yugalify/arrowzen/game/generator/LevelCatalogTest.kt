package com.yugalify.arrowzen.game.generator

import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.game.validator.PuzzleValidationResult
import com.yugalify.arrowzen.game.validator.PuzzleValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every level shipped in [LevelCatalog] must be independently verified
 * solvable — Section 40 explicitly forbids shipping unsolvable levels. This
 * is the regression test that would have caught the level_002
 * obstacle-on-path bug from Phase 2 development.
 */
class LevelCatalogTest {

    @Test
    fun `catalog contains at least fifty levels`() {
        assertTrue(LevelCatalog.allLevelIds().size >= 50)
    }

    @Test
    fun `every catalog level passes full puzzle validation`() {
        for (levelId in LevelCatalog.allLevelIds()) {
            val board = LevelCatalog.boardFor(levelId)
            val result = PuzzleValidator.validate(board)
            assertTrue(
                "Level $levelId failed validation: $result",
                result is PuzzleValidationResult.Valid
            )
        }
    }

    @Test
    fun `no two catalog levels are identical boards`() {
        val boards = LevelCatalog.allLevelIds().map { LevelCatalog.boardFor(it) }
        assertEquals(boards.size, boards.toSet().size)
    }

    @Test
    fun `all five difficulty tiers are represented`() {
        val grouped = LevelCatalog.entriesGroupedByDifficulty()
        for (difficulty in Difficulty.entries) {
            assertTrue("No levels found for $difficulty", grouped[difficulty]?.isNotEmpty() == true)
        }
    }

    @Test
    fun `every level has a positive par time`() {
        for (levelId in LevelCatalog.allLevelIds()) {
            assertTrue(LevelCatalog.parTimeFor(levelId) > 0)
        }
    }

    @Test
    fun `an unknown level id falls back to level_001 rather than crashing`() {
        val board = LevelCatalog.boardFor("level_does_not_exist")
        assertEquals(LevelCatalog.boardFor("level_001"), board)
    }
}
