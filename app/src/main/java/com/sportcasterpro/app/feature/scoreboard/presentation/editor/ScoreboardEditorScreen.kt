package com.sportcasterpro.app.feature.scoreboard.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sportcasterpro.app.R
import com.sportcasterpro.app.core.ui.components.FullScreenLoading
import com.sportcasterpro.app.core.ui.components.SportCasterTopBar

private val TEAM_COLOR_PRESETS = listOf("#FF6A1A", "#1A6AFF", "#E53935", "#2E7D32", "#6A1AFF", "#FFC107")

@Composable
fun ScoreboardEditorScreen(
    matchId: String,
    onGoLive: (matchId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ScoreboardEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.readyToGoLive) {
        if (uiState.readyToGoLive) {
            onGoLive(matchId)
            viewModel.onGoLiveHandled()
        }
    }

    Scaffold(topBar = { SportCasterTopBar(stringResource(R.string.scoreboard_editor_title), onBack) }) { padding ->
        if (uiState.isLoading) {
            FullScreenLoading(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(text = uiState.match?.sport?.displayName.orEmpty(), style = MaterialTheme.typography.labelLarge)

            TeamEditor(
                titleRes = R.string.label_home_team,
                name = uiState.homeTeamName,
                onNameChanged = viewModel::onHomeTeamNameChanged,
                selectedColorHex = uiState.homeColorHex,
                onColorSelected = viewModel::onHomeColorSelected,
            )

            TeamEditor(
                titleRes = R.string.label_away_team,
                name = uiState.awayTeamName,
                onNameChanged = viewModel::onAwayTeamNameChanged,
                selectedColorHex = uiState.awayColorHex,
                onColorSelected = viewModel::onAwayColorSelected,
                topPadding = 24.dp,
            )

            Button(
                onClick = viewModel::startBroadcast,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            ) {
                Text(stringResource(R.string.action_start_broadcast))
            }
        }
    }
}

@Composable
private fun TeamEditor(
    titleRes: Int,
    name: String,
    onNameChanged: (String) -> Unit,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    topPadding: Dp = 16.dp,
) {
    Column(modifier = Modifier.padding(top = topPadding)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(titleRes)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.label_team_color),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TEAM_COLOR_PRESETS.forEach { colorHex ->
                ColorSwatch(
                    colorHex = colorHex,
                    isSelected = colorHex.equals(selectedColorHex, ignoreCase = true),
                    onClick = { onColorSelected(colorHex) },
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(colorHex: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = Color(android.graphics.Color.parseColor(colorHex))
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}
