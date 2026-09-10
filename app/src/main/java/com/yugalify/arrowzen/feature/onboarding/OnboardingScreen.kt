package com.yugalify.arrowzen.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yugalify.arrowzen.ArrowZenApplication
import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.game.engine.GameEngine
import com.yugalify.arrowzen.game.engine.MoveResult
import kotlinx.coroutines.launch

private data class OnboardingStep(val microcopy: String, val board: Board)

/**
 * Interactive onboarding (Section 36): three tiny hands-on puzzles, no long
 * text pages. Step 1 teaches tapping, step 2 teaches blocking, step 3
 * teaches sequence -- each solvable in a few seconds, under a minute total.
 * Returning users can skip entirely.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    val scope = rememberCoroutineScope()

    val steps = remember {
        listOf(
            OnboardingStep(
                microcopy = "Tap the arrow to escape.",
                board = Board(rows = 2, columns = 2, arrows = listOf(Arrow("t1", 0, 0, Direction.RIGHT)))
            ),
            OnboardingStep(
                microcopy = "Some arrows block others.",
                board = Board(
                    rows = 2, columns = 3,
                    arrows = listOf(
                        Arrow("t1", 0, 0, Direction.RIGHT),
                        Arrow("t2", 0, 2, Direction.DOWN)
                    )
                )
            ),
            OnboardingStep(
                microcopy = "Think before you tap.",
                board = Board(
                    rows = 3, columns = 3,
                    arrows = listOf(
                        Arrow("t1", 0, 0, Direction.RIGHT),
                        Arrow("t2", 0, 2, Direction.DOWN),
                        Arrow("t3", 2, 0, Direction.UP)
                    )
                )
            )
        )
    }

    var stepIndex by remember { mutableIntStateOf(0) }
    var board by remember { mutableStateOf(steps[0].board) }

    fun finish() {
        scope.launch { application.container.settingsRepository.setOnboardingCompleted(true) }
        onFinished()
    }

    Scaffold(
        topBar = {},
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { finish() }) { Text("Skip") }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = steps[stepIndex].microcopy,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )

            OnboardingBoard(board = board) { arrow ->
                val fakeState = com.yugalify.arrowzen.domain.model.GameState(
                    levelId = "onboarding",
                    difficulty = com.yugalify.arrowzen.domain.model.Difficulty.BEGINNER,
                    board = board,
                    originalBoard = steps[stepIndex].board
                )
                when (val result = GameEngine.attemptMove(fakeState, arrow)) {
                    is MoveResult.Valid -> {
                        board = result.newState.board
                        if (board.isSolved) {
                            if (stepIndex < steps.lastIndex) {
                                stepIndex++
                                board = steps[stepIndex].board
                            } else {
                                finish()
                            }
                        }
                    }
                    else -> Unit
                }
            }

            Text(
                text = "Step ${stepIndex + 1} of ${steps.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun OnboardingBoard(board: Board, onArrowTapped: (Arrow) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.3f)
            .border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until board.rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (column in 0 until board.columns) {
                    val arrow = board.arrowAt(row, column)
                    val isObstacle = board.hasObstacleAt(row, column)
                    val background = when {
                        arrow != null -> MaterialTheme.colorScheme.primary
                        isObstacle -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(background, RoundedCornerShape(8.dp))
                            .clickable(enabled = arrow != null) { arrow?.let(onArrowTapped) },
                        contentAlignment = Alignment.Center
                    ) {
                        arrow?.let {
                            Text(
                                text = it.direction.glyph(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Direction.glyph(): String = when (this) {
    Direction.UP -> "↑"
    Direction.DOWN -> "↓"
    Direction.LEFT -> "←"
    Direction.RIGHT -> "→"
}
