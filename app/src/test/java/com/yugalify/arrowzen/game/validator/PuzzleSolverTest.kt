package com.yugalify.arrowzen.game.validator

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.domain.model.Obstacle
import com.yugalify.arrowzen.game.engine.MoveValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleSolverTest {

    @Test
    fun `solves a trivially clear single-arrow board`() {
        val board = Board(
            rows = 3,
            columns = 3,
            arrows = listOf(Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT))
        )

        val solution = PuzzleSolver.solve(board)

        assertEquals(listOf("a1"), solution)
    }

    @Test
    fun `finds the required order when arrows block each other`() {
        // a2 must escape before a1 can move right.
        val a1 = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val a2 = Arrow(id = "a2", row = 0, column = 2, direction = Direction.DOWN)
        val board = Board(rows = 3, columns = 3, arrows = listOf(a1, a2))

        val solution = PuzzleSolver.solve(board)

        requireNotNull(solution)
        assertEquals(listOf("a2", "a1"), solution)
    }

    @Test
    fun `returns null for an unsolvable board`() {
        // Two arrows facing each other, each permanently blocking the other,
        // with obstacles sealing every other exit.
        val a1 = Arrow(id = "a1", row = 1, column = 0, direction = Direction.RIGHT)
        val a2 = Arrow(id = "a2", row = 1, column = 2, direction = Direction.LEFT)
        val board = Board(
            rows = 3,
            columns = 3,
            arrows = listOf(a1, a2),
            obstacles = listOf(
                Obstacle(row = 0, column = 0), Obstacle(row = 0, column = 1), Obstacle(row = 0, column = 2),
                Obstacle(row = 2, column = 0), Obstacle(row = 2, column = 1), Obstacle(row = 2, column = 2)
            )
        )

        val solution = PuzzleSolver.solve(board)

        assertNull(solution)
    }

    @Test
    fun `every returned solution is replayable move by move`() {
        val a1 = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val a2 = Arrow(id = "a2", row = 1, column = 3, direction = Direction.DOWN)
        val a3 = Arrow(id = "a3", row = 3, column = 1, direction = Direction.LEFT)
        val board = Board(rows = 4, columns = 4, arrows = listOf(a1, a2, a3))

        val solution = PuzzleSolver.solve(board)
        requireNotNull(solution)

        var current = board
        for (arrowId in solution) {
            val arrow = current.arrows.first { it.id == arrowId }
            assertTrue("Move $arrowId should be legal at this point", MoveValidator.canEscape(current, arrow))
            current = MoveValidator.applyMove(current, arrow)
        }
        assertTrue(current.isSolved)
    }
}
