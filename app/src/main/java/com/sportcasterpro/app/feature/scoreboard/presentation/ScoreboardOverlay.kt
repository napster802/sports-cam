package com.sportcasterpro.app.feature.scoreboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.feature.scoreboard.domain.PossessionTeam
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreboardState

/**
 * Broadcast-style lower-third scoreboard, meant to be layered on top of the camera preview /
 * stream output. Pure rendering of [state]; all interaction lives in the caller's controls.
 */
@Composable
fun ScoreboardOverlay(state: ScoreboardState, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.72f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                TeamScore(name = state.homeTeam.name, score = state.displayScoreFor(isHome = true), showPossession = state.sport.hasPossessionIndicator && state.possession == PossessionTeam.HOME)
                PeriodAndTimer(state)
                TeamScore(name = state.awayTeam.name, score = state.displayScoreFor(isHome = false), showPossession = state.sport.hasPossessionIndicator && state.possession == PossessionTeam.AWAY, alignEnd = true)
            }
            if (state.sport == Sport.TENNIS || state.sport == Sport.VOLLEYBALL) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Sets ${state.homePeriodsWon} - ${state.awayPeriodsWon}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamScore(name: String, score: String, showPossession: Boolean, alignEnd: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showPossession && !alignEnd) PossessionDot()
        Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
            Text(text = name.ifBlank { "Team" }, color = Color.White, style = MaterialTheme.typography.labelMedium)
            Text(text = score, color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }
        if (showPossession && alignEnd) PossessionDot()
    }
}

@Composable
private fun PossessionDot() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .size(8.dp)
            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
    )
}

@Composable
private fun PeriodAndTimer(state: ScoreboardState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = state.periodLabel, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
        Text(text = formatTimer(state.timerMillisRemaining), color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

private fun formatTimer(millisRemaining: Long): String {
    val totalSeconds = millisRemaining / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
