package com.yugalify.arrowzen.data.database.entity

import androidx.room.Entity

/**
 * One row per date the player completed (or attempted) the Daily Challenge.
 * [dateKey] is the device-local date as "YYYYMMDD" — the same string used
 * to derive the deterministic puzzle seed (Section 9), so lookups and
 * generation always agree on "today."
 */
@Entity(tableName = "daily_challenge", primaryKeys = ["dateKey"])
data class DailyChallengeEntity(
    val dateKey: String,
    val isCompleted: Boolean = false,
    val completionTimeMillis: Long? = null,
    val mistakes: Int = 0,
    val hintsUsed: Int = 0
)
