package com.sportcasterpro.app.core.domain.model

enum class MatchStatus { SCHEDULED, LIVE, COMPLETED, CANCELLED }

data class Match(
    val id: String,
    val sport: Sport,
    val homeTeam: Team,
    val awayTeam: Team,
    val dateTimeEpochMillis: Long,
    val location: String,
    val status: MatchStatus,
)
