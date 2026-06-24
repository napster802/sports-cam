package com.sportcasterpro.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sportcasterpro.app.core.data.local.entity.ScoreEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreEventDao {

    @Query("SELECT * FROM score_events WHERE matchId = :matchId ORDER BY timestampEpochMillis ASC")
    fun observeHistory(matchId: String): Flow<List<ScoreEventEntity>>

    @Insert
    suspend fun insert(event: ScoreEventEntity)

    @Query("DELETE FROM score_events WHERE matchId = :matchId")
    suspend fun clearHistory(matchId: String)
}
