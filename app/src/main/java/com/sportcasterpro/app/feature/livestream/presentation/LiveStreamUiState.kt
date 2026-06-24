package com.sportcasterpro.app.feature.livestream.presentation

import com.sportcasterpro.app.core.domain.model.Match
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreboardState
import com.sportcasterpro.app.feature.streaming.domain.StreamSettings
import com.sportcasterpro.app.feature.streaming.domain.StreamStats

data class LiveStreamUiState(
    val match: Match? = null,
    val scoreboard: ScoreboardState? = null,
    val streamSettings: StreamSettings = StreamSettings(),
    val streamStats: StreamStats = StreamStats(),
) {
    val isLoading: Boolean get() = match == null || scoreboard == null
}
