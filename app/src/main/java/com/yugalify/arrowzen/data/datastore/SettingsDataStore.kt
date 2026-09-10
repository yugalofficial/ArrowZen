package com.yugalify.arrowzen.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yugalify.arrowzen.core.constants.AppConstants
import com.yugalify.arrowzen.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = AppConstants.DATASTORE_SETTINGS_NAME)

/** Immutable snapshot of every user-facing toggle in the Settings screen (Section 38). */
data class ArrowZenSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedAnimations: Boolean = false,
    val highContrast: Boolean = false,
    val onboardingCompleted: Boolean = false
)

/**
 * Thin wrapper around Jetpack DataStore Preferences. Kept separate from
 * Room (which owns structured progress data) per the Section 20 split:
 * DataStore for toggles/preferences, Room for gameplay records.
 */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCED_ANIMATIONS = booleanPreferencesKey("reduced_animations")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val settings: Flow<ArrowZenSettings> = context.dataStore.data.map { prefs ->
        ArrowZenSettings(
            theme = prefs[Keys.THEME]?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
                ?: ThemePreference.SYSTEM,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            reducedAnimations = prefs[Keys.REDUCED_ANIMATIONS] ?: false,
            highContrast = prefs[Keys.HIGH_CONTRAST] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun setTheme(theme: ThemePreference) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setReducedAnimations(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REDUCED_ANIMATIONS] = enabled }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }
}
