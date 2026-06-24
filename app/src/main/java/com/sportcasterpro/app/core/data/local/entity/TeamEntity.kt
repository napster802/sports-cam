package com.sportcasterpro.app.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sportcasterpro.app.core.domain.model.Team

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logoUri: String?,
    val colorHex: String,
)

fun TeamEntity.toDomain() = Team(id = id, name = name, logoUri = logoUri, colorHex = colorHex)

fun Team.toEntity() = TeamEntity(id = id, name = name, logoUri = logoUri, colorHex = colorHex)
