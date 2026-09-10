package com.yugalify.arrowzen.feature.challenge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yugalify.arrowzen.ArrowZenApplication
import com.yugalify.arrowzen.data.datastore.ChallengeDataStore
import com.yugalify.arrowzen.data.datastore.ChallengeRecord
import com.yugalify.arrowzen.data.datastore.ChallengeType
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.feature.freeplay.FreePlayScreen
import com.yugalify.arrowzen.feature.freeplay.FreePlayViewModel
import com.yugalify.arrowzen.game.generator.PuzzleGenerator

private const val SPEED_TARGET_MILLIS = 30_000L

/**
 * Challenge Mode (Section 11): three fixed variants, each a pass/fail rule
 * layered on top of the same engine.
 *  - Perfect Run: complete with zero mistakes.
 *  - No Hint: complete without using a single hint.
 *  - Speed: complete within [SPEED_TARGET_MILLIS].
 * Every variant uses a fresh procedurally generated MEDIUM board per
 * attempt, since the point is repeatable practice, not a fixed puzzle.
 */
@Composable
fun ChallengeScreen(onBackClicked: () -> Unit) {
    var selected by remember { mutableStateOf<ChallengeType?>(null) }

    val currentSelection = selected
    if (currentSelection != null) {
        ChallengeAttemptScreen(type = currentSelection, onBackClicked = { selected = null })
        return
    }

    val application = LocalContext.current.applicationContext as ArrowZenApplication

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Challenge") },
                navigationIcon = { IconButton(onClick = onBackClicked) { Text("←") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChallengeOptionRow(
                type = ChallengeType.PERFECT_RUN,
                title = "Perfect Run",
                description = "Complete a puzzle without a single mistake.",
                dataStore = application.container.challengeDataStore,
                onClick = { selected = ChallengeType.PERFECT_RUN }
            )
            ChallengeOptionRow(
                type = ChallengeType.NO_HINT,
                title = "No Hint Challenge",
                description = "Complete a puzzle without using any hints.",
                dataStore = application.container.challengeDataStore,
                onClick = { selected = ChallengeType.NO_HINT }
            )
            ChallengeOptionRow(
                type = ChallengeType.SPEED,
                title = "Speed Challenge",
                description = "Complete a puzzle within ${SPEED_TARGET_MILLIS / 1000} seconds.",
                dataStore = application.container.challengeDataStore,
                onClick = { selected = ChallengeType.SPEED }
            )
        }
    }
}

@Composable
private fun ChallengeOptionRow(
    type: ChallengeType,
    title: String,
    description: String,
    dataStore: ChallengeDataStore,
    onClick: () -> Unit
) {
    val record by dataStore.recordFor(type).collectAsState(initial = ChallengeRecord())
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
        if (record.everCompleted) {
            val bestSuffix = record.bestTimeMillis?.let { " • best ${it / 1000}s" } ?: ""
            Text(text = "Completed$bestSuffix", style = MaterialTheme.typography.bodyMedium)
        }
        Button(onClick = onClick, modifier = Modifier.padding(top = 8.dp)) { Text("Play") }
    }
}

@Composable
private fun ChallengeAttemptScreen(type: ChallengeType, onBackClicked: () -> Unit) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    val seed = remember { kotlin.random.Random.nextLong() }
    val board = remember(seed) { PuzzleGenerator.generate(seed, Difficulty.MEDIUM) }

    val factory = viewModelFactory {
        initializer {
            FreePlayViewModel(initialBoard = board, difficulty = Difficulty.MEDIUM) { mistakes, hintsUsed, elapsedMillis, arrowsEscaped ->
                application.container.statisticsRepository.recordPuzzleCompleted(arrowsEscaped, hintsUsed, elapsedMillis)
                val passed = when (type) {
                    ChallengeType.PERFECT_RUN -> mistakes == 0
                    ChallengeType.NO_HINT -> hintsUsed == 0
                    ChallengeType.SPEED -> elapsedMillis <= SPEED_TARGET_MILLIS
                }
                if (passed) {
                    application.container.challengeDataStore.recordSuccess(type, elapsedMillis)
                }
            }
        }
    }
    val viewModel: FreePlayViewModel = viewModel(factory = factory)

    FreePlayScreen(
        viewModel = viewModel,
        title = when (type) {
            ChallengeType.PERFECT_RUN -> "Perfect Run"
            ChallengeType.NO_HINT -> "No Hint Challenge"
            ChallengeType.SPEED -> "Speed Challenge"
        },
        showMistakes = true,
        showTimer = true,
        completionMessage = { mistakes, hintsUsed, seconds ->
            val passed = when (type) {
                ChallengeType.PERFECT_RUN -> mistakes == 0
                ChallengeType.NO_HINT -> hintsUsed == 0
                ChallengeType.SPEED -> seconds * 1000 <= SPEED_TARGET_MILLIS
            }
            if (passed) "Challenge passed in ${seconds}s!" else "Solved, but this run didn't meet the challenge. Try again!"
        },
        onBackClicked = onBackClicked
    )
}
