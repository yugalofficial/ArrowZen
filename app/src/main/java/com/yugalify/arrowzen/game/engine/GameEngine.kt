package com.yugalify.arrowzen.game.engine

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.GameState
import com.yugalify.arrowzen.domain.model.MoveRecord

/** Result of attempting a move, so callers can trigger the right UI feedback (Section 7/62). */
sealed interface MoveResult {
    data class Valid(val newState: GameState) : MoveResult
    data class Blocked(val state: GameState) : MoveResult
    data class AlreadyComplete(val state: GameState) : MoveResult
}

/**
 * Stateless orchestration layer over [MoveValidator]. ViewModels call these
 * functions and replace their held [GameState] with the returned value —
 * no in-place mutation happens here, which keeps undo/restart trivial and
 * the engine trivially unit-testable.
 */
object GameEngine {

    fun attemptMove(state: GameState, arrow: Arrow): MoveResult {
        if (state.isComplete) return MoveResult.AlreadyComplete(state)

        if (!MoveValidator.canEscape(state.board, arrow)) {
            return MoveResult.Blocked(state.copy(mistakes = state.mistakes + 1))
        }

        val boardBeforeMove = state.board
        val newBoard = MoveValidator.applyMove(state.board, arrow)
        val newHistory = state.moveHistory + MoveRecord(arrow = arrow, boardBeforeMove = boardBeforeMove)

        val newState = state.copy(
            board = newBoard,
            moveHistory = newHistory,
            isComplete = newBoard.isSolved
        )
        return MoveResult.Valid(newState)
    }

    /** Restores the board to just before the last move. No-op if there is nothing to undo. */
    fun undo(state: GameState): GameState {
        val lastMove = state.moveHistory.lastOrNull() ?: return state
        return state.copy(
            board = lastMove.boardBeforeMove,
            moveHistory = state.moveHistory.dropLast(1),
            isComplete = false
        )
    }

    /** Resets to the original board while preserving best-record history elsewhere (Section 16). */
    fun restart(state: GameState): GameState = state.copy(
        board = state.originalBoard,
        moveHistory = emptyList(),
        mistakes = 0,
        hintsUsed = 0,
        elapsedMillis = 0L,
        isComplete = false
    )
}
