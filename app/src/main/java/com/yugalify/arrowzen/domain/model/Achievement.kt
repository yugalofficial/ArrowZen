package com.yugalify.arrowzen.domain.model

/** UI-facing achievement (Section 19). */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconReference: String,
    val progress: Int,
    val target: Int,
    val isUnlocked: Boolean
)
