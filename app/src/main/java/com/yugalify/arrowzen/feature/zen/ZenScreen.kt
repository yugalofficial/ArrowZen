package com.yugalify.arrowzen.feature.zen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
 * Zen Mode (Section 10): no timer pressure, no mistake penalty, no forced
 * interruptions. The engine underneath is identical to every other mode --
 * what makes it Zen is that this screen never surfaces mistakes or a ticking
 * clock, and completion only ever rolls statistics forward, never fails
 * anything.
 */
@Composable
fun ZenScreen(onBackClicked: () -> Unit) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    val seed = remember { Random.nextLong() }
    val board = remember(seed) { PuzzleGenerator.generate(seed, Difficulty.EASY) }

    val factory = viewModelFactory {
        initializer {
            FreePlayViewModel(
                initialBoard = board,
                difficulty = Difficulty.EASY
            ) { _, hintsUsed, elapsedMillis, arrowsEscaped ->
                application.container.statisticsRepository.recordPuzzleCompleted(
                    arrowsEscaped = arrowsEscaped,
                    hintsUsed = hintsUsed,
                    playTimeMillis = elapsedMillis
                )
            }
        }
    }
    val viewModel: FreePlayViewModel = viewModel(factory = factory)

    FreePlayScreen(
        viewModel = viewModel,
        title = "Zen",
        showMistakes = false,
        showTimer = false,
        completionMessage = { _, _, _ -> "Nicely done. Take your time with the next one." },
        onBackClicked = onBackClicked
    )
}
