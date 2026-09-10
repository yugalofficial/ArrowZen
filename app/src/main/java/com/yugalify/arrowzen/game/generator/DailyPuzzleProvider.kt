package com.yugalify.arrowzen.game.generator

import com.yugalify.arrowzen.core.constants.AppConstants
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Difficulty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Deterministic Daily Challenge puzzle (Section 9): the same calendar date
 * always produces the same board, on every device, forever -- with zero
 * server dependency. The seed is derived from `YYYYMMDD + salt`, exactly as
 * the spec describes, run through Java's `String.hashCode()`. That
 * algorithm is fixed by the Java Language Specification (not
 * implementation-defined), so it is guaranteed identical across JVM
 * versions and devices -- which is exactly the guarantee "same date, same
 * puzzle" needs.
 */
object DailyPuzzleProvider {

    private val dateKeyFormatter: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE // yyyyMMdd

    fun todayDateKey(): String = dateKeyFor(LocalDate.now())

    fun dateKeyFor(date: LocalDate): String = date.format(dateKeyFormatter)

    fun boardFor(dateKey: String): Board {
        val seed = (dateKey + AppConstants.DAILY_CHALLENGE_SALT).hashCode().toLong()
        return PuzzleGenerator.generate(seed, Difficulty.MEDIUM)
    }
}
