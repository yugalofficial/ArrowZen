package com.yugalify.arrowzen.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yugalify.arrowzen.data.database.entity.DailyChallengeEntity

@Dao
interface DailyChallengeDao {

    @Query("SELECT * FROM daily_challenge WHERE dateKey = :dateKey")
    suspend fun getByDate(dateKey: String): DailyChallengeEntity?

    @Query("SELECT * FROM daily_challenge WHERE isCompleted = 1 ORDER BY dateKey DESC")
    suspend fun getCompletedDatesDescending(): List<DailyChallengeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyChallengeEntity)
}
