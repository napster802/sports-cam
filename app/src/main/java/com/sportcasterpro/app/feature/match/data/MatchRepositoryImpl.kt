package com.sportcasterpro.app.feature.match.data

import com.sportcasterpro.app.core.data.local.dao.MatchDao
import com.sportcasterpro.app.core.data.local.entity.toDomain
import com.sportcasterpro.app.core.data.local.entity.toEntity
import com.sportcasterpro.app.core.domain.model.Match
import com.sportcasterpro.app.core.domain.model.MatchStatus
import com.sportcasterpro.app.core.domain.util.DispatcherProvider
import com.sportcasterpro.app.feature.match.domain.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val dispatchers: DispatcherProvider,
) : MatchRepository {

    override fun observeMatches(): Flow<List<Match>> =
        matchDao.observeMatches().map { rows -> rows.map { it.toDomain() } }

    override fun observeMatch(matchId: String): Flow<Match?> =
        matchDao.observeMatch(matchId).map { it?.toDomain() }

    override suspend fun getMatch(matchId: String): Match? =
        withContext(dispatchers.io) { matchDao.getMatch(matchId)?.toDomain() }

    override suspend fun createMatch(match: Match) =
        withContext(dispatchers.io) { matchDao.upsert(match.toEntity()) }

    override suspend fun updateStatus(matchId: String, status: MatchStatus) =
        withContext(dispatchers.io) { matchDao.updateStatus(matchId, status.name) }

    override suspend fun deleteMatch(match: Match) =
        withContext(dispatchers.io) { matchDao.delete(match.toEntity()) }
}
