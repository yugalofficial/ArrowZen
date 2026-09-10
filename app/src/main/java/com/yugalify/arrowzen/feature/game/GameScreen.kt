package com.yugalify.arrowzen.feature.game

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yugalify.arrowzen.ArrowZenApplication
import com.yugalify.arrowzen.core.ads.AdConfig
import com.yugalify.arrowzen.core.ads.provideAdManager
import com.yugalify.arrowzen.core.audio.HapticEvent
import com.yugalify.arrowzen.core.audio.SfxEvent
import com.yugalify.arrowzen.core.util.ShareUtil
import com.yugalify.arrowzen.data.datastore.ArrowZenSettings
import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.game.engine.MoveValidator
import com.yugalify.arrowzen.game.hint.Hint
import kotlinx.coroutines.launch

@Composable
fun GameScreen(levelId: String, onBackClicked: () -> Unit, onSettingsClicked: () -> Unit) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    val adManager = remember { provideAdManager(application) }
    LaunchedEffect(Unit) { adManager.loadRewardedAd() }
    val factory = viewModelFactory {
        initializer { GameViewModel(levelId, application.container.completeLevelUseCase) }
    }
    val viewModel: GameViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val event by viewModel.events.collectAsState()
    val settings by application.container.settingsRepository.settings.collectAsState(initial = ArrowZenSettings())

    var hintMessage by remember { mutableStateOf<String?>(null) }
    var highlightedArrowId by remember { mutableStateOf<String?>(null) }
    var shakeKey by remember { mutableStateOf(0) }
    var shakingArrowId by remember { mutableStateOf<String?>(null) }
    var animatingArrowId by remember { mutableStateOf<String?>(null) }
    var showPauseMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(event) {
        when (val current = event) {
            is GameUiEvent.MoveBlocked -> {
                shakeKey++
                application.container.soundManager.play(SfxEvent.BLOCKED_MOVE, settings.soundEnabled)
                application.container.hapticsManager.perform(HapticEvent.BLOCKED_MOVE, settings.hapticsEnabled)
            }
            is GameUiEvent.HintRevealed -> {
                highlightedArrowId = null
                application.container.soundManager.play(SfxEvent.HINT, settings.soundEnabled)
                hintMessage = when (val hint = current.hint) {
                    is Hint.TextClue -> hint.message
                    is Hint.RegionHighlight -> hint.message
                    is Hint.ArrowHighlight -> {
                        highlightedArrowId = hint.arrow.id
                        "That arrow can escape right now."
                    }
                    is Hint.RevealMove -> {
                        highlightedArrowId = hint.arrow.id
                        "Try that highlighted arrow next."
                    }
                    Hint.NoHintAvailable -> null
                }
            }
            is GameUiEvent.LevelComplete -> {
                application.container.soundManager.play(SfxEvent.LEVEL_COMPLETE, settings.soundEnabled)
                application.container.hapticsManager.perform(HapticEvent.LEVEL_COMPLETE, settings.hapticsEnabled)
            }
            else -> Unit
        }
    }

    fun handleArrowTap(arrow: Arrow) {
        if (animatingArrowId != null) return
        hintMessage = null
        highlightedArrowId = null

        val isValidMove = MoveValidator.canEscape(state.board, arrow)
        if (isValidMove) {
            application.container.soundManager.play(SfxEvent.VALID_MOVE, settings.soundEnabled)
            application.container.hapticsManager.perform(HapticEvent.VALID_MOVE, settings.hapticsEnabled)
            val animationDuration = if (settings.reducedAnimations) 0 else 220
            animatingArrowId = arrow.id
            scope.launch {
                if (animationDuration > 0) {
                    kotlinx.coroutines.delay(animationDuration.toLong())
                }
                viewModel.onArrowTapped(arrow)
                animatingArrowId = null
            }
        } else {
            shakingArrowId = arrow.id
            viewModel.onArrowTapped(arrow)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = levelId) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.semantics { contentDescription = "Back" }
                    ) {
                        Text("←")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            showPauseMenu = true
                            viewModel.pauseTimer()
                        },
                        modifier = Modifier.semantics { contentDescription = "Pause" }
                    ) {
                        Text("⏸")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Time: ${state.elapsedMillis / 1000}s  •  Mistakes: ${state.mistakes}  •  Hints: ${state.hintsUsed}",
                style = MaterialTheme.typography.bodyMedium
            )

            PuzzleBoard(
                board = state.board,
                animatingArrowId = animatingArrowId,
                shakingArrowId = shakingArrowId,
                shakeKey = shakeKey,
                highlightedArrowId = highlightedArrowId,
                highContrast = settings.highContrast,
                onArrowTapped = ::handleArrowTap
            )

            hintMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = viewModel::onUndoClicked,
                    enabled = state.canUndo,
                    modifier = Modifier.semantics { contentDescription = "Undo last move" }
                ) {
                    Text("Undo")
                }
                OutlinedButton(
                    onClick = viewModel::onHintClicked,
                    modifier = Modifier.semantics { contentDescription = "Show a hint" }
                ) {
                    Text("Hint")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.onRestartClicked()
                        hintMessage = null
                        highlightedArrowId = null
                    },
                    modifier = Modifier.semantics { contentDescription = "Restart level" }
                ) {
                    Text("Restart")
                }
            }

            if (AdConfig.isAdsEnabled) {
                val activity = LocalContext.current as? Activity
                TextButton(onClick = {
                    activity?.let {
                        adManager.showRewardedAd(
                            activity = it,
                            onRewardEarned = { viewModel.onHintClicked() },
                            onUnavailable = { hintMessage = "No ad available right now — try again shortly." }
                        )
                    }
                }) {
                    Text("Watch Ad for Free Hint")
                }
            }
        }
    }

    if (state.isComplete && event is GameUiEvent.LevelComplete) {
        val progress = (event as GameUiEvent.LevelComplete).progress
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = onBackClicked,
            title = { Text("Level Complete!", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${"⭐".repeat(progress.bestStars)}\n" +
                        "Time: ${state.elapsedMillis / 1000}s\n" +
                        "Mistakes: ${state.mistakes}\n" +
                        "Hints used: ${state.hintsUsed}"
                )
            },
            confirmButton = {
                Button(onClick = onBackClicked) {
                    Text("Home")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    ShareUtil.shareText(
                        context,
                        ShareUtil.levelCompleteShareText(levelId, progress.bestStars, state.elapsedMillis / 1000)
                    )
                }) {
                    Text("Share")
                }
            }
        )
    }

    if (showPauseMenu) {
        AlertDialog(
            onDismissRequest = {
                showPauseMenu = false
                viewModel.resumeTimer()
            },
            title = { Text("Paused") },
            text = {
                Column(verticalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = {
                        showPauseMenu = false
                        viewModel.resumeTimer()
                    }) { Text("Resume") }
                    TextButton(onClick = {
                        showPauseMenu = false
                        viewModel.onRestartClicked()
                        hintMessage = null
                        highlightedArrowId = null
                    }) { Text("Restart") }
                    TextButton(onClick = {
                        showPauseMenu = false
                        onSettingsClicked()
                    }) { Text("Settings") }
                    TextButton(onClick = {
                        showPauseMenu = false
                        onBackClicked()
                    }) { Text("Exit") }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun PuzzleBoard(
    board: Board,
    animatingArrowId: String?,
    shakingArrowId: String?,
    shakeKey: Int,
    highlightedArrowId: String?,
    highContrast: Boolean,
    onArrowTapped: (Arrow) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until board.rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (column in 0 until board.columns) {
                    val arrow = board.arrowAt(row, column)
                    val isObstacle = board.hasObstacleAt(row, column)
                    BoardCell(
                        arrow = arrow,
                        isObstacle = isObstacle,
                        isAnimatingOut = arrow?.id == animatingArrowId,
                        isShaking = arrow?.id == shakingArrowId,
                        shakeKey = shakeKey,
                        isHighlighted = arrow?.id == highlightedArrowId,
                        highContrast = highContrast,
                        modifier = Modifier.weight(1f),
                        onTapped = { arrow?.let(onArrowTapped) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardCell(
    arrow: Arrow?,
    isObstacle: Boolean,
    isAnimatingOut: Boolean,
    isShaking: Boolean,
    shakeKey: Int,
    isHighlighted: Boolean,
    highContrast: Boolean,
    modifier: Modifier = Modifier,
    onTapped: () -> Unit
) {
    val shape: Shape = RoundedCornerShape(8.dp)
    val background = when {
        arrow != null && isHighlighted -> MaterialTheme.colorScheme.tertiary
        arrow != null -> MaterialTheme.colorScheme.primary
        isObstacle -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val borderWidth = if (highContrast) 2.dp else 0.dp

    // Lightweight "escape" animation: the tapped arrow shrinks and fades
    // rather than sliding a precise pixel distance, since exact per-cell
    // travel distance depends on runtime-measured cell size. This still
    // gives the tap -> feedback -> exit -> board-updates flow from
    // Section 25 without over-engineering the layout math.
    val exitScale = remember { Animatable(1f) }
    LaunchedEffect(isAnimatingOut) {
        if (isAnimatingOut) {
            exitScale.animateTo(0f, animationSpec = tween(durationMillis = 200))
        } else {
            exitScale.snapTo(1f)
        }
    }

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeKey) {
        if (isShaking) {
            shakeOffset.animateTo(10f, tween(40))
            shakeOffset.animateTo(-10f, tween(80))
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    val description = when {
        arrow == null -> null
        else -> "Arrow pointing ${arrow.direction.name.lowercase()}, tap to attempt escape"
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = exitScale.value
                scaleY = exitScale.value
                translationX = shakeOffset.value
            }
            .background(background, shape)
            .let { if (isHighlighted) it.border(3.dp, MaterialTheme.colorScheme.error, shape) else it }
            .let { if (borderWidth > 0.dp) it.border(borderWidth, MaterialTheme.colorScheme.onSurface, shape) else it }
            .clickable(enabled = arrow != null, onClick = onTapped)
            .let { if (description != null) it.semantics { contentDescription = description } else it },
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

private fun Direction.glyph(): String = when (this) {
    Direction.UP -> "↑"
    Direction.DOWN -> "↓"
    Direction.LEFT -> "←"
    Direction.RIGHT -> "→"
}
