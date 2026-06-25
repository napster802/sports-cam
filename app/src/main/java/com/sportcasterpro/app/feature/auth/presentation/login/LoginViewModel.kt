package com.sportcasterpro.app.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportcasterpro.app.core.domain.util.Resource
import com.sportcasterpro.app.feature.auth.domain.usecase.SignInWithEmailUseCase
import com.sportcasterpro.app.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.sportcasterpro.app.feature.auth.domain.usecase.SignUpWithEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithEmail: SignInWithEmailUseCase,
    private val signUpWithEmail: SignUpWithEmailUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (state.isSignUpMode) {
                signUpWithEmail(state.email, state.password)
            } else {
                signInWithEmail(state.email, state.password)
            }
            applyResult(result)
        }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            applyResult(signInWithGoogle(idToken))
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    private fun applyResult(result: Resource<*>) {
        when (result) {
            is Resource.Success<*> -> _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            Resource.Loading -> Unit
        }
    }
}
