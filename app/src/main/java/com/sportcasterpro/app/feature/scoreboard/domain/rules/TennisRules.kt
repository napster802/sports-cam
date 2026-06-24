package com.sportcasterpro.app.feature.scoreboard.domain.rules

import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreboardState
import com.sportcasterpro.app.feature.scoreboard.domain.ScoringTeam
import com.sportcasterpro.app.feature.scoreboard.domain.SportRules

/**
 * Best-of-3-sets tennis scoring. [ScoreboardState.homeScore]/[awayScore] hold raw points within
 * the current game, [homeGamesInSet]/[awayGamesInSet] hold games won in the current set, and
 * [homePeriodsWon]/[awayPeriodsWon] hold sets won.
 *
 * Simplification: a set at 6-6 is decided by whichever side reaches 7 games next, rather than a
 * full 7-point tiebreak game. [removePoint] only undoes the last point and cannot reverse a
 * completed game or set.
 */
class TennisRules : SportRules {

    override fun initialState(sport: Sport, homeTeam: Team, awayTeam: Team): ScoreboardState =
        ScoreboardState(sport = sport, homeTeam = homeTeam, awayTeam = awayTeam)

    override fun addPoint(state: ScoreboardState, team: ScoringTeam, value: Int): ScoreboardState {
        if (state.isMatchComplete) return state
        val scored = when (team) {
            ScoringTeam.HOME -> state.copy(homeScore = state.homeScore + 1)
            ScoringTeam.AWAY -> state.copy(awayScore = state.awayScore + 1)
        }
        return applyGameCompletion(scored)
    }

    override fun removePoint(state: ScoreboardState, team: ScoringTeam, value: Int): ScoreboardState = when (team) {
        ScoringTeam.HOME -> state.copy(homeScore = (state.homeScore - 1).coerceAtLeast(0))
        ScoringTeam.AWAY -> state.copy(awayScore = (state.awayScore - 1).coerceAtLeast(0))
    }

    override fun nextPeriod(state: ScoreboardState): ScoreboardState = advanceSet(state)

    private fun applyGameCompletion(state: ScoreboardState): ScoreboardState {
        val diff = state.homeScore - state.awayScore
        val gameWon = (state.homeScore >= 4 || state.awayScore >= 4) && kotlin.math.abs(diff) >= 2
        if (!gameWon) return state

        val homeWonGame = diff > 0
        val withGame = state.copy(
            homeScore = 0,
            awayScore = 0,
            homeGamesInSet = state.homeGamesInSet + if (homeWonGame) 1 else 0,
            awayGamesInSet = state.awayGamesInSet + if (homeWonGame) 0 else 1,
        )
        return applySetCompletion(withGame)
    }

    private fun applySetCompletion(state: ScoreboardState): ScoreboardState {
        val gameDiff = state.homeGamesInSet - state.awayGamesInSet
        val setWon = (state.homeGamesInSet >= 6 || state.awayGamesInSet >= 6) &&
            (kotlin.math.abs(gameDiff) >= 2 || state.homeGamesInSet == 7 || state.awayGamesInSet == 7)
        return if (setWon) advanceSet(state) else state
    }

    private fun advanceSet(state: ScoreboardState): ScoreboardState {
        val homeWonSet = state.homeGamesInSet > state.awayGamesInSet
        val homePeriodsWon = state.homePeriodsWon + if (homeWonSet) 1 else 0
        val awayPeriodsWon = state.awayPeriodsWon + if (homeWonSet) 0 else 1
        val matchComplete = homePeriodsWon >= 2 || awayPeriodsWon >= 2
        return state.copy(
            homeGamesInSet = 0,
            awayGamesInSet = 0,
            homePeriodsWon = homePeriodsWon,
            awayPeriodsWon = awayPeriodsWon,
            periodIndex = if (matchComplete) state.periodIndex else state.periodIndex + 1,
            isMatchComplete = matchComplete,
        )
    }
}
