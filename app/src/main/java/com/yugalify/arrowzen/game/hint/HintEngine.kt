package com.yugalify.arrowzen.game.hint

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.game.validator.PuzzleSolver

/** The four hint tiers described in Section 14, from vaguest to most direct. */
sealed interface Hint {
    /** Tier 1: a text clue, no board reference. */
    data class TextClue(val message: String) : Hint

    /** Tier 2: a general board region (quadrant) to look at. */
    data class RegionHighlight(val message: String, val rows: IntRange, val columns: IntRange) : Hint

    /** Tier 3: one specific arrow that is likely (and, here, guaranteed) correct. */
    data class ArrowHighlight(val arrow: Arrow) : Hint

    /** Tier 4: reveal the move outright. */
    data class RevealMove(val arrow: Arrow) : Hint

    data object NoHintAvailable : Hint
}

/**
 * Never suggests an invalid move (Section 14's hard requirement): every tier
 * above 1 is derived from a real solution computed by [PuzzleSolver] against
 * the board's *current* state, not a cached solution from when the puzzle
 * started — so a hint stays correct no matter what order the player has
 * already played moves in.
 */
object HintEngine {

    fun getHint(board: Board, tier: Int): Hint {
        if (board.arrows.isEmpty()) return Hint.NoHintAvailable
        val solution = PuzzleSolver.solve(board) ?: return Hint.NoHintAvailable
        val nextArrowId = solution.firstOrNull() ?: return Hint.NoHintAvailable
        val nextArrow = board.arrows.first { it.id == nextArrowId }

        return when (tier) {
            1 -> Hint.TextClue("Look for an arrow with a clear path to the edge.")
            2 -> regionHint(board, nextArrow)
            3 -> Hint.ArrowHighlight(nextArrow)
            else -> Hint.RevealMove(nextArrow)
        }
    }

    private fun regionHint(board: Board, arrow: Arrow): Hint {
        val midRow = board.rows / 2
        val midColumn = board.columns / 2
        val isTop = arrow.row < midRow
        val isLeft = arrow.column < midColumn

        val rows = if (isTop) 0 until midRow else midRow until board.rows
        val columns = if (isLeft) 0 until midColumn else midColumn until board.columns
        val vertical = if (isTop) "top" else "bottom"
        val horizontal = if (isLeft) "left" else "right"

        return Hint.RegionHighlight(
            message = "Check the $vertical-$horizontal of the board.",
            rows = rows,
            columns = columns
        )
    }
}
