package com.yugalify.arrowzen.feature.endless

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.feature.freeplay.FreePlayScreen
import com.yugalify.arrowzen.feature.freeplay.FreePlayViewModel
import com.yugalify.arrowzen.game.generator.PuzzleGenerator
import kotlin.random.Random

/**
 * Endless Mode (Section 12): an unbroken chain of procedurally generated
 * puzzles with gradually increasing difficulty. Every board still goes
 * through the same [com.yugalify.arrowzen.game.validator.PuzzleValidator]
 * check inside [PuzzleGenerator] before it's ever shown, so a long Endless
 * run can never hit an impossible board.
 */
@Composable
fun EndlessScreen(onBackClicked: () -> Unit) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    var round by remember { mutableIntStateOf(1) }
    var seed by remember { mutableStateOf(Random.nextLong()) }
    var showRoundComplete by remember { mutableStateOf(false) }

    val difficulty = remember(round) { difficultyForRound(round) }
    val board = remember(seed, round) { PuzzleGenerator.generate(seed, difficulty) }

    val factory = viewModelFactory {
        initializer {
            FreePlayViewModel(initialBoard = board, difficulty = difficulty) { _, hintsUsed, elapsedMillis, arrowsEscaped ->
                application.container.statisticsRepository.recordPuzzleCompleted(arrowsEscaped, hintsUsed, elapsedMillis)
            }
        }
    }
    val viewModel: FreePlayViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) showRoundComplete = true
    }

    FreePlayScreen(
        viewModel = viewModel,
        title = "Endless — Round $round",
        showMistakes = true,
        showTimer = true,
        completionMessage = { _, _, _ -> "" }, // superseded by the round-complete dialog below
        onBackClicked = onBackClicked,
        showCompletionDialog = false
    )

    if (showRoundComplete) {
        AlertDialog(
            onDismissRequest = onBackClicked,
            title = { Text("Round $round Complete!") },
            text = { Text("Difficulty climbs a little more each round. Ready for round ${round + 1}?") },
            confirmButton = {
                Button(onClick = {
                    round++
                    seed = Random.nextLong()
                    showRoundComplete = false
                }) { Text("Next Round") }
            },
            dismissButton = {
                Button(onClick = onBackClicked) { Text("Stop Here") }
            }
        )
    }
}

/** Difficulty climbs roughly every 3 rounds, capping at EXPERT. */
private fun difficultyForRound(round: Int): Difficulty {
    val tiers = Difficulty.entries
    val index = ((round - 1) / 3).coerceAtMost(tiers.lastIndex)
    return tiers[index]
}
