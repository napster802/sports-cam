package com.sportcasterpro.app.feature.scoreboard.presentation.editor

import com.sportcasterpro.app.core.domain.model.Match

data class ScoreboardEditorUiState(
    val match: Match? = null,
    val homeTeamName: String = "",
    val awayTeamName: String = "",
    val homeColorHex: String = "#FF6A1A",
    val awayColorHex: String = "#1A6AFF",
    val isSaving: Boolean = false,
    val readyToGoLive: Boolean = false,
) {
    val isLoading: Boolean get() = match == null
}
