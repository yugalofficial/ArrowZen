package com.yugalify.arrowzen.feature.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yugalify.arrowzen.ArrowZenApplication
import com.yugalify.arrowzen.core.util.ShareUtil
import com.yugalify.arrowzen.domain.model.DailySummary
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.feature.freeplay.FreePlayScreen
import com.yugalify.arrowzen.feature.freeplay.FreePlayViewModel
import com.yugalify.arrowzen.game.generator.DailyPuzzleProvider

/**
 * Daily Challenge (Section 9): one deterministic puzzle per calendar date,
 * fully offline, no login. Shows the streak/status summary first; tapping
 * in starts the actual puzzle if it isn't already completed today.
 */
@Composable
fun DailyChallengeScreen(onBackClicked: () -> Unit) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    var summary by remember { mutableStateOf<DailySummary?>(null) }
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        summary = application.container.dailyChallengeRepository.getTodaySummary()
    }

    val currentSummary = summary
    if (started && currentSummary != null && !currentSummary.isCompletedToday) {
        val board = remember { DailyPuzzleProvider.boardFor(currentSummary.dateKey) }
        val factory = viewModelFactory {
            initializer {
                FreePlayViewModel(initialBoard = board, difficulty = Difficulty.MEDIUM) { mistakes, hintsUsed, elapsedMillis, arrowsEscaped ->
                    application.container.dailyChallengeRepository.recordCompletion(mistakes, hintsUsed, elapsedMillis)
                    application.container.statisticsRepository.recordPuzzleCompleted(arrowsEscaped, hintsUsed, elapsedMillis)
                    summary = application.container.dailyChallengeRepository.getTodaySummary()
                }
            }
        }
        val viewModel: FreePlayViewModel = viewModel(factory = factory)
        val context = LocalContext.current
        val liveState by viewModel.state.collectAsState()
        FreePlayScreen(
            viewModel = viewModel,
            title = "Daily Arrow",
            showMistakes = true,
            showTimer = true,
            completionMessage = { _, _, seconds -> "Daily Arrow complete in ${seconds}s. See you tomorrow!" },
            onBackClicked = onBackClicked,
            onShareClicked = {
                ShareUtil.shareText(
                    context,
                    ShareUtil.dailyChallengeShareText(
                        streak = summary?.currentStreak ?: 1,
                        elapsedSeconds = liveState.elapsedMillis / 1000,
                        wasPerfect = liveState.mistakes == 0
                    )
                )
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Arrow") },
                navigationIcon = { IconButton(onClick = onBackClicked) { Text("←") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            currentSummary?.let { s ->
                Text(text = "🔥 ${s.currentStreak} Day Streak", style = MaterialTheme.typography.headlineMedium)
                s.bestTimeMillis?.let {
                    Text(text = "Best time: ${it / 1000}s", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (s.isCompletedToday) {
                    Text(text = "You've already completed today's Daily Arrow.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Button(onClick = { started = true }) { Text("Play Today's Puzzle") }
                }
            }
        }
    }
}
