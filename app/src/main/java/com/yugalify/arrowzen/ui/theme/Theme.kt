package com.yugalify.arrowzen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Mirrors the Settings screen's Theme options (Section 38): System / Light / Dark. */
enum class ThemePreference { SYSTEM, LIGHT, DARK }

private val LightColors = lightColorScheme(
    primary = ZenAmberDeep,
    onPrimary = ZenCream,
    secondary = ZenSage,
    background = ZenCream,
    surface = ZenCream,
    onBackground = ZenCharcoal,
    onSurface = ZenCharcoal
)

private val DarkColors = darkColorScheme(
    primary = ZenAmber,
    onPrimary = ZenCharcoal,
    secondary = ZenSage,
    background = ZenDarkBackground,
    surface = ZenDarkSurface,
    onBackground = ZenCream,
    onSurface = ZenCream
)

@Composable
fun ArrowZenTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    val colorScheme = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArrowZenTypography,
        content = content
    )
}
