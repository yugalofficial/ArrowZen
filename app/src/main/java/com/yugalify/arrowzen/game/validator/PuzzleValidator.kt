package com.yugalify.arrowzen.game.validator

import com.yugalify.arrowzen.domain.model.Board

/** Reasons a puzzle can fail validation, so callers can log/report precisely. */
sealed interface PuzzleValidationResult {
    data class Valid(val solution: List<String>) : PuzzleValidationResult
    data object EmptyBoard : PuzzleValidationResult
    data object DuplicateArrowPosition : PuzzleValidationResult
    data object ArrowOutOfBounds : PuzzleValidationResult
    data object ArrowOverlapsObstacle : PuzzleValidationResult
    data object Unsolvable : PuzzleValidationResult
}

/**
 * Every puzzle — hand-authored starter level or procedurally generated —
 * must pass through here before it's presented to a player. This is what
 * Section 12/13 calls "never present an invalid or unsolvable puzzle."
 */
object PuzzleValidator {

    fun validate(board: Board): PuzzleValidationResult {
        if (board.arrows.isEmpty()) return PuzzleValidationResult.EmptyBoard

        val positions = board.arrows.map { it.row to it.column }
        if (positions.size != positions.toSet().size) {
            return PuzzleValidationResult.DuplicateArrowPosition
        }

        val outOfBounds = board.arrows.any { !board.isInBounds(it.row, it.column) }
        if (outOfBounds) return PuzzleValidationResult.ArrowOutOfBounds

        val overlapsObstacle = board.arrows.any { arrow ->
            board.obstacles.any { it.row == arrow.row && it.column == arrow.column }
        }
        if (overlapsObstacle) return PuzzleValidationResult.ArrowOverlapsObstacle

        val solution = PuzzleSolver.solve(board) ?: return PuzzleValidationResult.Unsolvable

        return PuzzleValidationResult.Valid(solution)
    }

    fun isValid(board: Board): Boolean = validate(board) is PuzzleValidationResult.Valid
}
