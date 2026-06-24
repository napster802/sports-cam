package com.sportcasterpro.app.feature.match.domain

import com.sportcasterpro.app.core.domain.model.Match
import com.sportcasterpro.app.core.domain.model.MatchStatus
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun observeMatches(): Flow<List<Match>>
    fun observeMatch(matchId: String): Flow<Match?>
    suspend fun getMatch(matchId: String): Match?
    suspend fun createMatch(match: Match)
    suspend fun updateStatus(matchId: String, status: MatchStatus)
    suspend fun deleteMatch(match: Match)
}
