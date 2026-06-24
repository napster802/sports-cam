package com.sportcasterpro.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sportcasterpro.app.core.data.local.dao.MatchDao
import com.sportcasterpro.app.core.data.local.dao.PlayerDao
import com.sportcasterpro.app.core.data.local.dao.ScoreEventDao
import com.sportcasterpro.app.core.data.local.dao.TeamDao
import com.sportcasterpro.app.core.data.local.entity.MatchEntity
import com.sportcasterpro.app.core.data.local.entity.PlayerEntity
import com.sportcasterpro.app.core.data.local.entity.ScoreEventEntity
import com.sportcasterpro.app.core.data.local.entity.TeamEntity

@Database(
    entities = [
        TeamEntity::class,
        PlayerEntity::class,
        MatchEntity::class,
        ScoreEventEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
    abstract fun scoreEventDao(): ScoreEventDao

    companion object {
        const val DATABASE_NAME = "sportcaster_pro.db"
    }
}
