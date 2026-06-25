package com.sportcasterpro.app.feature.scoreboard.domain

import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import com.sportcasterpro.app.feature.scoreboard.domain.rules.GenericPointRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TIMER_TICK_MILLIS = 1_000L

/**
 * Holds the live, in-memory scoring state for a single broadcast and delegates every transition
 * to the [SportRules] resolved for the match's sport. Callers should create one controller per
 * live match (it is not a singleton) and call [start] before issuing any other commands.
 */
class ScoreboardController @Inject constructor(
    private val sportRulesFactory: SportRulesFactory,
) {
    private var rules: SportRules = GenericPointRules()
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(
        ScoreboardState(
            sport = Sport.BASKETBALL,
            homeTeam = Team(id = "", name = ""),
            awayTeam = Team(id = "", name = ""),
        ),
    )
    val state: StateFlow<ScoreboardState> = _state.asStateFlow()

    fun start(sport: Sport, homeTeam: Team, awayTeam: Team) {
        timerJob?.cancel()
        rules = sportRulesFactory.rulesFor(sport)
        _state.value = rules.initialState(sport, homeTeam, awayTeam)
    }

    fun addPoint(team: ScoringTeam, value: Int = 1) {
        _state.update { rules.addPoint(it, team, value) }
    }

    fun removePoint(team: ScoringTeam, value: Int = 1) {
        _state.update { rules.removePoint(it, team, value) }
    }

    fun nextPeriod() {
        _state.update { rules.nextPeriod(it) }
    }

    fun setPossession(team: PossessionTeam) {
        _state.update { it.copy(possession = team) }
    }

    fun editTeamName(isHome: Boolean, name: String) {
        _state.update {
            if (isHome) it.copy(homeTeam = it.homeTeam.copy(name = name)) else it.copy(awayTeam = it.awayTeam.copy(name = name))
        }
    }

    fun swapTeams() {
        _state.update {
            it.copy(
                homeTeam = it.awayTeam,
                awayTeam = it.homeTeam,
                homeScore = it.awayScore,
                awayScore = it.homeScore,
                homeGamesInSet = it.awayGamesInSet,
                awayGamesInSet = it.homeGamesInSet,
                homePeriodsWon = it.awayPeriodsWon,
                awayPeriodsWon = it.homePeriodsWon,
            )
        }
    }

    fun resetScore() {
        val current = _state.value
        start(current.sport, current.homeTeam, current.awayTeam)
    }

    fun setTimer(millisRemaining: Long) {
        timerJob?.cancel()
        _state.update { it.copy(timerMillisRemaining = millisRemaining, isTimerRunning = false) }
    }

    fun startTimer(scope: CoroutineScope) {
        if (timerJob?.isActive == true) return
        _state.update { it.copy(isTimerRunning = true) }
        timerJob = scope.launch {
            while (_state.value.isTimerRunning && _state.value.timerMillisRemaining > 0) {
                delay(TIMER_TICK_MILLIS)
                _state.update { it.copy(timerMillisRemaining = (it.timerMillisRemaining - TIMER_TICK_MILLIS).coerceAtLeast(0)) }
            }
            _state.update { it.copy(isTimerRunning = false) }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _state.update { it.copy(isTimerRunning = false) }
    }
}
