package com.yugalify.arrowzen.domain.model

/**
 * The direction an [Arrow] points toward. An arrow can only escape the board
 * by traveling in this direction toward the corresponding edge.
 */
enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    /** Row delta applied per step when moving in this direction. */
    val rowDelta: Int
        get() = when (this) {
            UP -> -1
            DOWN -> 1
            LEFT -> 0
            RIGHT -> 0
        }

    /** Column delta applied per step when moving in this direction. */
    val columnDelta: Int
        get() = when (this) {
            UP -> 0
            DOWN -> 0
            LEFT -> -1
            RIGHT -> 1
        }
}
