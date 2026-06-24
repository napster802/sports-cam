package com.sportcasterpro.app.feature.match.di

import com.sportcasterpro.app.feature.match.data.MatchRepositoryImpl
import com.sportcasterpro.app.feature.match.data.TeamRepositoryImpl
import com.sportcasterpro.app.feature.match.domain.MatchRepository
import com.sportcasterpro.app.feature.match.domain.TeamRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MatchModule {

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository

    @Binds
    @Singleton
    abstract fun bindTeamRepository(impl: TeamRepositoryImpl): TeamRepository
}
