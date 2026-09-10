package com.yugalify.arrowzen.game.generator

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.game.validator.PuzzleValidationResult
import com.yugalify.arrowzen.game.validator.PuzzleValidator
import kotlin.random.Random

/**
 * Deterministic procedural puzzle generator (Section 13).
 *
 * Strategy: for a given seed + difficulty, repeatedly propose a random
 * arrow layout sized for that difficulty tier and run it through
 * [PuzzleValidator]. Because [PuzzleValidator] calls the real
 * [com.yugalify.arrowzen.game.validator.PuzzleSolver], only genuinely
 * solvable boards are ever accepted — this is what Section 13 means by
 * "reverse construction" in spirit: we don't hand-place arrows in solve
 * order, but we never accept a board the solver can't independently prove
 * solvable. If no valid layout is found within [maxAttempts] (should be
 * effectively never at these board sizes), we fall back to
 * [safeFallback], a layout that is solvable by construction — arrows
 * placed in separate rows/columns so none can ever block another,
 * regardless of order. This guarantees Section 12's "never present an
 * unsolvable puzzle, never crash" requirement even in a worst case.
 */
object PuzzleGenerator {

    private const val MAX_ATTEMPTS = 300

    private data class Tier(val rows: Int, val columns: Int, val minArrows: Int, val maxArrows: Int)

    private val tiers: Map<Difficulty, Tier> = mapOf(
        Difficulty.BEGINNER to Tier(rows = 4, columns = 4, minArrows = 3, maxArrows = 4),
        Difficulty.EASY to Tier(rows = 4, columns = 4, minArrows = 4, maxArrows = 5),
        Difficulty.MEDIUM to Tier(rows = 5, columns = 5, minArrows = 5, maxArrows = 7),
        Difficulty.HARD to Tier(rows = 6, columns = 6, minArrows = 7, maxArrows = 9),
        Difficulty.EXPERT to Tier(rows = 7, columns = 7, minArrows = 9, maxArrows = 12)
    )

    fun generate(seed: Long, difficulty: Difficulty): Board {
        val random = Random(seed)
        val tier = tiers.getValue(difficulty)

        repeat(MAX_ATTEMPTS) {
            val candidate = proposeBoard(random, tier)
            if (PuzzleValidator.validate(candidate) is PuzzleValidationResult.Valid) {
                return candidate
            }
        }

        return safeFallback(tier)
    }

    private fun proposeBoard(random: Random, tier: Tier): Board {
        val arrowCount = random.nextInt(tier.minArrows, tier.maxArrows + 1)
        val usedCells = HashSet<Pair<Int, Int>>()
        val arrows = mutableListOf<Arrow>()

        var index = 0
        var guard = 0
        while (arrows.size < arrowCount && guard < arrowCount * 50) {
            guard++
            val row = random.nextInt(tier.rows)
            val column = random.nextInt(tier.columns)
            val cell = row to column
            if (cell in usedCells) continue

            usedCells += cell
            arrows += Arrow(
                id = "arrow_${index + 1}",
                row = row,
                column = column,
                direction = Direction.entries[random.nextInt(Direction.entries.size)]
            )
            index++
        }

        return Board(rows = tier.rows, columns = tier.columns, arrows = arrows)
    }

    /** Arrows placed one per row, all facing RIGHT with a clear lane — always solvable in any order. */
    private fun safeFallback(tier: Tier): Board {
        val arrowCount = tier.minArrows.coerceAtMost(tier.rows)
        val arrows = (0 until arrowCount).map { row ->
            Arrow(id = "arrow_${row + 1}", row = row, column = 0, direction = Direction.RIGHT)
        }
        return Board(rows = tier.rows, columns = tier.columns, arrows = arrows)
    }
}
