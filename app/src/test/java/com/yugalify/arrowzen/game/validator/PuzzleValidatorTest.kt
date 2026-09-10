package com.yugalify.arrowzen.game.validator

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.domain.model.Obstacle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleValidatorTest {

    @Test
    fun `valid solvable board passes validation and returns its solution`() {
        val board = Board(
            rows = 4,
            columns = 4,
            arrows = listOf(Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT))
        )

        val result = PuzzleValidator.validate(board)

        assertTrue(result is PuzzleValidationResult.Valid)
    }

    @Test
    fun `empty board is rejected`() {
        val board = Board(rows = 4, columns = 4, arrows = emptyList())

        val result = PuzzleValidator.validate(board)

        assertEquals(PuzzleValidationResult.EmptyBoard, result)
    }

    @Test
    fun `two arrows sharing a position are rejected`() {
        val a1 = Arrow(id = "a1", row = 1, column = 1, direction = Direction.RIGHT)
        val a2 = Arrow(id = "a2", row = 1, column = 1, direction = Direction.LEFT)
        val board = Board(rows = 4, columns = 4, arrows = listOf(a1, a2))

        val result = PuzzleValidator.validate(board)

        assertEquals(PuzzleValidationResult.DuplicateArrowPosition, result)
    }

    @Test
    fun `arrow placed out of bounds is rejected`() {
        val board = Board(
            rows = 3,
            columns = 3,
            arrows = listOf(Arrow(id = "a1", row = 5, column = 5, direction = Direction.RIGHT))
        )

        val result = PuzzleValidator.validate(board)

        assertEquals(PuzzleValidationResult.ArrowOutOfBounds, result)
    }

    @Test
    fun `arrow overlapping an obstacle is rejected`() {
        val board = Board(
            rows = 3,
            columns = 3,
            arrows = listOf(Arrow(id = "a1", row = 1, column = 1, direction = Direction.RIGHT)),
            obstacles = listOf(Obstacle(row = 1, column = 1))
        )

        val result = PuzzleValidator.validate(board)

        assertEquals(PuzzleValidationResult.ArrowOverlapsObstacle, result)
    }

    @Test
    fun `unsolvable board is rejected with Unsolvable`() {
        val a1 = Arrow(id = "a1", row = 1, column = 0, direction = Direction.RIGHT)
        val a2 = Arrow(id = "a2", row = 1, column = 2, direction = Direction.LEFT)
        val board = Board(rows = 3, columns = 3, arrows = listOf(a1, a2))

        val result = PuzzleValidator.validate(board)

        assertEquals(PuzzleValidationResult.Unsolvable, result)
    }

    @Test
    fun `isValid convenience function matches validate result`() {
        val solvable = Board(
            rows = 3,
            columns = 3,
            arrows = listOf(Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT))
        )
        val unsolvable = Board(rows = 3, columns = 3, arrows = emptyList())

        assertTrue(PuzzleValidator.isValid(solvable))
        assertEquals(false, PuzzleValidator.isValid(unsolvable))
    }
}
