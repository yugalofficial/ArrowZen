package com.yugalify.arrowzen.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.challengeDataStore by preferencesDataStore(name = "arrowzen_challenges")

enum class ChallengeType { PERFECT_RUN, NO_HINT, SPEED }

data class ChallengeRecord(
    val everCompleted: Boolean = false,
    val bestTimeMillis: Long? = null
)

/**
 * Lightweight, dedicated DataStore for the three Challenge Mode variants
 * (Section 11). Kept separate from [SettingsDataStore] since these are
 * player records, not app preferences, even though both are small enough
 * not to need a full Room table.
 */
class ChallengeDataStore(private val context: Context) {

    private fun completedKey(type: ChallengeType) = booleanPreferencesKey("${type.name}_completed")
    private fun bestTimeKey(type: ChallengeType) = longPreferencesKey("${type.name}_best_time")

    fun recordFor(type: ChallengeType): Flow<ChallengeRecord> =
        context.challengeDataStore.data.map { prefs ->
            ChallengeRecord(
                everCompleted = prefs[completedKey(type)] ?: false,
                bestTimeMillis = prefs[bestTimeKey(type)]
            )
        }

    suspend fun recordSuccess(type: ChallengeType, timeMillis: Long) {
        context.challengeDataStore.edit { prefs ->
            prefs[completedKey(type)] = true
            val existingBest = prefs[bestTimeKey(type)]
            if (existingBest == null || timeMillis < existingBest) {
                prefs[bestTimeKey(type)] = timeMillis
            }
        }
    }
}
