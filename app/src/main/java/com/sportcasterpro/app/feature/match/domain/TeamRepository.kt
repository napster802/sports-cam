package com.sportcasterpro.app.feature.match.domain

import com.sportcasterpro.app.core.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun observeTeams(): Flow<List<Team>>
    suspend fun getTeam(teamId: String): Team?
    suspend fun upsertTeam(team: Team)
    suspend fun deleteTeam(team: Team)
}
