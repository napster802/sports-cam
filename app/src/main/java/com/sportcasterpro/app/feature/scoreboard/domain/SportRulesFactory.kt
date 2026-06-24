package com.sportcasterpro.app.feature.scoreboard.domain

import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.feature.scoreboard.domain.rules.GenericPointRules
import com.sportcasterpro.app.feature.scoreboard.domain.rules.TennisRules
import com.sportcasterpro.app.feature.scoreboard.domain.rules.VolleyballRules
import javax.inject.Inject

class SportRulesFactory @Inject constructor() {

    fun rulesFor(sport: Sport): SportRules = when (sport) {
        Sport.VOLLEYBALL -> VolleyballRules()
        Sport.TENNIS -> TennisRules()
        else -> GenericPointRules()
    }
}
