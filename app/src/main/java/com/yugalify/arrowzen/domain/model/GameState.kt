package com.yugalify.arrowzen.domain.model

enum class Difficulty {
    BEGINNER,
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}

/** One entry in the move-history stack, used to support undo (Section 15). */
data class MoveRecord(
    val arrow: Arrow,
    val boardBeforeMove: Board
)

/**
 * Full mutable-in-spirit (but immutable-in-implementation) state of an active
 * puzzle session. ViewModels hold this in a StateFlow and replace it wholesale
 * on every player action rather than mutating it in place.
 */
data class GameState(
    val levelId: String,
    val difficulty: Difficulty,
    val board: Board,
    val originalBoard: Board,
    val moveHistory: List<MoveRecord> = emptyList(),
    val mistakes: Int = 0,
    val hintsUsed: Int = 0,
    val elapsedMillis: Long = 0L,
    val isComplete: Boolean = false
) {
    val canUndo: Boolean get() = moveHistory.isNotEmpty()

    val remainingArrows: Int get() = board.arrows.size
}
