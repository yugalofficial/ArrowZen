package com.yugalify.arrowzen.data.database.entity

/**
 * Seed data for the achievements table, inserted once on first app launch
 * (Section 19). New achievements can be appended here safely — the DAO
 * inserts with IGNORE-on-conflict, so existing player progress is untouched.
 */
object AchievementCatalog {

    fun seedList(): List<AchievementEntity> = listOf(
        AchievementEntity(
            id = "first_escape",
            title = "First Escape",
            description = "Complete your first puzzle.",
            iconReference = "ic_achievement_first_escape",
            target = 1
        ),
        AchievementEntity(
            id = "escape_artist",
            title = "Escape Artist",
            description = "Complete 100 puzzles.",
            iconReference = "ic_achievement_escape_artist",
            target = 100
        ),
        AchievementEntity(
            id = "speed_arrow",
            title = "Speed Arrow",
            description = "Complete a puzzle in under 15 seconds.",
            iconReference = "ic_achievement_speed_arrow",
            target = 1
        ),
        AchievementEntity(
            id = "no_help_needed",
            title = "No Help Needed",
            description = "Complete 10 puzzles without using a hint.",
            iconReference = "ic_achievement_no_help_needed",
            target = 10
        ),
        AchievementEntity(
            id = "perfect_mind",
            title = "Perfect Mind",
            description = "Earn 3 stars on 10 levels.",
            iconReference = "ic_achievement_perfect_mind",
            target = 10
        ),
        AchievementEntity(
            id = "arrow_master",
            title = "Arrow Master",
            description = "Complete 500 puzzles.",
            iconReference = "ic_achievement_arrow_master",
            target = 500
        )
    )
}
