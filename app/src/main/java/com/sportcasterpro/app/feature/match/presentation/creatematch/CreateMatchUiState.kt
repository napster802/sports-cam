package com.sportcasterpro.app.feature.match.presentation.creatematch

import com.sportcasterpro.app.core.domain.model.Sport

data class CreateMatchUiState(
    val sport: Sport = Sport.BASKETBALL,
    val homeTeamName: String = "",
    val awayTeamName: String = "",
    val location: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val createdMatchId: String? = null,
)
