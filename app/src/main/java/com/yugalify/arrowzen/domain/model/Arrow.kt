package com.yugalify.arrowzen.domain.model

/** Type of arrow tile. Reserved for future puzzle mechanics (Section 58). */
enum class ArrowType {
    STANDARD,
    LOCKED,
    MULTI_STEP
}

/**
 * Immutable representation of a single arrow on the board.
 * [row] and [column] are zero-indexed board coordinates.
 */
data class Arrow(
    val id: String,
    val row: Int,
    val column: Int,
    val direction: Direction,
    val type: ArrowType = ArrowType.STANDARD
) {
    /** Returns a copy of this arrow moved one step in its own direction. */
    fun stepped(): Arrow = copy(
        row = row + direction.rowDelta,
        column = column + direction.columnDelta
    )
}
