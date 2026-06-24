package com.sportcasterpro.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import com.sportcasterpro.app.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.sportcasterpro.app.feature.auth.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAuthState: ObserveAuthStateUseCase,
    private val signOut: SignOutUseCase,
) : ViewModel() {

    val authUser = observeAuthState()

    fun onSignOut() = signOut()
}
