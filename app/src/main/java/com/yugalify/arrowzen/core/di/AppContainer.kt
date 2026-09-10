package com.yugalify.arrowzen.core.di

import android.content.Context
import com.yugalify.arrowzen.core.audio.HapticsManager
import com.yugalify.arrowzen.core.audio.SoundManager
import com.yugalify.arrowzen.data.database.ArrowZenDatabase
import com.yugalify.arrowzen.data.datastore.ChallengeDataStore
import com.yugalify.arrowzen.data.datastore.SettingsDataStore
import com.yugalify.arrowzen.data.repository.AchievementRepositoryImpl
import com.yugalify.arrowzen.data.repository.DailyChallengeRepositoryImpl
import com.yugalify.arrowzen.data.repository.LevelRepositoryImpl
import com.yugalify.arrowzen.data.repository.SettingsRepositoryImpl
import com.yugalify.arrowzen.data.repository.StatisticsRepositoryImpl
import com.yugalify.arrowzen.domain.repository.AchievementRepository
import com.yugalify.arrowzen.domain.repository.DailyChallengeRepository
import com.yugalify.arrowzen.domain.repository.LevelRepository
import com.yugalify.arrowzen.domain.repository.SettingsRepository
import com.yugalify.arrowzen.domain.repository.StatisticsRepository
import com.yugalify.arrowzen.domain.usecase.CompleteLevelUseCase

/**
 * Manual dependency container, chosen over Hilt for now per the reasoning
 * in docs/ARCHITECTURE.md: the dependency graph is still small enough that
 * a hand-written container is more transparent than DI codegen. Revisit if
 * the graph grows significantly (e.g. once per-feature ViewModels multiply
 * in Phase 4/5).
 *
 * Everything here is a `by lazy` singleton scoped to the process, built
 * once from [ArrowZenApplication].
 */
class AppContainer(context: Context) {

    private val database: ArrowZenDatabase by lazy { ArrowZenDatabase.getInstance(context) }
    private val settingsDataStore: SettingsDataStore by lazy { SettingsDataStore(context) }
    val challengeDataStore: ChallengeDataStore by lazy { ChallengeDataStore(context) }
    val soundManager: SoundManager by lazy { SoundManager(context) }
    val hapticsManager: HapticsManager by lazy { HapticsManager(context) }

    val levelRepository: LevelRepository by lazy { LevelRepositoryImpl(database.levelProgressDao()) }
    val statisticsRepository: StatisticsRepository by lazy { StatisticsRepositoryImpl(database.gameStatisticsDao()) }
    val achievementRepository: AchievementRepository by lazy { AchievementRepositoryImpl(database.achievementDao()) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(settingsDataStore) }
    val dailyChallengeRepository: DailyChallengeRepository by lazy { DailyChallengeRepositoryImpl(database.dailyChallengeDao()) }

    val completeLevelUseCase: CompleteLevelUseCase by lazy {
        CompleteLevelUseCase(levelRepository, statisticsRepository, achievementRepository)
    }
}
