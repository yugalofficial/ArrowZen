package com.yugalify.arrowzen.game.validator

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.game.engine.MoveValidator

/**
 * Finds a valid escape order for a board, if one exists.
 *
 * Key insight: arrows never move except to instantly leave the board, so the
 * only thing that changes between states is *which* arrows are still present.
 * A state is therefore fully described by the set of remaining arrow ids —
 * we don't need to re-derive positions, since every arrow's original
 * position/direction is fixed. This turns solving into a search over subsets
 * of arrows rather than a full grid-state search, which stays fast even for
 * the 7x7 / dozen-arrow boards this game targets (Section 24).
 *
 * Failed subsets are memoized so the DFS never re-explores a dead branch,
 * keeping worst-case behavior well within budget-device limits (Section 13).
 */
object PuzzleSolver {

    /**
     * Returns an ordered list of arrow ids representing one valid solution,
     * or null if the board cannot be fully cleared from its current state.
     */
    fun solve(board: Board): List<String>? {
        val deadStates = HashSet<Set<String>>()
        val path = mutableListOf<String>()
        val solved = search(board, board.arrows, deadStates, path)
        return if (solved) path.toList() else null
    }

    private fun search(
        originalBoard: Board,
        remaining: List<Arrow>,
        deadStates: MutableSet<Set<String>>,
        path: MutableList<String>
    ): Boolean {
        if (remaining.isEmpty()) return true

        val stateKey = remaining.map { it.id }.toHashSet()
        if (stateKey in deadStates) return false

        val currentBoard = originalBoard.copy(arrows = remaining)
        val candidates = MoveValidator.availableMoves(currentBoard)

        for (candidate in candidates) {
            val nextRemaining = remaining.filterNot { it.id == candidate.id }
            path.add(candidate.id)
            if (search(originalBoard, nextRemaining, deadStates, path)) {
                return true
            }
            path.removeAt(path.lastIndex)
        }

        deadStates.add(stateKey)
        return false
    }
}
