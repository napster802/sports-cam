package com.sportcasterpro.app.feature.scoreboard.di

import com.sportcasterpro.app.feature.scoreboard.data.ScoreEventRepositoryImpl
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreEventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScoreboardModule {

    @Binds
    @Singleton
    abstract fun bindScoreEventRepository(impl: ScoreEventRepositoryImpl): ScoreEventRepository
}
