package com.sportcasterpro.app.feature.auth.domain

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)
