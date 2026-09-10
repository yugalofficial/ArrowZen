package com.yugalify.arrowzen.game.engine

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.domain.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private fun freshState(): GameState {
        val arrow = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val board = Board(rows = 2, columns = 2, arrows = listOf(arrow))
        return GameState(
            levelId = "level_test",
            difficulty = Difficulty.BEGINNER,
            board = board,
            originalBoard = board
        )
    }

    @Test
    fun `valid move removes arrow and marks board complete when it was the last one`() {
        val state = freshState()
        val arrow = state.board.arrows.first()

        val result = GameEngine.attemptMove(state, arrow)

        assertTrue(result is MoveResult.Valid)
        val newState = (result as MoveResult.Valid).newState
        assertTrue(newState.isComplete)
        assertEquals(0, newState.remainingArrows)
    }

    @Test
    fun `blocked move increments mistakes and keeps arrow on board`() {
        val mover = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val blocker = Arrow(id = "a2", row = 0, column = 1, direction = Direction.DOWN)
        val board = Board(rows = 2, columns = 2, arrows = listOf(mover, blocker))
        val state = GameState(
            levelId = "level_test",
            difficulty = Difficulty.BEGINNER,
            board = board,
            originalBoard = board
        )

        val result = GameEngine.attemptMove(state, mover)

        assertTrue(result is MoveResult.Blocked)
        val newState = (result as MoveResult.Blocked).state
        assertEquals(1, newState.mistakes)
        assertEquals(2, newState.remainingArrows)
    }

    @Test
    fun `undo restores the board from before the last move`() {
        val state = freshState()
        val arrow = state.board.arrows.first()
        val afterMove = (GameEngine.attemptMove(state, arrow) as MoveResult.Valid).newState

        val undone = GameEngine.undo(afterMove)

        assertEquals(1, undone.remainingArrows)
        assertFalse(undone.isComplete)
        assertTrue(undone.moveHistory.isEmpty())
    }

    @Test
    fun `undo with no history is a no-op`() {
        val state = freshState()

        val undone = GameEngine.undo(state)

        assertEquals(state, undone)
    }

    @Test
    fun `restart resets board, mistakes, hints and history`() {
        val state = freshState()
        val arrow = state.board.arrows.first()
        val afterMove = (GameEngine.attemptMove(state, arrow) as MoveResult.Valid).newState
            .copy(hintsUsed = 2)

        val restarted = GameEngine.restart(afterMove)

        assertEquals(state.originalBoard, restarted.board)
        assertEquals(0, restarted.mistakes)
        assertEquals(0, restarted.hintsUsed)
        assertTrue(restarted.moveHistory.isEmpty())
        assertFalse(restarted.isComplete)
    }

    @Test
    fun `attemptMove on an already complete state does not change it`() {
        val arrow = Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT)
        val emptyBoard = Board(rows = 2, columns = 2, arrows = emptyList())
        val completeState = GameState(
            levelId = "level_test",
            difficulty = Difficulty.BEGINNER,
            board = emptyBoard,
            originalBoard = emptyBoard,
            isComplete = true
        )

        val result = GameEngine.attemptMove(completeState, arrow)

        assertTrue(result is MoveResult.AlreadyComplete)
    }
}
