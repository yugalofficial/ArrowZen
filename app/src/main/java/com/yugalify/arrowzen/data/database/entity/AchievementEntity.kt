package com.yugalify.arrowzen.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Row shape matches Section 19's spec exactly: id, title, description,
 * icon reference, progress, target, unlocked. New achievements can be added
 * by inserting new rows (via a REPLACE-on-conflict seed step) without a
 * schema migration.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconReference: String,
    val progress: Int = 0,
    val target: Int,
    val isUnlocked: Boolean = false,
    val unlockedAtMillis: Long? = null
)
