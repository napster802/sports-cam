package com.sportcasterpro.app.feature.scoreboard.domain.rules

import com.google.common.truth.Truth.assertThat
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import com.sportcasterpro.app.feature.scoreboard.domain.ScoringTeam
import org.junit.Test

class GenericPointRulesTest {

    private val rules = GenericPointRules()
    private val homeTeam = Team(id = "home", name = "Home")
    private val awayTeam = Team(id = "away", name = "Away")

    @Test
    fun `initialState seeds zero score and first period`() {
        val state = rules.initialState(Sport.BASKETBALL, homeTeam, awayTeam)

        assertThat(state.homeScore).isEqualTo(0)
        assertThat(state.awayScore).isEqualTo(0)
        assertThat(state.periodIndex).isEqualTo(1)
        assertThat(state.isMatchComplete).isFalse()
    }

    @Test
    fun `addPoint increments the scoring team only`() {
        val state = rules.initialState(Sport.BASKETBALL, homeTeam, awayTeam)

        val afterHome = rules.addPoint(state, ScoringTeam.HOME, value = 2)

        assertThat(afterHome.homeScore).isEqualTo(2)
        assertThat(afterHome.awayScore).isEqualTo(0)
    }

    @Test
    fun `removePoint never drops score below zero`() {
        val state = rules.initialState(Sport.BASKETBALL, homeTeam, awayTeam)

        val result = rules.removePoint(state, ScoringTeam.AWAY, value = 3)

        assertThat(result.awayScore).isEqualTo(0)
    }

    @Test
    fun `nextPeriod advances period index until default count then completes match`() {
        var state = rules.initialState(Sport.BASKETBALL, homeTeam, awayTeam)

        repeat(Sport.BASKETBALL.defaultPeriodCount - 1) {
            state = rules.nextPeriod(state)
            assertThat(state.isMatchComplete).isFalse()
        }
        assertThat(state.periodIndex).isEqualTo(Sport.BASKETBALL.defaultPeriodCount)

        state = rules.nextPeriod(state)

        assertThat(state.isMatchComplete).isTrue()
    }
}
