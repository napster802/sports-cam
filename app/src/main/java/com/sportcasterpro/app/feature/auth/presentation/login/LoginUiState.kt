package com.sportcasterpro.app.feature.auth.presentation.login

data class LoginUiState(
    val email: String = "aldenarmenteros@gmail.com",
    val password: String = "",
    val isSignUpMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
)
