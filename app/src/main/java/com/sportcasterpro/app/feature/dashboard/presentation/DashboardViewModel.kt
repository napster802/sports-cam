package com.sportcasterpro.app.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportcasterpro.app.core.domain.model.Match
import com.sportcasterpro.app.feature.match.domain.usecase.GetMatchesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getMatches: GetMatchesUseCase,
) : ViewModel() {

    val matches: StateFlow<List<Match>> = getMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
