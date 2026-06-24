package com.sportcasterpro.app.feature.auth.domain.usecase

import com.sportcasterpro.app.core.domain.util.Resource
import com.sportcasterpro.app.feature.auth.domain.AuthRepository
import com.sportcasterpro.app.feature.auth.domain.AuthUser
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke(): StateFlow<AuthUser?> = repository.currentUser
}

class SignInWithEmailUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Resource<AuthUser> {
        if (email.isBlank() || password.isBlank()) {
            return Resource.Error("Email and password are required")
        }
        return repository.signInWithEmail(email.trim(), password)
    }
}

class SignUpWithEmailUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Resource<AuthUser> {
        if (email.isBlank() || password.length < 6) {
            return Resource.Error("Password must be at least 6 characters")
        }
        return repository.signUpWithEmail(email.trim(), password)
    }
}

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(idToken: String): Resource<AuthUser> =
        repository.signInWithGoogleIdToken(idToken)
}

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke() = repository.signOut()
}
