package com.yugalify.arrowzen.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table (fixed [id] = 0) holding the player's overall mastery
 * progression. Kept separate from [GameStatisticsEntity] so profile/rank
 * concerns don't get tangled with raw counters.
 */
@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 0,
    val totalPuzzlesCompleted: Int = 0,
    val perfectCompletions: Int = 0,
    val longestStreak: Int = 0,
    val hardestDifficultyCompleted: String? = null
)
