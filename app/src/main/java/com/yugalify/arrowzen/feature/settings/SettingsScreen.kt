package com.yugalify.arrowzen.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yugalify.arrowzen.ArrowZenApplication
import com.yugalify.arrowzen.core.constants.AppConstants
import com.yugalify.arrowzen.data.datastore.ArrowZenSettings
import com.yugalify.arrowzen.ui.theme.ThemePreference
import kotlinx.coroutines.launch

/** Settings screen (Section 38): theme, sound, music, haptics, accessibility, reset tutorial, about. */
@Composable
fun SettingsScreen(onBackClicked: () -> Unit, onAboutClicked: () -> Unit) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    val repo = application.container.settingsRepository
    val settings by repo.settings.collectAsState(initial = ArrowZenSettings())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBackClicked) { Text("←") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionLabel("Theme")
            ThemeRow("System", settings.theme == ThemePreference.SYSTEM) {
                scope.launch { repo.setTheme(ThemePreference.SYSTEM) }
            }
            ThemeRow("Light", settings.theme == ThemePreference.LIGHT) {
                scope.launch { repo.setTheme(ThemePreference.LIGHT) }
            }
            ThemeRow("Dark", settings.theme == ThemePreference.DARK) {
                scope.launch { repo.setTheme(ThemePreference.DARK) }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SectionLabel("Audio & Feedback")
            SettingSwitchRow("Sound", settings.soundEnabled) { scope.launch { repo.setSoundEnabled(it) } }
            SettingSwitchRow("Music", settings.musicEnabled) { scope.launch { repo.setMusicEnabled(it) } }
            SettingSwitchRow("Haptics", settings.hapticsEnabled) { scope.launch { repo.setHapticsEnabled(it) } }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SectionLabel("Accessibility")
            SettingSwitchRow("Reduced Animations", settings.reducedAnimations) { scope.launch { repo.setReducedAnimations(it) } }
            SettingSwitchRow("High Contrast", settings.highContrast) { scope.launch { repo.setHighContrast(it) } }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SectionLabel("General")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Reset tutorial", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.clickable {
                    scope.launch { repo.setOnboardingCompleted(false) }
                })
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable(onClick = onAboutClicked),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("About", style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                text = "Version ${AppConstants.APP_NAME} 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
}

@Composable
private fun ThemeRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(if (selected) "✓" else "", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
