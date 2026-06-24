package com.sportcasterpro.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sportcasterpro.app.core.data.local.entity.MatchEntity
import com.sportcasterpro.app.core.data.local.entity.MatchWithTeams
import kotlinx.coroutines.flow.Flow

private const val MATCH_WITH_TEAMS_QUERY = """
    SELECT
        m.*,
        ht.id AS home_id, ht.name AS home_name, ht.logoUri AS home_logoUri, ht.colorHex AS home_colorHex,
        at.id AS away_id, at.name AS away_name, at.logoUri AS away_logoUri, at.colorHex AS away_colorHex
    FROM matches m
    INNER JOIN teams ht ON ht.id = m.homeTeamId
    INNER JOIN teams at ON at.id = m.awayTeamId
"""

@Dao
interface MatchDao {

    @Transaction
    @Query("$MATCH_WITH_TEAMS_QUERY ORDER BY m.dateTimeEpochMillis ASC")
    fun observeMatches(): Flow<List<MatchWithTeams>>

    @Transaction
    @Query("$MATCH_WITH_TEAMS_QUERY WHERE m.id = :matchId")
    fun observeMatch(matchId: String): Flow<MatchWithTeams?>

    @Transaction
    @Query("$MATCH_WITH_TEAMS_QUERY WHERE m.id = :matchId")
    suspend fun getMatch(matchId: String): MatchWithTeams?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(match: MatchEntity)

    @Query("UPDATE matches SET status = :status WHERE id = :matchId")
    suspend fun updateStatus(matchId: String, status: String)

    @Delete
    suspend fun delete(match: MatchEntity)
}
