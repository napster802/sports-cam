package com.sportcasterpro.app.feature.scoreboard.domain

import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team

enum class ScoringTeam { HOME, AWAY }

/**
 * Encapsulates the scoring behavior that differs between sports. A [ScoreboardController]
 * delegates all state transitions to the [SportRules] resolved for the match's [Sport], so the
 * controller itself stays sport-agnostic.
 */
interface SportRules {
    fun initialState(sport: Sport, homeTeam: Team, awayTeam: Team): ScoreboardState
    fun addPoint(state: ScoreboardState, team: ScoringTeam, value: Int = 1): ScoreboardState
    fun removePoint(state: ScoreboardState, team: ScoringTeam, value: Int = 1): ScoreboardState
    fun nextPeriod(state: ScoreboardState): ScoreboardState
}
