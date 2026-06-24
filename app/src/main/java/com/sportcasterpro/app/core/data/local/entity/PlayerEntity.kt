package com.sportcasterpro.app.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sportcasterpro.app.core.domain.model.Player

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("teamId")],
)
data class PlayerEntity(
    @PrimaryKey val id: String,
    val teamId: String,
    val name: String,
    val jerseyNumber: Int,
)

fun PlayerEntity.toDomain() = Player(id = id, teamId = teamId, name = name, jerseyNumber = jerseyNumber)

fun Player.toEntity() = PlayerEntity(id = id, teamId = teamId, name = name, jerseyNumber = jerseyNumber)
