package com.yugalify.arrowzen.core.constants

/**
 * Central, easily-editable configuration values.
 * Change [PACKAGE_NAME] in gradle.properties (ARROWZEN_PACKAGE_NAME) before publishing a fork.
 */
object AppConstants {
    const val APP_NAME = "ArrowZen"
    const val APP_FULL_NAME = "ArrowZen: Logic Escape"
    const val APP_TAGLINE = "Tap. Think. Escape."
    const val DEVELOPER_NAME = "Yugal"
    const val DEVELOPER_CREDIT = "Designed & Developed by Yugal"
    const val COPYRIGHT_YEAR = 2026
    const val COPYRIGHT_NOTICE = "© 2026 Yugal"

    // --- Fill these in before a real Play Store submission ---
    // Intentionally left blank rather than an invented URL. See docs/PLAY_STORE.md.
    const val PRIVACY_POLICY_URL = ""
    const val SUPPORT_EMAIL = ""
    const val WEBSITE_URL = ""

    const val MIN_SDK = 26
    const val TARGET_SDK = 36
    const val COMPILE_SDK = 36

    const val DAILY_CHALLENGE_SALT = "ArrowZenDaily"

    const val DATASTORE_SETTINGS_NAME = "arrowzen_settings"
    const val DATABASE_NAME = "arrowzen_database"
    const val DATABASE_VERSION = 1

    const val MOVE_ANIMATION_MIN_MS = 200
    const val MOVE_ANIMATION_MAX_MS = 400

    const val STARTER_LEVEL_COUNT = 50
    const val PLANNED_LEVEL_CAPACITY = 300
}
