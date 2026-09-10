package com.yugalify.arrowzen.ui.navigation

/** Central list of navigation routes. */
object ArrowZenDestinations {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val GAME = "game/{levelId}"
    const val LEVELS = "levels"
    const val DAILY = "daily"
    const val ZEN = "zen"
    const val CHALLENGE = "challenge"
    const val ENDLESS = "endless"
    const val STATS = "stats"
    const val ACHIEVEMENTS = "achievements"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun gameRoute(levelId: String) = "game/$levelId"
}
