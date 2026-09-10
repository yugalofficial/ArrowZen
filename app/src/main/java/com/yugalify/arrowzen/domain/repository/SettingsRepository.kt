package com.yugalify.arrowzen.domain.repository

import com.yugalify.arrowzen.data.datastore.ArrowZenSettings
import com.yugalify.arrowzen.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<ArrowZenSettings>
    suspend fun setTheme(theme: ThemePreference)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setReducedAnimations(enabled: Boolean)
    suspend fun setHighContrast(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
