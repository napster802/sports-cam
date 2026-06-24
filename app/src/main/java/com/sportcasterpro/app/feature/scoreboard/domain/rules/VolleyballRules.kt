package com.sportcasterpro.app.feature.scoreboard.domain.rules

import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import com.sportcasterpro.app.feature.scoreboard.domain.PossessionTeam
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreboardState
import com.sportcasterpro.app.feature.scoreboard.domain.ScoringTeam
import com.sportcasterpro.app.feature.scoreboard.domain.SportRules

/**
 * Best-of-5 set scoring: 25 points to win a set (15 in the deciding 5th set), win by 2. First
 * team to 3 sets wins the match. [ScoreboardState.homeScore]/[awayScore] hold the current set's
 * point tally; [ScoreboardState.homePeriodsWon]/[awayPeriodsWon] hold sets won.
 */
class VolleyballRules : SportRules {

    override fun initialState(sport: Sport, homeTeam: Team, awayTeam: Team): ScoreboardState =
        ScoreboardState(sport = sport, homeTeam = homeTeam, awayTeam = awayTeam)

    override fun addPoint(state: ScoreboardState, team: ScoringTeam, value: Int): ScoreboardState {
        if (state.isMatchComplete) return state
        val scored = when (team) {
            ScoringTeam.HOME -> state.copy(homeScore = state.homeScore + 1, possession = PossessionTeam.HOME)
            ScoringTeam.AWAY -> state.copy(awayScore = state.awayScore + 1, possession = PossessionTeam.AWAY)
        }
        return applySetCompletion(scored)
    }

    override fun removePoint(state: ScoreboardState, team: ScoringTeam, value: Int): ScoreboardState = when (team) {
        ScoringTeam.HOME -> state.copy(homeScore = (state.homeScore - 1).coerceAtLeast(0))
        ScoringTeam.AWAY -> state.copy(awayScore = (state.awayScore - 1).coerceAtLeast(0))
    }

    override fun nextPeriod(state: ScoreboardState): ScoreboardState = advanceSet(state)

    private fun applySetCompletion(state: ScoreboardState): ScoreboardState {
        val pointsToWin = if (state.periodIndex >= 5) 15 else 25
        val leader = state.homeScore - state.awayScore
        val setWon = (state.homeScore >= pointsToWin || state.awayScore >= pointsToWin) && kotlin.math.abs(leader) >= 2
        return if (setWon) advanceSet(state) else state
    }

    private fun advanceSet(state: ScoreboardState): ScoreboardState {
        val homeWonSet = state.homeScore > state.awayScore
        val homePeriodsWon = state.homePeriodsWon + if (homeWonSet) 1 else 0
        val awayPeriodsWon = state.awayPeriodsWon + if (homeWonSet) 0 else 1
        val matchComplete = homePeriodsWon >= 3 || awayPeriodsWon >= 3
        return state.copy(
            homeScore = 0,
            awayScore = 0,
            homePeriodsWon = homePeriodsWon,
            awayPeriodsWon = awayPeriodsWon,
            periodIndex = if (matchComplete) state.periodIndex else state.periodIndex + 1,
            isMatchComplete = matchComplete,
        )
    }
}
