package com.sportcasterpro.app.core.di

import android.content.Context
import androidx.room.Room
import com.sportcasterpro.app.core.data.local.AppDatabase
import com.sportcasterpro.app.core.data.local.dao.MatchDao
import com.sportcasterpro.app.core.data.local.dao.PlayerDao
import com.sportcasterpro.app.core.data.local.dao.ScoreEventDao
import com.sportcasterpro.app.core.data.local.dao.TeamDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTeamDao(db: AppDatabase): TeamDao = db.teamDao()

    @Provides
    fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()

    @Provides
    fun provideMatchDao(db: AppDatabase): MatchDao = db.matchDao()

    @Provides
    fun provideScoreEventDao(db: AppDatabase): ScoreEventDao = db.scoreEventDao()
}
