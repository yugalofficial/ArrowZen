package com.yugalify.arrowzen.game.engine

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.domain.model.Obstacle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveValidatorTest {

    @Test
    fun `arrow pointing right escapes when path to right edge is clear`() {
        val arrow = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val board = Board(rows = 4, columns = 4, arrows = listOf(arrow))

        assertTrue(MoveValidator.canEscape(board, arrow))
    }

    @Test
    fun `arrow pointing right is blocked by another arrow in its path`() {
        val mover = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val blocker = Arrow(id = "a2", row = 0, column = 2, direction = Direction.DOWN)
        val board = Board(rows = 4, columns = 4, arrows = listOf(mover, blocker))

        assertFalse(MoveValidator.canEscape(board, mover))
    }

    @Test
    fun `arrow pointing left escapes when path is clear`() {
        val arrow = Arrow(id = "a1", row = 2, column = 3, direction = Direction.LEFT)
        val board = Board(rows = 4, columns = 4, arrows = listOf(arrow))

        assertTrue(MoveValidator.canEscape(board, arrow))
    }

    @Test
    fun `arrow pointing up is blocked by an obstacle`() {
        val arrow = Arrow(id = "a1", row = 3, column = 1, direction = Direction.UP)
        val board = Board(
            rows = 4,
            columns = 4,
            arrows = listOf(arrow),
            obstacles = listOf(Obstacle(row = 1, column = 1))
        )

        assertFalse(MoveValidator.canEscape(board, arrow))
    }

    @Test
    fun `arrow pointing down escapes when path to bottom edge is clear`() {
        val arrow = Arrow(id = "a1", row = 0, column = 1, direction = Direction.DOWN)
        val board = Board(rows = 4, columns = 4, arrows = listOf(arrow))

        assertTrue(MoveValidator.canEscape(board, arrow))
    }

    @Test
    fun `arrow already on the edge facing outward can always escape`() {
        val arrow = Arrow(id = "a1", row = 0, column = 3, direction = Direction.RIGHT)
        val board = Board(rows = 4, columns = 4, arrows = listOf(arrow))

        assertTrue(MoveValidator.canEscape(board, arrow))
    }

    @Test
    fun `applyMove removes the arrow when the move is valid`() {
        val arrow = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val board = Board(rows = 4, columns = 4, arrows = listOf(arrow))

        val result = MoveValidator.applyMove(board, arrow)

        assertTrue(result.arrows.isEmpty())
        assertTrue(result.isSolved)
    }

    @Test
    fun `applyMove leaves the board unchanged when the move is blocked`() {
        val mover = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val blocker = Arrow(id = "a2", row = 0, column = 2, direction = Direction.DOWN)
        val board = Board(rows = 4, columns = 4, arrows = listOf(mover, blocker))

        val result = MoveValidator.applyMove(board, mover)

        assertEquals(board, result)
    }

    @Test
    fun `availableMoves only returns arrows with a clear path`() {
        val clear = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val blocked = Arrow(id = "a2", row = 1, column = 0, direction = Direction.RIGHT)
        val blocker = Arrow(id = "a3", row = 1, column = 2, direction = Direction.DOWN)
        val board = Board(rows = 4, columns = 4, arrows = listOf(clear, blocked, blocker))

        val available = MoveValidator.availableMoves(board)

        assertTrue(available.contains(clear))
        assertFalse(available.contains(blocked))
    }
}
