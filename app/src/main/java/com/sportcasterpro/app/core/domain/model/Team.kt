package com.sportcasterpro.app.core.domain.model

data class Team(
    val id: String,
    val name: String,
    val logoUri: String? = null,
    val colorHex: String = "#FF6A1A",
)
