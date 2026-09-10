package com.yugalify.arrowzen.data.repository

import com.yugalify.arrowzen.data.datastore.ArrowZenSettings
import com.yugalify.arrowzen.data.datastore.SettingsDataStore
import com.yugalify.arrowzen.domain.repository.SettingsRepository
import com.yugalify.arrowzen.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(private val dataStore: SettingsDataStore) : SettingsRepository {
    override val settings: Flow<ArrowZenSettings> = dataStore.settings

    override suspend fun setTheme(theme: ThemePreference) = dataStore.setTheme(theme)
    override suspend fun setSoundEnabled(enabled: Boolean) = dataStore.setSoundEnabled(enabled)
    override suspend fun setMusicEnabled(enabled: Boolean) = dataStore.setMusicEnabled(enabled)
    override suspend fun setHapticsEnabled(enabled: Boolean) = dataStore.setHapticsEnabled(enabled)
    override suspend fun setReducedAnimations(enabled: Boolean) = dataStore.setReducedAnimations(enabled)
    override suspend fun setHighContrast(enabled: Boolean) = dataStore.setHighContrast(enabled)
    override suspend fun setOnboardingCompleted(completed: Boolean) = dataStore.setOnboardingCompleted(completed)
}
