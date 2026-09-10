package com.yugalify.arrowzen.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yugalify.arrowzen.core.constants.AppConstants
import com.yugalify.arrowzen.data.database.dao.AchievementDao
import com.yugalify.arrowzen.data.database.dao.DailyChallengeDao
import com.yugalify.arrowzen.data.database.dao.GameStatisticsDao
import com.yugalify.arrowzen.data.database.dao.LevelProgressDao
import com.yugalify.arrowzen.data.database.dao.PlayerProfileDao
import com.yugalify.arrowzen.data.database.entity.AchievementEntity
import com.yugalify.arrowzen.data.database.entity.DailyChallengeEntity
import com.yugalify.arrowzen.data.database.entity.GameStatisticsEntity
import com.yugalify.arrowzen.data.database.entity.LevelProgressEntity
import com.yugalify.arrowzen.data.database.entity.PlayerProfileEntity

/**
 * Migration strategy (Section 21): every schema change from version 2 onward
 * must add an explicit `Room.databaseBuilder(...).addMigrations(MIGRATION_1_2, ...)`
 * entry here rather than relying on destructive fallback. Destructive fallback
 * is intentionally NOT enabled, because Section 21 forbids erasing progress
 * due to a schema change — a missing migration should surface as a crash in
 * debug builds (caught immediately by a developer) rather than silently wipe
 * player data in production.
 */
@Database(
    entities = [
        LevelProgressEntity::class,
        PlayerProfileEntity::class,
        DailyChallengeEntity::class,
        AchievementEntity::class,
        GameStatisticsEntity::class
    ],
    version = AppConstants.DATABASE_VERSION,
    exportSchema = true
)
abstract class ArrowZenDatabase : RoomDatabase() {

    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun gameStatisticsDao(): GameStatisticsDao

    companion object {
        @Volatile
        private var instance: ArrowZenDatabase? = null

        fun getInstance(context: Context): ArrowZenDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): ArrowZenDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                ArrowZenDatabase::class.java,
                AppConstants.DATABASE_NAME
            )
                // Add real migrations here as the schema evolves — see the
                // class-level doc comment for why destructive fallback is
                // deliberately not used.
                .build()
    }
}
