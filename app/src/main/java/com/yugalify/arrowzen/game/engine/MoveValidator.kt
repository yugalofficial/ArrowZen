package com.yugalify.arrowzen.game.engine

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board

/**
 * Pure, UI-independent move validation (Section 7). Given a board and an
 * arrow on that board, determines whether the arrow's path to the relevant
 * edge is completely clear of other arrows and obstacles.
 *
 * This class has no dependency on Android, Compose, or any other framework,
 * so it can be unit tested in plain JVM tests.
 */
object MoveValidator {

    /**
     * Returns true if [arrow] can escape [board] right now: every cell between
     * the arrow's current position (exclusive) and the board edge in its
     * direction (inclusive) must be free of other arrows and obstacles.
     */
    fun canEscape(board: Board, arrow: Arrow): Boolean {
        var row = arrow.row + arrow.direction.rowDelta
        var column = arrow.column + arrow.direction.columnDelta

        // Walking off the board immediately (arrow already on the edge facing out)
        // is a valid, immediate escape.
        while (board.isInBounds(row, column)) {
            if (!board.isEmpty(row, column)) {
                return false
            }
            row += arrow.direction.rowDelta
            column += arrow.direction.columnDelta
        }
        return true
    }

    /** Convenience overload that looks the arrow up by id first. */
    fun canEscape(board: Board, arrowId: String): Boolean {
        val arrow = board.arrows.firstOrNull { it.id == arrowId } ?: return false
        return canEscape(board, arrow)
    }

    /**
     * Returns the board that results from removing [arrow], or the same board
     * unchanged if the move is not currently valid. Callers should check
     * [canEscape] first if they need to distinguish "invalid move" from
     * "valid move that happened to change nothing" (which never occurs here,
     * but keeps the contract explicit).
     */
    fun applyMove(board: Board, arrow: Arrow): Board {
        if (!canEscape(board, arrow)) return board
        return board.copy(arrows = board.arrows.filterNot { it.id == arrow.id })
    }

    /** All arrows on [board] that could legally escape right now. */
    fun availableMoves(board: Board): List<Arrow> =
        board.arrows.filter { canEscape(board, it) }
}
