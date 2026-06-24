package com.sportcasterpro.app.feature.auth.domain

import com.sportcasterpro.app.core.domain.util.Resource
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>

    suspend fun signInWithEmail(email: String, password: String): Resource<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String): Resource<AuthUser>
    suspend fun signInWithGoogleIdToken(idToken: String): Resource<AuthUser>
    fun signOut()
}
