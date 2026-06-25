package com.sportcasterpro.app.feature.match.presentation.creatematch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sportcasterpro.app.R
import com.sportcasterpro.app.core.ui.components.SportCasterTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMatchScreen(
    onMatchCreated: (matchId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateMatchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var sportMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.createdMatchId) {
        uiState.createdMatchId?.let(onMatchCreated)
    }

    Scaffold(topBar = { SportCasterTopBar(stringResource(R.string.create_match_title), onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            ExposedDropdownMenuBox(
                expanded = sportMenuExpanded,
                onExpandedChange = { sportMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = uiState.sport.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_sport)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = sportMenuExpanded,
                    onDismissRequest = { sportMenuExpanded = false },
                    modifier = Modifier.exposedDropdownSize(),
                ) {
                    viewModel.availableSports.forEach { sport ->
                        DropdownMenuItem(
                            text = { Text(sport.displayName) },
                            onClick = {
                                viewModel.onSportSelected(sport)
                                sportMenuExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.homeTeamName,
                onValueChange = viewModel::onHomeTeamNameChanged,
                label = { Text(stringResource(R.string.label_home_team)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
            OutlinedTextField(
                value = uiState.awayTeamName,
                onValueChange = viewModel::onAwayTeamNameChanged,
                label = { Text(stringResource(R.string.label_away_team)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            OutlinedTextField(
                value = uiState.location,
                onValueChange = viewModel::onLocationChanged,
                label = { Text(stringResource(R.string.label_location)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                Text(stringResource(R.string.action_save_match))
            }
        }
    }
}
