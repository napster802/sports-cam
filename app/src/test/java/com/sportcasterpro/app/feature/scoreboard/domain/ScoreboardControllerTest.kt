package com.sportcasterpro.app.feature.scoreboard.domain

import com.google.common.truth.Truth.assertThat
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ScoreboardControllerTest {

    private val controller = ScoreboardController(SportRulesFactory())
    private val homeTeam = Team(id = "home", name = "Home")
    private val awayTeam = Team(id = "away", name = "Away")

    @Test
    fun `start seeds the state for the requested sport and teams`() {
        controller.start(Sport.VOLLEYBALL, homeTeam, awayTeam)

        val state = controller.state.value
        assertThat(state.sport).isEqualTo(Sport.VOLLEYBALL)
        assertThat(state.homeTeam).isEqualTo(homeTeam)
        assertThat(state.homeScore).isEqualTo(0)
    }

    @Test
    fun `addPoint and removePoint delegate to the sport's rules`() {
        controller.start(Sport.BASKETBALL, homeTeam, awayTeam)

        controller.addPoint(ScoringTeam.HOME, value = 3)
        assertThat(controller.state.value.homeScore).isEqualTo(3)

        controller.removePoint(ScoringTeam.HOME, value = 1)
        assertThat(controller.state.value.homeScore).isEqualTo(2)
    }

    @Test
    fun `swapTeams exchanges teams and their scores`() {
        controller.start(Sport.BASKETBALL, homeTeam, awayTeam)
        controller.addPoint(ScoringTeam.HOME, value = 5)

        controller.swapTeams()

        val state = controller.state.value
        assertThat(state.homeTeam).isEqualTo(awayTeam)
        assertThat(state.awayTeam).isEqualTo(homeTeam)
        assertThat(state.awayScore).isEqualTo(5)
        assertThat(state.homeScore).isEqualTo(0)
    }

    @Test
    fun `resetScore reinitializes the match while keeping the current teams`() {
        controller.start(Sport.BASKETBALL, homeTeam, awayTeam)
        controller.addPoint(ScoringTeam.AWAY, value = 7)
        controller.nextPeriod()

        controller.resetScore()

        val state = controller.state.value
        assertThat(state.awayScore).isEqualTo(0)
        assertThat(state.periodIndex).isEqualTo(1)
        assertThat(state.homeTeam).isEqualTo(homeTeam)
    }

    @Test
    fun `setPossession updates possession directly`() {
        controller.start(Sport.HOCKEY, homeTeam, awayTeam)

        controller.setPossession(PossessionTeam.AWAY)

        assertThat(controller.state.value.possession).isEqualTo(PossessionTeam.AWAY)
    }

    @Test
    fun `startTimer counts down to zero and stops itself`() = runTest {
        controller.start(Sport.BASKETBALL, homeTeam, awayTeam)
        controller.setTimer(3_000L)

        controller.startTimer(this)
        advanceUntilIdle()

        val state = controller.state.value
        assertThat(state.timerMillisRemaining).isEqualTo(0L)
        assertThat(state.isTimerRunning).isFalse()
    }

    @Test
    fun `pauseTimer stops the countdown without resetting remaining time`() = runTest {
        controller.start(Sport.BASKETBALL, homeTeam, awayTeam)
        controller.setTimer(10_000L)

        controller.startTimer(this)
        advanceTimeBy(2_500L)
        controller.pauseTimer()
        val remainingAfterPause = controller.state.value.timerMillisRemaining
        advanceUntilIdle()

        assertThat(controller.state.value.isTimerRunning).isFalse()
        assertThat(controller.state.value.timerMillisRemaining).isEqualTo(remainingAfterPause)
    }
}
