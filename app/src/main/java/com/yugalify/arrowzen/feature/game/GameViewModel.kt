package com.yugalify.arrowzen.feature.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.GameState
import com.yugalify.arrowzen.domain.model.LevelProgress
import com.yugalify.arrowzen.domain.usecase.CompleteLevelUseCase
import com.yugalify.arrowzen.game.engine.GameEngine
import com.yugalify.arrowzen.game.engine.MoveResult
import com.yugalify.arrowzen.game.generator.LevelCatalog
import com.yugalify.arrowzen.game.hint.Hint
import com.yugalify.arrowzen.game.hint.HintEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One-shot feedback events for the UI layer (haptics/sound hooks land later in Phase 6). */
sealed interface GameUiEvent {
    data object MoveBlocked : GameUiEvent
    data class LevelComplete(val progress: LevelProgress) : GameUiEvent
    data class HintRevealed(val hint: Hint) : GameUiEvent
}

class GameViewModel(
    private val levelId: String,
    private val completeLevelUseCase: CompleteLevelUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<GameState>
    val state: StateFlow<GameState>

    private val _events = MutableStateFlow<GameUiEvent?>(null)
    val events: StateFlow<GameUiEvent?> = _events.asStateFlow()

    private val totalArrowCount: Int
    private var timerJob: Job? = null
    private var segmentStartMillis = System.currentTimeMillis()
    private var accumulatedElapsedMillis = 0L

    init {
        val board = LevelCatalog.boardFor(levelId)
        totalArrowCount = board.arrows.size
        _state = MutableStateFlow(
            GameState(
                levelId = levelId,
                difficulty = LevelCatalog.difficultyFor(levelId),
                board = board,
                originalBoard = board
            )
        )
        state = _state.asStateFlow()
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        segmentStartMillis = System.currentTimeMillis()
        timerJob = launchTimerLoop()
    }

    /** Pauses the timer without losing time already elapsed (Section 37 pause menu). */
    fun pauseTimer() {
        if (timerJob == null) return
        accumulatedElapsedMillis += System.currentTimeMillis() - segmentStartMillis
        timerJob?.cancel()
        timerJob = null
    }

    fun resumeTimer() {
        if (timerJob != null || _state.value.isComplete) return
        segmentStartMillis = System.currentTimeMillis()
        timerJob = launchTimerLoop()
    }

    private fun launchTimerLoop(): Job = viewModelScope.launch {
        while (!_state.value.isComplete) {
            delay(1000)
            if (!_state.value.isComplete) {
                _state.value = _state.value.copy(
                    elapsedMillis = accumulatedElapsedMillis + (System.currentTimeMillis() - segmentStartMillis)
                )
            }
        }
    }

    fun onArrowTapped(arrow: Arrow) {
        when (val result = GameEngine.attemptMove(_state.value, arrow)) {
            is MoveResult.Valid -> {
                _state.value = result.newState
                if (result.newState.isComplete) {
                    onLevelCompleted(result.newState)
                }
            }
            is MoveResult.Blocked -> {
                _state.value = result.state
                _events.value = GameUiEvent.MoveBlocked
            }
            is MoveResult.AlreadyComplete -> Unit
        }
    }

    /** Hint tiers escalate each time the player asks again on the same board state (Section 14). */
    fun onHintClicked() {
        val current = _state.value
        if (current.isComplete) return

        val tier = (current.hintsUsed % 4) + 1
        val hint = HintEngine.getHint(current.board, tier)
        if (hint is Hint.NoHintAvailable) return

        _state.value = current.copy(hintsUsed = current.hintsUsed + 1)
        _events.value = GameUiEvent.HintRevealed(hint)
    }

    private fun onLevelCompleted(finalState: GameState) {
        timerJob?.cancel()
        val elapsed = accumulatedElapsedMillis + (System.currentTimeMillis() - segmentStartMillis)
        _state.value = finalState.copy(elapsedMillis = elapsed)

        viewModelScope.launch {
            val progress = completeLevelUseCase(
                levelId = levelId,
                arrowsEscaped = totalArrowCount,
                mistakes = finalState.mistakes,
                hintsUsed = finalState.hintsUsed,
                elapsedMillis = elapsed
            )
            _events.value = GameUiEvent.LevelComplete(progress)
        }
    }

    fun onUndoClicked() {
        _state.value = GameEngine.undo(_state.value)
    }

    fun onRestartClicked() {
        _state.value = GameEngine.restart(_state.value)
        accumulatedElapsedMillis = 0L
        startTimer()
    }

    fun consumeEvent() {
        _events.value = null
    }
}
