package com.sportcasterpro.app.feature.scoreboard.data

import com.sportcasterpro.app.core.data.local.dao.ScoreEventDao
import com.sportcasterpro.app.core.data.local.entity.toDomain
import com.sportcasterpro.app.core.data.local.entity.toEntity
import com.sportcasterpro.app.core.domain.model.ScoreEvent
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScoreEventRepositoryImpl @Inject constructor(
    private val scoreEventDao: ScoreEventDao,
) : ScoreEventRepository {

    override fun observeHistory(matchId: String): Flow<List<ScoreEvent>> =
        scoreEventDao.observeHistory(matchId).map { events -> events.map { it.toDomain() } }

    override suspend fun recordEvent(event: ScoreEvent) = scoreEventDao.insert(event.toEntity())
}
