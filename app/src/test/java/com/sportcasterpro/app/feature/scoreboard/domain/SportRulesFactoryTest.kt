package com.sportcasterpro.app.feature.scoreboard.domain

import com.google.common.truth.Truth.assertThat
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.feature.scoreboard.domain.rules.GenericPointRules
import com.sportcasterpro.app.feature.scoreboard.domain.rules.TennisRules
import com.sportcasterpro.app.feature.scoreboard.domain.rules.VolleyballRules
import org.junit.Test

class SportRulesFactoryTest {

    private val factory = SportRulesFactory()

    @Test
    fun `volleyball resolves to VolleyballRules`() {
        assertThat(factory.rulesFor(Sport.VOLLEYBALL)).isInstanceOf(VolleyballRules::class.java)
    }

    @Test
    fun `tennis resolves to TennisRules`() {
        assertThat(factory.rulesFor(Sport.TENNIS)).isInstanceOf(TennisRules::class.java)
    }

    @Test
    fun `every other sport resolves to GenericPointRules`() {
        val otherSports = Sport.entries.filterNot { it == Sport.VOLLEYBALL || it == Sport.TENNIS }

        otherSports.forEach { sport ->
            assertThat(factory.rulesFor(sport)).isInstanceOf(GenericPointRules::class.java)
        }
    }
}
