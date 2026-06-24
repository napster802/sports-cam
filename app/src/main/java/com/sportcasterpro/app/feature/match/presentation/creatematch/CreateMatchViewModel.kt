package com.sportcasterpro.app.feature.match.presentation.creatematch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.util.Resource
import com.sportcasterpro.app.feature.match.domain.usecase.CreateMatchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMatchViewModel @Inject constructor(
    private val createMatch: CreateMatchUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMatchUiState())
    val uiState: StateFlow<CreateMatchUiState> = _uiState.asStateFlow()

    val availableSports: List<Sport> = Sport.entries

    fun onSportSelected(sport: Sport) = _uiState.update { it.copy(sport = sport) }
    fun onHomeTeamNameChanged(value: String) = _uiState.update { it.copy(homeTeamName = value, errorMessage = null) }
    fun onAwayTeamNameChanged(value: String) = _uiState.update { it.copy(awayTeamName = value, errorMessage = null) }
    fun onLocationChanged(value: String) = _uiState.update { it.copy(location = value) }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = createMatch(
                sport = state.sport,
                homeTeamName = state.homeTeamName,
                awayTeamName = state.awayTeamName,
                location = state.location,
                dateTimeEpochMillis = System.currentTimeMillis(),
            )
            when (result) {
                is Resource.Success -> _uiState.update { it.copy(isSaving = false, createdMatchId = result.data.id) }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                Resource.Loading -> Unit
            }
        }
    }
}
