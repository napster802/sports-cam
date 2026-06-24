package com.sportcasterpro.app.feature.scoreboard.domain.rules

import com.google.common.truth.Truth.assertThat
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import com.sportcasterpro.app.feature.scoreboard.domain.ScoringTeam
import org.junit.Test

class TennisRulesTest {

    private val rules = TennisRules()
    private val homeTeam = Team(id = "home", name = "Home")
    private val awayTeam = Team(id = "away", name = "Away")

    private fun initialState() = rules.initialState(Sport.TENNIS, homeTeam, awayTeam)

    @Test
    fun `winning a game at 4 points with a 2 point lead resets points and adds a game`() {
        var state = initialState().copy(homeScore = 3, awayScore = 1)

        state = rules.addPoint(state, ScoringTeam.HOME)

        assertThat(state.homeScore).isEqualTo(0)
        assertThat(state.awayScore).isEqualTo(0)
        assertThat(state.homeGamesInSet).isEqualTo(1)
    }

    @Test
    fun `deuce at 3-3 does not award the game until a 2 point lead is reached`() {
        var state = initialState().copy(homeScore = 3, awayScore = 3)

        state = rules.addPoint(state, ScoringTeam.HOME)

        assertThat(state.homeScore).isEqualTo(4)
        assertThat(state.homeGamesInSet).isEqualTo(0)
    }

    @Test
    fun `winning the sixth game with a 2 game lead wins the set`() {
        var state = initialState().copy(homeGamesInSet = 5, awayGamesInSet = 3, homeScore = 3, awayScore = 1)

        state = rules.addPoint(state, ScoringTeam.HOME)

        assertThat(state.homeGamesInSet).isEqualTo(0)
        assertThat(state.awayGamesInSet).isEqualTo(0)
        assertThat(state.homePeriodsWon).isEqualTo(1)
        assertThat(state.periodIndex).isEqualTo(2)
    }

    @Test
    fun `a set tied at 6-6 is decided by reaching 7 games`() {
        var state = initialState().copy(homeGamesInSet = 6, awayGamesInSet = 6, homeScore = 3, awayScore = 1)

        state = rules.addPoint(state, ScoringTeam.HOME)

        assertThat(state.homeGamesInSet).isEqualTo(0)
        assertThat(state.homePeriodsWon).isEqualTo(1)
    }

    @Test
    fun `winning the second set completes a best-of-three match`() {
        var state = initialState().copy(
            homePeriodsWon = 1,
            homeGamesInSet = 5,
            awayGamesInSet = 3,
            homeScore = 3,
            awayScore = 1,
        )

        state = rules.addPoint(state, ScoringTeam.HOME)

        assertThat(state.isMatchComplete).isTrue()
        assertThat(state.homePeriodsWon).isEqualTo(2)
    }

    @Test
    fun `addPoint is a no-op once the match is complete`() {
        val completed = initialState().copy(isMatchComplete = true, homeScore = 1)

        val state = rules.addPoint(completed, ScoringTeam.HOME)

        assertThat(state).isEqualTo(completed)
    }

    @Test
    fun `removePoint never drops the current game score below zero`() {
        val state = rules.removePoint(initialState(), ScoringTeam.AWAY)

        assertThat(state.awayScore).isEqualTo(0)
    }
}
