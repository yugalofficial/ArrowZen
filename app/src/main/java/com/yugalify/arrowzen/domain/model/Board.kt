package com.yugalify.arrowzen.domain.model

/** A fixed, non-arrow blocking tile on the board. */
data class Obstacle(
    val row: Int,
    val column: Int
)

/**
 * Immutable snapshot of the puzzle board. [arrows] and [obstacles] describe
 * everything a move-validation pass needs to know.
 *
 * Board sizes are intentionally flexible (Section 24): 4x4 up to 7x7 for v1,
 * larger sizes can be added later without changing this model.
 */
data class Board(
    val rows: Int,
    val columns: Int,
    val arrows: List<Arrow>,
    val obstacles: List<Obstacle> = emptyList()
) {
    init {
        require(rows > 0 && columns > 0) { "Board dimensions must be positive." }
    }

    fun isInBounds(row: Int, column: Int): Boolean =
        row in 0 until rows && column in 0 until columns

    fun arrowAt(row: Int, column: Int): Arrow? =
        arrows.firstOrNull { it.row == row && it.column == column }

    fun hasObstacleAt(row: Int, column: Int): Boolean =
        obstacles.any { it.row == row && it.column == column }

    fun isEmpty(row: Int, column: Int): Boolean =
        arrowAt(row, column) == null && !hasObstacleAt(row, column)

    /** True once every arrow has escaped the board. */
    val isSolved: Boolean
        get() = arrows.isEmpty()
}
