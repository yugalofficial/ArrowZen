package com.yugalify.arrowzen.game.difficulty

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.game.validator.PuzzleSolver
import org.junit.Assert.assertEquals
import org.junit.Test

class DifficultyCalculatorTest {

    @Test
    fun `single clear arrow rates as beginner`() {
        val board = Board(
            rows = 3,
            columns = 3,
            arrows = listOf(Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT))
        )
        val solution = requireNotNull(PuzzleSolver.solve(board))

        val difficulty = DifficultyCalculator.calculate(board, solution)

        assertEquals(Difficulty.BEGINNER, difficulty)
    }

    @Test
    fun `larger board with more arrows rates above beginner`() {
        val board = Board(
            rows = 7,
            columns = 7,
            arrows = listOf(
                Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT),
                Arrow(id = "a2", row = 1, column = 0, direction = Direction.RIGHT),
                Arrow(id = "a3", row = 2, column = 0, direction = Direction.RIGHT),
                Arrow(id = "a4", row = 3, column = 0, direction = Direction.RIGHT),
                Arrow(id = "a5", row = 4, column = 0, direction = Direction.RIGHT),
                Arrow(id = "a6", row = 5, column = 0, direction = Direction.RIGHT),
                Arrow(id = "a7", row = 6, column = 0, direction = Direction.RIGHT)
            )
        )
        val solution = requireNotNull(PuzzleSolver.solve(board))

        val difficulty = DifficultyCalculator.calculate(board, solution)

        assertEquals(true, difficulty.ordinal > Difficulty.BEGINNER.ordinal)
    }

    @Test
    fun `difficulty ordering is monotonic with the enum declaration order`() {
        // Sanity check that BEGINNER..EXPERT is declared in ascending order,
        // since calculate() relies on comparing ordinals in the app UI later.
        val expectedOrder = listOf(
            Difficulty.BEGINNER,
            Difficulty.EASY,
            Difficulty.MEDIUM,
            Difficulty.HARD,
            Difficulty.EXPERT
        )
        assertEquals(expectedOrder, Difficulty.entries)
    }
}
