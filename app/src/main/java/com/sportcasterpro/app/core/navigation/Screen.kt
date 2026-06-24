package com.sportcasterpro.app.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object CreateMatch : Screen("create_match")
    data object ScoreboardEditor : Screen("scoreboard_editor/{matchId}") {
        fun routeFor(matchId: String) = "scoreboard_editor/$matchId"
        const val ARG_MATCH_ID = "matchId"
    }
    data object LiveStream : Screen("live_stream/{matchId}") {
        fun routeFor(matchId: String) = "live_stream/$matchId"
        const val ARG_MATCH_ID = "matchId"
    }
    data object Settings : Screen("settings")
}
