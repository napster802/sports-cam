package com.sportcasterpro.app.feature.scoreboard.domain

import com.google.common.truth.Truth.assertThat
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import org.junit.Test

class ScoreboardStateTest {

    private val homeTeam = Team(id = "home", name = "Home")
    private val awayTeam = Team(id = "away", name = "Away")

    private fun tennisState(homeScore: Int, awayScore: Int) = ScoreboardState(
        sport = Sport.TENNIS,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        homeScore = homeScore,
        awayScore = awayScore,
    )

    @Test
    fun `non-tennis sports display raw score`() {
        val state = ScoreboardState(sport = Sport.BASKETBALL, homeTeam = homeTeam, awayTeam = awayTeam, homeScore = 12)

        assertThat(state.displayScoreFor(isHome = true)).isEqualTo("12")
    }

    @Test
    fun `tennis points below three map to 0-15-30-40`() {
        val state = tennisState(homeScore = 2, awayScore = 1)

        assertThat(state.displayScoreFor(isHome = true)).isEqualTo("30")
        assertThat(state.displayScoreFor(isHome = false)).isEqualTo("15")
    }

    @Test
    fun `tennis points tied at 3-3 or higher shows Deuce`() {
        val state = tennisState(homeScore = 3, awayScore = 3)

        assertThat(state.displayScoreFor(isHome = true)).isEqualTo("Deuce")
        assertThat(state.displayScoreFor(isHome = false)).isEqualTo("Deuce")
    }

    @Test
    fun `tennis points ahead by one past 40 shows AD`() {
        val state = tennisState(homeScore = 4, awayScore = 3)

        assertThat(state.displayScoreFor(isHome = true)).isEqualTo("AD")
        assertThat(state.displayScoreFor(isHome = false)).isEqualTo("40")
    }

    @Test
    fun `periodLabel combines the sport period name with the period index`() {
        val state = ScoreboardState(sport = Sport.HOCKEY, homeTeam = homeTeam, awayTeam = awayTeam, periodIndex = 3)

        assertThat(state.periodLabel).isEqualTo("Period 3")
    }
}
