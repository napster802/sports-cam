package com.sportcasterpro.app.feature.scoreboard.domain.rules

import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreboardState
import com.sportcasterpro.app.feature.scoreboard.domain.ScoringTeam
import com.sportcasterpro.app.feature.scoreboard.domain.SportRules

/**
 * Default rules for sports with a simple running point/run total and a manually-advanced period
 * (quarters, halves, innings, etc). Used by every sport that doesn't need bespoke set/game logic.
 */
class GenericPointRules : SportRules {

    override fun initialState(sport: Sport, homeTeam: Team, awayTeam: Team): ScoreboardState =
        ScoreboardState(sport = sport, homeTeam = homeTeam, awayTeam = awayTeam)

    override fun addPoint(state: ScoreboardState, team: ScoringTeam, value: Int): ScoreboardState = when (team) {
        ScoringTeam.HOME -> state.copy(homeScore = state.homeScore + value)
        ScoringTeam.AWAY -> state.copy(awayScore = state.awayScore + value)
    }

    override fun removePoint(state: ScoreboardState, team: ScoringTeam, value: Int): ScoreboardState = when (team) {
        ScoringTeam.HOME -> state.copy(homeScore = (state.homeScore - value).coerceAtLeast(0))
        ScoringTeam.AWAY -> state.copy(awayScore = (state.awayScore - value).coerceAtLeast(0))
    }

    override fun nextPeriod(state: ScoreboardState): ScoreboardState =
        if (state.periodIndex >= state.sport.defaultPeriodCount) {
            state.copy(isMatchComplete = true)
        } else {
            state.copy(periodIndex = state.periodIndex + 1)
        }
}
