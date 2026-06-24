package com.sportcasterpro.app.core.domain.util

import kotlinx.coroutines.CoroutineDispatcher

/** Indirection over [kotlinx.coroutines.Dispatchers] so ViewModels/repositories stay testable with a test dispatcher. */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
