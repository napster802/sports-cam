package com.sportcasterpro.app.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sportcasterpro.app.core.domain.model.ScoreEvent

@Entity(
    tableName = "score_events",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("matchId")],
)
data class ScoreEventEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val teamId: String,
    val pointsDelta: Int,
    val periodLabel: String,
    val homeScoreAfter: Int,
    val awayScoreAfter: Int,
    val timestampEpochMillis: Long,
)

fun ScoreEventEntity.toDomain() = ScoreEvent(
    id = id,
    matchId = matchId,
    teamId = teamId,
    pointsDelta = pointsDelta,
    periodLabel = periodLabel,
    homeScoreAfter = homeScoreAfter,
    awayScoreAfter = awayScoreAfter,
    timestampEpochMillis = timestampEpochMillis,
)

fun ScoreEvent.toEntity() = ScoreEventEntity(
    id = id,
    matchId = matchId,
    teamId = teamId,
    pointsDelta = pointsDelta,
    periodLabel = periodLabel,
    homeScoreAfter = homeScoreAfter,
    awayScoreAfter = awayScoreAfter,
    timestampEpochMillis = timestampEpochMillis,
)
