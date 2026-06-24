package com.sportcasterpro.app.core.domain.model

/** A single score change, persisted so a match's score history can be replayed for statistics. */
data class ScoreEvent(
    val id: String,
    val matchId: String,
    val teamId: String,
    val pointsDelta: Int,
    val periodLabel: String,
    val homeScoreAfter: Int,
    val awayScoreAfter: Int,
    val timestampEpochMillis: Long,
)
