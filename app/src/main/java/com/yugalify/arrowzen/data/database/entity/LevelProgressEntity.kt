package com.yugalify.arrowzen.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per puzzle level the player has ever attempted. Rows are created
 * lazily on first attempt rather than pre-populated for all levels, so
 * expanding the level catalog (Section 40) never requires a migration.
 */
@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: String,
    val bestStars: Int = 0,
    val bestTimeMillis: Long? = null,
    val fewestMistakes: Int? = null,
    val hintsUsedOnBest: Int = 0,
    val completionCount: Int = 0,
    val isUnlocked: Boolean = false
)
