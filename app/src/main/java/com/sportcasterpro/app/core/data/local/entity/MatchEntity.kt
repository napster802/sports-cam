package com.sportcasterpro.app.core.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sportcasterpro.app.core.domain.model.Match
import com.sportcasterpro.app.core.domain.model.MatchStatus
import com.sportcasterpro.app.core.domain.model.Sport

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val sport: String,
    val homeTeamId: String,
    val awayTeamId: String,
    val dateTimeEpochMillis: Long,
    val location: String,
    val status: String,
)

/** A match row joined with its two team rows, as returned by [com.sportcasterpro.app.core.data.local.dao.MatchDao]. */
data class MatchWithTeams(
    @Embedded val match: MatchEntity,
    @Embedded(prefix = "home_") val homeTeam: TeamEntity,
    @Embedded(prefix = "away_") val awayTeam: TeamEntity,
)

fun MatchWithTeams.toDomain() = Match(
    id = match.id,
    sport = Sport.valueOf(match.sport),
    homeTeam = homeTeam.toDomain(),
    awayTeam = awayTeam.toDomain(),
    dateTimeEpochMillis = match.dateTimeEpochMillis,
    location = match.location,
    status = MatchStatus.valueOf(match.status),
)

fun Match.toEntity() = MatchEntity(
    id = id,
    sport = sport.name,
    homeTeamId = homeTeam.id,
    awayTeamId = awayTeam.id,
    dateTimeEpochMillis = dateTimeEpochMillis,
    location = location,
    status = status.name,
)
