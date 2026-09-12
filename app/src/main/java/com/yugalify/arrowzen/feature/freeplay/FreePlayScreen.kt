package com.yugalify.arrowzen.feature.freeplay

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yugalify.arrowzen.ArrowZenApplication
import com.yugalify.arrowzen.core.audio.HapticEvent
import com.yugalify.arrowzen.core.audio.SfxEvent
import com.yugalify.arrowzen.data.datastore.ArrowZenSettings
import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.game.engine.MoveValidator
import com.yugalify.arrowzen.game.hint.Hint

/**
 * Shared board/controls UI for every non-Classic mode. [title] and
 * [showMistakes]/[showTimer] let each mode frame the same engine
 * differently -- Zen Mode (Section 10) hides both since there's no
 * pressure by design, while Challenge Mode's Speed variant emphasizes
 * the timer.
 */
@Composable
fun FreePlayScreen(
    viewModel: FreePlayViewModel,
    title: String,
    showMistakes: Boolean,
    showTimer: Boolean,
    completionMessage: (mistakes: Int, hintsUsed: Int, elapsedSeconds: Long) -> String,
    onBackClicked: () -> Unit,
    showCompletionDialog: Boolean = true,
    onShareClicked: (() -> Unit)? = null
) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    val state by viewModel.state.collectAsState()
    val event by viewModel.events.collectAsState()
    val settings by application.container.settingsRepository.settings.collectAsState(initial = ArrowZenSettings())
    var hintMessage by remember { mutableStateOf<String?>(null) }
    var highlightedArrowId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(event) {
        when (val current = event) {
            is FreePlayEvent.HintRevealed -> {
                application.container.soundManager.play(SfxEvent.HINT, settings.soundEnabled)
                highlightedArrowId = null
                hintMessage = when (val hint = current.hint) {
                    is Hint.TextClue -> hint.message
                    is Hint.RegionHighlight -> hint.message
                    is Hint.ArrowHighlight -> { highlightedArrowId = hint.arrow.id; "That arrow can escape right now." }
                    is Hint.RevealMove -> { highlightedArrowId = hint.arrow.id; "Try that highlighted arrow next." }
                    Hint.NoHintAvailable -> null
                }
                viewModel.consumeEvent()
            }
            is FreePlayEvent.MoveBlocked -> {
                application.container.soundManager.play(SfxEvent.BLOCKED_MOVE, settings.soundEnabled)
                application.container.hapticsManager.perform(HapticEvent.BLOCKED_MOVE, settings.hapticsEnabled)
                viewModel.consumeEvent()
            }
            is FreePlayEvent.LevelComplete -> {
                application.container.soundManager.play(SfxEvent.LEVEL_COMPLETE, settings.soundEnabled)
                application.container.hapticsManager.perform(HapticEvent.LEVEL_COMPLETE, settings.hapticsEnabled)
            }
            null -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBackClicked) { Text("←") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val statusParts = mutableListOf<String>()
            if (showTimer) statusParts += "Time: ${state.elapsedMillis / 1000}s"
            if (showMistakes) statusParts += "Mistakes: ${state.mistakes}"
            statusParts += "Hints: ${state.hintsUsed}"
            Text(text = statusParts.joinToString("  •  "), style = MaterialTheme.typography.bodyMedium)

            FreePlayBoard(
                board = state.board,
                highlightedArrowId = highlightedArrowId,
                onArrowTapped = {
                    hintMessage = null
                    highlightedArrowId = null
                    if (MoveValidator.canEscape(state.board, it)) {
                        application.container.soundManager.play(SfxEvent.VALID_MOVE, settings.soundEnabled)
                        application.container.hapticsManager.perform(HapticEvent.VALID_MOVE, settings.hapticsEnabled)
                    }
                    viewModel.onArrowTapped(it)
                }
            )

            hintMessage?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = viewModel::onUndoClicked, enabled = state.canUndo) { Text("Undo") }
                OutlinedButton(onClick = viewModel::onHintClicked) { Text("Hint") }
                OutlinedButton(onClick = {
                    viewModel.onRestartClicked()
                    hintMessage = null
                    highlightedArrowId = null
                }) { Text("Restart") }
            }
        }
    }

    if (showCompletionDialog && state.isComplete && event is FreePlayEvent.LevelComplete) {
        AlertDialog(
            onDismissRequest = onBackClicked,
            title = { Text("Complete!", fontWeight = FontWeight.Bold) },
            text = { Text(completionMessage(state.mistakes, state.hintsUsed, state.elapsedMillis / 1000)) },
            confirmButton = { Button(onClick = onBackClicked) { Text("Home") } },
            dismissButton = if (onShareClicked != null) {
                { OutlinedButton(onClick = onShareClicked) { Text("Share") } }
            } else null
        )
    }
}

@Composable
private fun FreePlayBoard(board: Board, highlightedArrowId: String?, onArrowTapped: (Arrow) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until board.rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (column in 0 until board.columns) {
                    val arrow = board.arrowAt(row, column)
                    val isObstacle = board.hasObstacleAt(row, column)
                    val isHighlighted = arrow?.id == highlightedArrowId
                    val background = when {
                        isHighlighted -> MaterialTheme.colorScheme.tertiary
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
