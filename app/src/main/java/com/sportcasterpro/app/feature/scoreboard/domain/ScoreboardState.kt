package com.sportcasterpro.app.feature.scoreboard.domain

import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team

enum class PossessionTeam { NONE, HOME, AWAY }

/**
 * Full visual + scoring state of a scoreboard overlay at a point in time.
 *
 * [homeScore]/[awayScore] mean different things per sport: for point-tally sports (basketball,
 * football, etc.) they are the running total. For game-based sports (tennis) they hold the raw
 * point count within the current game, and [homeGamesInSet]/[awayGamesInSet] hold games won in
 * the current set while [homePeriodsWon]/[awayPeriodsWon] hold sets won.
 */
data class ScoreboardState(
    val sport: Sport,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homeGamesInSet: Int = 0,
    val awayGamesInSet: Int = 0,
    val homePeriodsWon: Int = 0,
    val awayPeriodsWon: Int = 0,
    val periodIndex: Int = 1,
    val possession: PossessionTeam = PossessionTeam.NONE,
    val timerMillisRemaining: Long = 0L,
    val isTimerRunning: Boolean = false,
    val isMatchComplete: Boolean = false,
) {
    val periodLabel: String get() = "${sport.periodLabel} $periodIndex"

    fun displayScoreFor(isHome: Boolean): String = when (sport) {
        Sport.TENNIS -> tennisPointLabel(if (isHome) homeScore else awayScore, if (isHome) awayScore else homeScore)
        else -> (if (isHome) homeScore else awayScore).toString()
    }

    private fun tennisPointLabel(mine: Int, theirs: Int): String = when {
        mine >= 3 && theirs >= 3 && mine == theirs -> "Deuce"
        mine >= 4 && mine - theirs == 1 -> "AD"
        else -> TENNIS_POINT_LABELS.getOrElse(mine) { mine.toString() }
    }

    private companion object {
        val TENNIS_POINT_LABELS = listOf("0", "15", "30", "40")
    }
}
