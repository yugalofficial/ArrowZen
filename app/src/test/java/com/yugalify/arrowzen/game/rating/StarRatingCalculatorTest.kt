package com.yugalify.arrowzen.game.rating

import org.junit.Assert.assertEquals
import org.junit.Test

class StarRatingCalculatorTest {

    private val parTime = 30_000L

    @Test
    fun `no mistakes, no hints, within par time earns three stars`() {
        val stars = StarRatingCalculator.calculateStars(
            mistakes = 0, hintsUsed = 0, elapsedMillis = 20_000L, parTimeMillis = parTime
        )
        assertEquals(3, stars)
    }

    @Test
    fun `exactly at par time still earns three stars`() {
        val stars = StarRatingCalculator.calculateStars(
            mistakes = 0, hintsUsed = 0, elapsedMillis = parTime, parTimeMillis = parTime
        )
        assertEquals(3, stars)
    }

    @Test
    fun `one mistake over par time drops to two stars`() {
        val stars = StarRatingCalculator.calculateStars(
            mistakes = 1, hintsUsed = 0, elapsedMillis = 40_000L, parTimeMillis = parTime
        )
        assertEquals(2, stars)
    }

    @Test
    fun `using a hint but finishing well within grace window earns two stars`() {
        val stars = StarRatingCalculator.calculateStars(
            mistakes = 0, hintsUsed = 1, elapsedMillis = 25_000L, parTimeMillis = parTime
        )
        assertEquals(2, stars)
    }

    @Test
    fun `many mistakes and hints and slow completion earns one star`() {
        val stars = StarRatingCalculator.calculateStars(
            mistakes = 5, hintsUsed = 3, elapsedMillis = 90_000L, parTimeMillis = parTime
        )
        assertEquals(1, stars)
    }
}
