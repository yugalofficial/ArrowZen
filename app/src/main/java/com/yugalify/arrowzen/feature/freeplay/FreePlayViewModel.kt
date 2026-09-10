package com.yugalify.arrowzen.feature.freeplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.domain.model.GameState
import com.yugalify.arrowzen.game.engine.GameEngine
import com.yugalify.arrowzen.game.engine.MoveResult
import com.yugalify.arrowzen.game.hint.Hint
import com.yugalify.arrowzen.game.hint.HintEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FreePlayEvent {
    data object MoveBlocked : FreePlayEvent
    data object LevelComplete : FreePlayEvent
    data class HintRevealed(val hint: Hint) : FreePlayEvent
}

/**
 * Generalized version of [com.yugalify.arrowzen.feature.game.GameViewModel]
 * for the non-Classic modes (Zen, Daily, Challenge, Endless). Those modes
 * don't have a stable `levelId` to persist per-level best records against --
 * a Daily puzzle is a new board every day, Endless boards are infinite and
 * disposable -- so this reports completion through a plain callback instead
 * of CompleteLevelUseCase. Each mode's screen decides what "completed"
 * should do (advance a streak, roll statistics forward, generate the next
 * Endless board, etc).
 */
class FreePlayViewModel(
    initialBoard: Board,
    difficulty: Difficulty,
    private val onCompletedCallback: suspend (mistakes: Int, hintsUsed: Int, elapsedMillis: Long, arrowsEscaped: Int) -> Unit
) : ViewModel() {

    private val totalArrowCount = initialBoard.arrows.size
    private val _state = MutableStateFlow(
        GameState(
            levelId = "freeplay",
            difficulty = difficulty,
            board = initialBoard,
            originalBoard = initialBoard
        )
    )
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _events = MutableStateFlow<FreePlayEvent?>(null)
    val events: StateFlow<FreePlayEvent?> = _events.asStateFlow()

    private var timerJob: Job? = null
    private var startTimeMillis = System.currentTimeMillis()

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        startTimeMillis = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (!_state.value.isComplete) {
                delay(1000)
                if (!_state.value.isComplete) {
                    _state.value = _state.value.copy(elapsedMillis = System.currentTimeMillis() - startTimeMillis)
                }
            }
        }
    }

    fun onArrowTapped(arrow: Arrow) {
        when (val result = GameEngine.attemptMove(_state.value, arrow)) {
            is MoveResult.Valid -> {
                _state.value = result.newState
                if (result.newState.isComplete) handleCompletion(result.newState)
            }
            is MoveResult.Blocked -> {
                _state.value = result.state
                _events.value = FreePlayEvent.MoveBlocked
            }
            is MoveResult.AlreadyComplete -> Unit
        }
    }

    fun onHintClicked() {
        val current = _state.value
        if (current.isComplete) return
        val tier = (current.hintsUsed % 4) + 1
        val hint = HintEngine.getHint(current.board, tier)
        if (hint is Hint.NoHintAvailable) return
        _state.value = current.copy(hintsUsed = current.hintsUsed + 1)
        _events.value = FreePlayEvent.HintRevealed(hint)
    }

    fun onUndoClicked() {
        _state.value = GameEngine.undo(_state.value)
    }

    fun onRestartClicked() {
        _state.value = GameEngine.restart(_state.value)
        startTimer()
    }

    fun consumeEvent() {
        _events.value = null
    }

    private fun handleCompletion(finalState: GameState) {
        timerJob?.cancel()
        val elapsed = System.currentTimeMillis() - startTimeMillis
        _state.value = finalState.copy(elapsedMillis = elapsed)

        viewModelScope.launch {
            onCompletedCallback(finalState.mistakes, finalState.hintsUsed, elapsed, totalArrowCount)
            _events.value = FreePlayEvent.LevelComplete
        }
    }
}
