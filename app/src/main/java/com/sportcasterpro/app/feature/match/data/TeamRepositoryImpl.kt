package com.sportcasterpro.app.feature.match.data

import com.sportcasterpro.app.core.data.local.dao.TeamDao
import com.sportcasterpro.app.core.data.local.entity.toDomain
import com.sportcasterpro.app.core.data.local.entity.toEntity
import com.sportcasterpro.app.core.domain.model.Team
import com.sportcasterpro.app.core.domain.util.DispatcherProvider
import com.sportcasterpro.app.feature.match.domain.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val teamDao: TeamDao,
    private val dispatchers: DispatcherProvider,
) : TeamRepository {

    override fun observeTeams(): Flow<List<Team>> =
        teamDao.observeTeams().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTeam(teamId: String): Team? =
        withContext(dispatchers.io) { teamDao.getTeam(teamId)?.toDomain() }

    override suspend fun upsertTeam(team: Team) =
        withContext(dispatchers.io) { teamDao.upsert(team.toEntity()) }

    override suspend fun deleteTeam(team: Team) =
        withContext(dispatchers.io) { teamDao.delete(team.toEntity()) }
}
