package com.yugalify.arrowzen.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yugalify.arrowzen.data.database.entity.GameStatisticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatisticsDao {

    @Query("SELECT * FROM game_statistics WHERE id = 0")
    fun observeStatistics(): Flow<GameStatisticsEntity?>

    @Query("SELECT * FROM game_statistics WHERE id = 0")
    suspend fun getStatistics(): GameStatisticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(statistics: GameStatisticsEntity)
}
