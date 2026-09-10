package com.yugalify.arrowzen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yugalify.arrowzen.feature.about.AboutScreen
import com.yugalify.arrowzen.feature.achievements.AchievementsScreen
import com.yugalify.arrowzen.feature.challenge.ChallengeScreen
import com.yugalify.arrowzen.feature.daily.DailyChallengeScreen
import com.yugalify.arrowzen.feature.endless.EndlessScreen
import com.yugalify.arrowzen.feature.game.GameScreen
import com.yugalify.arrowzen.feature.home.HomeScreen
import com.yugalify.arrowzen.feature.levels.LevelSelectScreen
import com.yugalify.arrowzen.feature.onboarding.OnboardingScreen
import com.yugalify.arrowzen.feature.settings.SettingsScreen
import com.yugalify.arrowzen.feature.stats.StatsScreen
import com.yugalify.arrowzen.feature.zen.ZenScreen

@Composable
fun ArrowZenNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = ArrowZenDestinations.HOME
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(ArrowZenDestinations.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(ArrowZenDestinations.HOME) {
                        popUpTo(ArrowZenDestinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(ArrowZenDestinations.HOME) {
            HomeScreen(
                onPlayClicked = { navController.navigate(ArrowZenDestinations.LEVELS) },
                onClassicClicked = { navController.navigate(ArrowZenDestinations.LEVELS) },
                onDailyClicked = { navController.navigate(ArrowZenDestinations.DAILY) },
                onZenClicked = { navController.navigate(ArrowZenDestinations.ZEN) },
                onChallengeClicked = { navController.navigate(ArrowZenDestinations.CHALLENGE) },
                onEndlessClicked = { navController.navigate(ArrowZenDestinations.ENDLESS) },
                onStatsClicked = { navController.navigate(ArrowZenDestinations.STATS) },
                onAchievementsClicked = { navController.navigate(ArrowZenDestinations.ACHIEVEMENTS) },
                onSettingsClicked = { navController.navigate(ArrowZenDestinations.SETTINGS) }
            )
        }
        composable(ArrowZenDestinations.LEVELS) {
            LevelSelectScreen(
                onLevelSelected = { levelId -> navController.navigate(ArrowZenDestinations.gameRoute(levelId)) },
                onBackClicked = { navController.popBackStack() }
            )
        }
        composable(ArrowZenDestinations.GAME) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "level_001"
            GameScreen(
                levelId = levelId,
                onBackClicked = { navController.popBackStack() },
                onSettingsClicked = { navController.navigate(ArrowZenDestinations.SETTINGS) }
            )
        }
        composable(ArrowZenDestinations.DAILY) {
            DailyChallengeScreen(onBackClicked = { navController.popBackStack() })
        }
        composable(ArrowZenDestinations.ZEN) {
            ZenScreen(onBackClicked = { navController.popBackStack() })
        }
        composable(ArrowZenDestinations.CHALLENGE) {
            ChallengeScreen(onBackClicked = { navController.popBackStack() })
        }
        composable(ArrowZenDestinations.ENDLESS) {
            EndlessScreen(onBackClicked = { navController.popBackStack() })
        }
        composable(ArrowZenDestinations.STATS) {
            StatsScreen(onBackClicked = { navController.popBackStack() })
        }
        composable(ArrowZenDestinations.ACHIEVEMENTS) {
            AchievementsScreen(onBackClicked = { navController.popBackStack() })
        }
        composable(ArrowZenDestinations.SETTINGS) {
            SettingsScreen(
                onBackClicked = { navController.popBackStack() },
                onAboutClicked = { navController.navigate(ArrowZenDestinations.ABOUT) }
            )
        }
        composable(ArrowZenDestinations.ABOUT) {
            AboutScreen(onBackClicked = { navController.popBackStack() })
        }
    }
}
