package com.yugalify.arrowzen.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yugalify.arrowzen.ArrowZenApplication
import com.yugalify.arrowzen.core.constants.AppConstants
import com.yugalify.arrowzen.domain.model.DailySummary

/**
 * Home screen (Section 22): logo, tagline, primary Play button, Daily
 * streak card, and mode navigation -- kept to one clean grid rather than a
 * long button list so it doesn't feel overcrowded on small phones.
 */
@Composable
fun HomeScreen(
    onPlayClicked: () -> Unit,
    onClassicClicked: () -> Unit,
    onDailyClicked: () -> Unit,
    onZenClicked: () -> Unit,
    onChallengeClicked: () -> Unit,
    onEndlessClicked: () -> Unit,
    onStatsClicked: () -> Unit,
    onAchievementsClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    var dailySummary by remember { mutableStateOf<DailySummary?>(null) }

    LaunchedEffect(Unit) {
        dailySummary = application.container.dailyChallengeRepository.getTodaySummary()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = AppConstants.APP_NAME,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = AppConstants.APP_TAGLINE, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onPlayClicked) { Text("PLAY") }

            Spacer(modifier = Modifier.height(16.dp))
            dailySummary?.let { summary ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onDailyClicked)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Arrow", style = MaterialTheme.typography.titleLarge)
                    Text("🔥 ${summary.currentStreak} Day Streak", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val modes = listOf(
                    "Classic" to onClassicClicked,
                    "Zen" to onZenClicked,
                    "Challenge" to onChallengeClicked,
                    "Endless" to onEndlessClicked,
                    "Statistics" to onStatsClicked,
                    "Achievements" to onAchievementsClicked,
                    "Settings" to onSettingsClicked
                )
                items(modes) { (label, onClick) ->
                    ModeCard(label = label, onClick = onClick)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = AppConstants.DEVELOPER_CREDIT, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ModeCard(label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
    }
}
