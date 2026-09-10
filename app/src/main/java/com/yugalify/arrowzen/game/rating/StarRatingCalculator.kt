package com.yugalify.arrowzen.game.rating

/**
 * Implements the three-star criteria from Section 17:
 *  - 3 stars: no hints, zero mistakes, finished within the level's par time
 *  - 2 stars: completed with minor mistakes or slower than par, but no hints
 *             burned the run entirely
 *  - 1 star: completed at all
 *
 * Kept as a pure function (no Android deps) so it's trivially unit testable
 * and reusable from both the live game screen and any future replay/audit
 * tooling.
 */
object StarRatingCalculator {

    fun calculateStars(
        mistakes: Int,
        hintsUsed: Int,
        elapsedMillis: Long,
        parTimeMillis: Long
    ): Int {
        val perfect = mistakes == 0 && hintsUsed == 0 && elapsedMillis <= parTimeMillis
        if (perfect) return 3

        val minorMistakesOnly = hintsUsed == 0 && mistakes <= 2
        val withinGraceWindow = elapsedMillis <= (parTimeMillis * 3) / 2
        if (minorMistakesOnly || withinGraceWindow) return 2

        return 1
    }
}
