package com.yugalify.arrowzen.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table (fixed [id] = 0) backing the Statistics screen (Section 30). */
@Entity(tableName = "game_statistics")
data class GameStatisticsEntity(
    @PrimaryKey val id: Int = 0,
    val totalArrowsEscaped: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalHintsUsed: Long = 0,
    val totalPlayTimeMillis: Long = 0
)
