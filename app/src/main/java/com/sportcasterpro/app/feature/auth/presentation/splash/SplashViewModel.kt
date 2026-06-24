package com.sportcasterpro.app.feature.auth.presentation.splash

import androidx.lifecycle.ViewModel
import com.sportcasterpro.app.feature.auth.domain.usecase.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    observeAuthState: ObserveAuthStateUseCase,
) : ViewModel() {

    val authState = observeAuthState()
}
