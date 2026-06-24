package com.sportcasterpro.app.core.data.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.sportcasterpro.app.core.data.local.entity.MatchEntity
import com.sportcasterpro.app.core.data.local.entity.ScoreEventEntity
import com.sportcasterpro.app.core.data.local.entity.TeamEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun teamEntity(id: String, name: String) = TeamEntity(id = id, name = name, logoUri = null, colorHex = "#FF6A1A")

    private fun matchEntity(id: String, homeId: String, awayId: String) = MatchEntity(
        id = id,
        sport = "BASKETBALL",
        homeTeamId = homeId,
        awayTeamId = awayId,
        dateTimeEpochMillis = 1_000L,
        location = "Court 1",
        status = "SCHEDULED",
    )

    @Test
    fun observeMatch_joinsHomeAndAwayTeams() = runBlocking {
        database.teamDao().upsert(teamEntity("home", "Home Heroes"))
        database.teamDao().upsert(teamEntity("away", "Away Aces"))
        database.matchDao().upsert(matchEntity("match-1", "home", "away"))

        val result = database.matchDao().getMatch("match-1")

        assertThat(result).isNotNull()
        assertThat(result!!.homeTeam.name).isEqualTo("Home Heroes")
        assertThat(result.awayTeam.name).isEqualTo("Away Aces")
    }

    @Test
    fun updateStatus_changesOnlyTheStatusColumn() = runBlocking {
        database.teamDao().upsert(teamEntity("home", "Home Heroes"))
        database.teamDao().upsert(teamEntity("away", "Away Aces"))
        database.matchDao().upsert(matchEntity("match-1", "home", "away"))

        database.matchDao().updateStatus("match-1", "LIVE")

        val result = database.matchDao().getMatch("match-1")
        assertThat(result!!.match.status).isEqualTo("LIVE")
    }

    @Test
    fun observeMatches_ordersByScheduledTime() = runBlocking {
        database.teamDao().upsert(teamEntity("home", "Home Heroes"))
        database.teamDao().upsert(teamEntity("away", "Away Aces"))
        database.matchDao().upsert(matchEntity("later", "home", "away").copy(dateTimeEpochMillis = 2_000L))
        database.matchDao().upsert(matchEntity("earlier", "home", "away").copy(dateTimeEpochMillis = 1_000L))

        val matches = database.matchDao().observeMatches().first()

        assertThat(matches.map { it.match.id }).containsExactly("earlier", "later").inOrder()
    }

    @Test
    fun scoreEventHistory_isOrderedByTimestampAndScopedToMatch() = runBlocking {
        database.teamDao().upsert(teamEntity("home", "Home Heroes"))
        database.teamDao().upsert(teamEntity("away", "Away Aces"))
        database.matchDao().upsert(matchEntity("match-1", "home", "away"))
        database.matchDao().upsert(matchEntity("match-2", "home", "away"))

        database.scoreEventDao().insert(scoreEvent("event-2", "match-1", timestamp = 2_000L))
        database.scoreEventDao().insert(scoreEvent("event-1", "match-1", timestamp = 1_000L))
        database.scoreEventDao().insert(scoreEvent("event-other-match", "match-2", timestamp = 500L))

        val history = database.scoreEventDao().observeHistory("match-1").first()

        assertThat(history.map { it.id }).containsExactly("event-1", "event-2").inOrder()
    }

    @Test
    fun deletingAMatch_cascadesToItsScoreEvents() = runBlocking {
        database.teamDao().upsert(teamEntity("home", "Home Heroes"))
        database.teamDao().upsert(teamEntity("away", "Away Aces"))
        val match = matchEntity("match-1", "home", "away")
        database.matchDao().upsert(match)
        database.scoreEventDao().insert(scoreEvent("event-1", "match-1", timestamp = 1_000L))

        database.matchDao().delete(match)

        val history = database.scoreEventDao().observeHistory("match-1").first()
        assertThat(history).isEmpty()
    }

    private fun scoreEvent(id: String, matchId: String, timestamp: Long) = ScoreEventEntity(
        id = id,
        matchId = matchId,
        teamId = "home",
        pointsDelta = 1,
        periodLabel = "Quarter 1",
        homeScoreAfter = 1,
        awayScoreAfter = 0,
        timestampEpochMillis = timestamp,
    )
}
