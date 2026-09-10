package com.yugalify.arrowzen.game.difficulty

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.game.engine.MoveValidator

/**
 * Estimates puzzle difficulty from a known solution path.
 *
 * The core signal is the *average branching factor* along the solution: at
 * each step, how many arrows could legally move, versus how many actually
 * lead to the solution. A puzzle where only one arrow is ever escapable at
 * a time is mechanically constrained but not necessarily "hard" to reason
 * about; a puzzle with many available-but-wrong choices at each step is
 * harder because the player must actually plan ahead rather than just take
 * the only option. We combine that with raw arrow count and board area so a
 * small board with high branching doesn't get miscategorized as trivial.
 */
object DifficultyCalculator {

    fun calculate(board: Board, solutionOrder: List<String>): Difficulty {
        val avgBranching = averageBranchingFactor(board, solutionOrder)
        val arrowCount = board.arrows.size
        val boardArea = board.rows * board.columns

        // Weighted score: more arrows, bigger boards, and higher average
        // branching factor all push difficulty up. Thresholds below were
        // calibrated against the actual score distribution produced by
        // PuzzleGenerator across all five board-size tiers (Section 24:
        // 4x4 up to 7x7), not chosen arbitrarily — see docs/ARCHITECTURE.md
        // for the calibration data. Board area alone can contribute several
        // points even on a 4x4 board, so thresholds must be wide enough to
        // still classify small boards as BEGINNER/EASY.
        val score = (arrowCount * 1.5) + (boardArea * 0.2) + (avgBranching * 2.0)

        return when {
            score < 13.0 -> Difficulty.BEGINNER
            score < 17.0 -> Difficulty.EASY
            score < 22.0 -> Difficulty.MEDIUM
            score < 29.0 -> Difficulty.HARD
            else -> Difficulty.EXPERT
        }
    }

    /** Replays [solutionOrder] and averages how many arrows were escapable at each step. */
    private fun averageBranchingFactor(board: Board, solutionOrder: List<String>): Double {
        if (solutionOrder.isEmpty()) return 0.0

        var remaining: List<Arrow> = board.arrows
        var totalChoices = 0

        for (arrowId in solutionOrder) {
            val currentBoard = board.copy(arrows = remaining)
            val choices = MoveValidator.availableMoves(currentBoard).size
            totalChoices += choices
            remaining = remaining.filterNot { it.id == arrowId }
        }

        return totalChoices.toDouble() / solutionOrder.size
    }
}
