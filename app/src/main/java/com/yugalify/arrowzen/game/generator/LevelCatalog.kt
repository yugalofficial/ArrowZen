package com.yugalify.arrowzen.game.generator

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.domain.model.Obstacle
import com.yugalify.arrowzen.game.difficulty.DifficultyCalculator
import com.yugalify.arrowzen.game.validator.PuzzleSolver
import com.yugalify.arrowzen.game.validator.PuzzleValidator

/** Everything the rest of the app needs to know about one catalog level. */
data class LevelCatalogEntry(
    val id: String,
    val board: Board,
    val difficulty: Difficulty,
    val parTimeMillis: Long
)

/**
 * The full Classic Mode level catalog (Section 40): at least 50 levels,
 * grouped by difficulty, every single one validated solvable before it's
 * exposed to the rest of the app.
 *
 * levels 1-3 are hand-authored tutorial levels (Section 36 — Level 1 teaches
 * tapping, Level 2 teaches blocking, Level 3 teaches sequence). Levels 4-50
 * are produced by [PuzzleGenerator] with a fixed seed per level index, so
 * the catalog is 100% deterministic and reproducible — the same "level_037"
 * is the same puzzle on every device and every run, forever, without
 * shipping 50 hand-typed boards that are one typo away from being
 * unsolvable (see the level_002 obstacle bug caught during Phase 2).
 *
 * The catalog is built once, eagerly, at class-init time — 50 boards at
 * these sizes solve in well under a second combined, so there's no need for
 * background loading or lazy-per-level generation.
 */
object LevelCatalog {

    private val entries: LinkedHashMap<String, LevelCatalogEntry> = buildCatalog()

    fun boardFor(levelId: String): Board = entries[levelId]?.board ?: entries.getValue("level_001").board

    fun parTimeFor(levelId: String): Long = entries[levelId]?.parTimeMillis ?: 30_000L

    fun difficultyFor(levelId: String): Difficulty = entries[levelId]?.difficulty ?: Difficulty.BEGINNER

    fun allLevelIds(): List<String> = entries.keys.toList()

    fun entriesGroupedByDifficulty(): Map<Difficulty, List<LevelCatalogEntry>> =
        entries.values.groupBy { it.difficulty }

    /** True only if the board passes full structural + solvability validation. */
    fun isSolvable(board: Board): Boolean = PuzzleValidator.isValid(board)

    private fun buildCatalog(): LinkedHashMap<String, LevelCatalogEntry> {
        val map = linkedMapOf<String, LevelCatalogEntry>()

        for (tutorial in tutorialBoards()) {
            map[tutorial.first] = toEntry(tutorial.first, tutorial.second)
        }

        // 47 generated levels, difficulty tier growing with level number so
        // the classic-mode progression curve feels intentional (Section 8).
        val bands = listOf(
            Difficulty.BEGINNER to 8,
            Difficulty.EASY to 9,
            Difficulty.MEDIUM to 10,
            Difficulty.HARD to 10,
            Difficulty.EXPERT to 10
        )

        var levelNumber = tutorialBoards().size + 1
        for ((difficulty, count) in bands) {
            repeat(count) {
                val seed = levelNumber.toLong()
                val board = PuzzleGenerator.generate(seed, difficulty)
                val id = "level_%03d".format(levelNumber)
                map[id] = toEntry(id, board)
                levelNumber++
            }
        }

        return map
    }

    private fun toEntry(id: String, board: Board): LevelCatalogEntry {
        val solution = requireNotNull(PuzzleSolver.solve(board)) {
            "Catalog level $id failed to validate as solvable — this should be unreachable, " +
                "since PuzzleGenerator only ever returns validated or safe-fallback boards."
        }
        val difficulty = DifficultyCalculator.calculate(board, solution)
        val parTime = 8_000L + board.arrows.size * 4_000L
        return LevelCatalogEntry(id = id, board = board, difficulty = difficulty, parTimeMillis = parTime)
    }

    /** Level 1 teaches tapping. Level 2 teaches blocking. Level 3 teaches sequence (Section 36). */
    private fun tutorialBoards(): List<Pair<String, Board>> = listOf(
        "level_001" to Board(
            rows = 4,
            columns = 4,
            arrows = listOf(
                Arrow(id = "arrow_1", row = 0, column = 0, direction = Direction.RIGHT),
                Arrow(id = "arrow_2", row = 1, column = 3, direction = Direction.DOWN),
                Arrow(id = "arrow_3", row = 3, column = 1, direction = Direction.LEFT)
            )
        ),
        "level_002" to Board(
            rows = 4,
            columns = 4,
            arrows = listOf(
                Arrow(id = "arrow_1", row = 0, column = 0, direction = Direction.RIGHT),
                Arrow(id = "arrow_2", row = 0, column = 2, direction = Direction.RIGHT),
                Arrow(id = "arrow_3", row = 2, column = 3, direction = Direction.DOWN),
                Arrow(id = "arrow_4", row = 3, column = 0, direction = Direction.UP)
            ),
            obstacles = listOf(Obstacle(row = 2, column = 2))
        ),
        "level_003" to Board(
            rows = 5,
            columns = 5,
            arrows = listOf(
                Arrow(id = "arrow_1", row = 0, column = 4, direction = Direction.DOWN),
                Arrow(id = "arrow_2", row = 2, column = 4, direction = Direction.DOWN),
                Arrow(id = "arrow_3", row = 4, column = 0, direction = Direction.RIGHT),
                Arrow(id = "arrow_4", row = 4, column = 2, direction = Direction.RIGHT),
                Arrow(id = "arrow_5", row = 0, column = 0, direction = Direction.LEFT)
            )
        )
    )
}
