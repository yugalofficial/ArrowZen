package com.yugalify.arrowzen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yugalify.arrowzen.data.datastore.ArrowZenSettings
import com.yugalify.arrowzen.ui.navigation.ArrowZenDestinations
import com.yugalify.arrowzen.ui.navigation.ArrowZenNavGraph
import com.yugalify.arrowzen.ui.theme.ArrowZenTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ArrowZenRoot(splashScreen)
        }
    }
}

/**
 * Resolves the first-launch onboarding decision and the theme preference
 * before rendering the real nav graph. The Android splash screen (installed
 * above) stays up until [settings] has its first value, so there's no
 * visible "blank frame -> content" flash while DataStore's first read
 * completes.
 */
@Composable
private fun ArrowZenRoot(splashScreen: androidx.core.splashscreen.SplashScreen) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    val settingsState = produceState<ArrowZenSettings?>(initialValue = null) {
        value = application.container.settingsRepository.settings.first()
    }
    val settings = settingsState.value

    splashScreen.setKeepOnScreenCondition { settings == null }

    if (settings != null) {
        ArrowZenTheme(themePreference = settings.theme) {
            ArrowZenNavGraph(
                startDestination = if (settings.onboardingCompleted) {
                    ArrowZenDestinations.HOME
                } else {
                    ArrowZenDestinations.ONBOARDING
                }
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize())
    }
}
