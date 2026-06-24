package com.sportcasterpro.app.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sportcasterpro.app.feature.auth.presentation.login.LoginScreen
import com.sportcasterpro.app.feature.auth.presentation.splash.SplashScreen
import com.sportcasterpro.app.feature.dashboard.presentation.DashboardScreen
import com.sportcasterpro.app.feature.livestream.presentation.LiveStreamScreen
import com.sportcasterpro.app.feature.match.presentation.creatematch.CreateMatchScreen
import com.sportcasterpro.app.feature.scoreboard.presentation.editor.ScoreboardEditorScreen
import com.sportcasterpro.app.feature.settings.presentation.SettingsScreen

private const val ANIM_DURATION_MS = 260

@Composable
fun SportCasterNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeSlideIn() },
        exitTransition = { fadeSlideOut() },
        popEnterTransition = { fadeSlideIn() },
        popExitTransition = { fadeSlideOut() },
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onCreateMatch = { navController.navigate(Screen.CreateMatch.route) },
                onOpenMatch = { matchId -> navController.navigate(Screen.ScoreboardEditor.routeFor(matchId)) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.CreateMatch.route) {
            CreateMatchScreen(
                onMatchCreated = { matchId ->
                    navController.navigate(Screen.ScoreboardEditor.routeFor(matchId)) {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.ScoreboardEditor.route,
            arguments = listOf(matchIdArgument(Screen.ScoreboardEditor.ARG_MATCH_ID)),
        ) { entry ->
            val matchId = entry.arguments?.getString(Screen.ScoreboardEditor.ARG_MATCH_ID).orEmpty()
            ScoreboardEditorScreen(
                matchId = matchId,
                onGoLive = { navController.navigate(Screen.LiveStream.routeFor(matchId)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.LiveStream.route,
            arguments = listOf(matchIdArgument(Screen.LiveStream.ARG_MATCH_ID)),
        ) { entry ->
            val matchId = entry.arguments?.getString(Screen.LiveStream.ARG_MATCH_ID).orEmpty()
            LiveStreamScreen(
                matchId = matchId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
            )
        }
    }
}

private fun matchIdArgument(name: String): NamedNavArgument = navArgument(name) { type = NavType.StringType }

private fun AnimatedContentTransitionScope<*>.fadeSlideIn() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_DURATION_MS))

private fun AnimatedContentTransitionScope<*>.fadeSlideOut() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_DURATION_MS))
