package com.sportcasterpro.app.feature.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sportcasterpro.app.core.domain.util.DispatcherProvider
import com.sportcasterpro.app.core.domain.util.Resource
import com.sportcasterpro.app.feature.auth.domain.AuthRepository
import com.sportcasterpro.app.feature.auth.domain.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dispatchers: DispatcherProvider,
) : AuthRepository {

    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser?.toAuthUser())
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser?.toAuthUser()
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Resource<AuthUser> =
        withContext(dispatchers.io) {
            runCatching {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                result.user!!.toAuthUser()
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Sign in failed", it) },
            )
        }

    override suspend fun signUpWithEmail(email: String, password: String): Resource<AuthUser> =
        withContext(dispatchers.io) {
            runCatching {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                result.user!!.toAuthUser()
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Account creation failed", it) },
            )
        }

    override suspend fun signInWithGoogleIdToken(idToken: String): Resource<AuthUser> =
        withContext(dispatchers.io) {
            runCatching {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = firebaseAuth.signInWithCredential(credential).await()
                result.user!!.toAuthUser()
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Google sign in failed", it) },
            )
        }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private fun com.google.firebase.auth.FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )
}
