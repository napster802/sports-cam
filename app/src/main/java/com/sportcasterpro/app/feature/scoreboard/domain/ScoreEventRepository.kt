package com.sportcasterpro.app.feature.scoreboard.domain

import com.sportcasterpro.app.core.domain.model.ScoreEvent
import kotlinx.coroutines.flow.Flow

interface ScoreEventRepository {
    fun observeHistory(matchId: String): Flow<List<ScoreEvent>>
    suspend fun recordEvent(event: ScoreEvent)
}
