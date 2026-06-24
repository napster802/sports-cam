package com.sportcasterpro.app.feature.scoreboard.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportcasterpro.app.core.domain.model.MatchStatus
import com.sportcasterpro.app.feature.match.domain.TeamRepository
import com.sportcasterpro.app.feature.match.domain.usecase.ObserveMatchUseCase
import com.sportcasterpro.app.feature.match.domain.usecase.UpdateMatchStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoreboardEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeMatch: ObserveMatchUseCase,
    private val teamRepository: TeamRepository,
    private val updateMatchStatus: UpdateMatchStatusUseCase,
) : ViewModel() {

    private val matchId: String = checkNotNull(savedStateHandle["matchId"])

    private val _uiState = MutableStateFlow(ScoreboardEditorUiState())
    val uiState: StateFlow<ScoreboardEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeMatch(matchId).collect { match ->
                if (match != null) {
                    _uiState.update {
                        it.copy(
                            match = match,
                            homeTeamName = match.homeTeam.name,
                            awayTeamName = match.awayTeam.name,
                            homeColorHex = match.homeTeam.colorHex,
                            awayColorHex = match.awayTeam.colorHex,
                        )
                    }
                }
            }
        }
    }

    fun onHomeTeamNameChanged(value: String) = _uiState.update { it.copy(homeTeamName = value) }
    fun onAwayTeamNameChanged(value: String) = _uiState.update { it.copy(awayTeamName = value) }
    fun onHomeColorSelected(colorHex: String) = _uiState.update { it.copy(homeColorHex = colorHex) }
    fun onAwayColorSelected(colorHex: String) = _uiState.update { it.copy(awayColorHex = colorHex) }

    fun startBroadcast() {
        val state = _uiState.value
        val match = state.match ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            teamRepository.upsertTeam(match.homeTeam.copy(name = state.homeTeamName.trim(), colorHex = state.homeColorHex))
            teamRepository.upsertTeam(match.awayTeam.copy(name = state.awayTeamName.trim(), colorHex = state.awayColorHex))
            updateMatchStatus(matchId, MatchStatus.LIVE)
            _uiState.update { it.copy(isSaving = false, readyToGoLive = true) }
        }
    }
}
