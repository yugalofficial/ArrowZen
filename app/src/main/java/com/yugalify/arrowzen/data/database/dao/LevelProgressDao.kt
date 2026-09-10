package com.yugalify.arrowzen.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yugalify.arrowzen.data.database.entity.LevelProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    suspend fun getProgress(levelId: String): LevelProgressEntity?

    @Query("SELECT * FROM level_progress")
    fun observeAllProgress(): Flow<List<LevelProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LevelProgressEntity)

    @Query("SELECT COUNT(*) FROM level_progress WHERE completionCount > 0")
    suspend fun completedLevelCount(): Int
}
