package com.sportcasterpro.app.feature.scoreboard.domain.rules

import com.google.common.truth.Truth.assertThat
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import com.sportcasterpro.app.feature.scoreboard.domain.PossessionTeam
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreboardState
import com.sportcasterpro.app.feature.scoreboard.domain.ScoringTeam
import org.junit.Test

class VolleyballRulesTest {

    private val rules = VolleyballRules()
    private val homeTeam = Team(id = "home", name = "Home")
    private val awayTeam = Team(id = "away", name = "Away")

    private fun initialState() = rules.initialState(Sport.VOLLEYBALL, homeTeam, awayTeam)

    @Test
    fun `addPoint sets possession to the scoring team`() {
        val state = rules.addPoint(initialState(), ScoringTeam.AWAY)

        assertThat(state.possession).isEqualTo(PossessionTeam.AWAY)
        assertThat(state.awayScore).isEqualTo(1)
    }

    @Test
    fun `reaching 25 points with a 2 point lead wins the set and resets the score`() {
        var state = initialState().copy(homeScore = 24, awayScore = 20)

        state = rules.addPoint(state, ScoringTeam.HOME)

        assertThat(state.homeScore).isEqualTo(0)
        assertThat(state.awayScore).isEqualTo(0)
        assertThat(state.homePeriodsWon).isEqualTo(1)
        assertThat(state.periodIndex).isEqualTo(2)
    }

    @Test
    fun `set is not won at 25 points without a 2 point lead`() {
        var state = initialState().copy(homeScore = 24, awayScore = 24)

        state = rules.addPoint(state, ScoringTeam.HOME)

        assertThat(state.homeScore).isEqualTo(25)
        assertThat(state.homePeriodsWon).isEqualTo(0)
    }

    @Test
    fun `fifth set only requires 15 points to win`() {
        var state = initialState().copy(periodIndex = 5, homeScore = 14, awayScore = 10)

        state = rules.addPoint(state, ScoringTeam.HOME)

        assertThat(state.homePeriodsWon).isEqualTo(1)
    }

    @Test
    fun `winning the third set completes the match`() {
        var state = initialState().copy(homePeriodsWon = 2, awayPeriodsWon = 1, homeScore = 24, awayScore = 10)

        state = rules.addPoint(state, ScoringTeam.HOME)

        assertThat(state.isMatchComplete).isTrue()
        assertThat(state.homePeriodsWon).isEqualTo(3)
    }

    @Test
    fun `addPoint is a no-op once the match is complete`() {
        val completed = initialState().copy(isMatchComplete = true, homeScore = 5)

        val state = rules.addPoint(completed, ScoringTeam.HOME)

        assertThat(state).isEqualTo(completed)
    }

    @Test
    fun `removePoint never drops the current set score below zero`() {
        val state: ScoreboardState = rules.removePoint(initialState(), ScoringTeam.HOME)

        assertThat(state.homeScore).isEqualTo(0)
    }
}
