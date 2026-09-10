package com.yugalify.arrowzen.game.hint

import com.yugalify.arrowzen.domain.model.Arrow
import com.yugalify.arrowzen.domain.model.Board
import com.yugalify.arrowzen.domain.model.Direction
import com.yugalify.arrowzen.game.engine.MoveValidator
import org.junit.Assert.assertTrue
import org.junit.Test

class HintEngineTest {

    private fun sampleBoard() = Board(
        rows = 4,
        columns = 4,
        arrows = listOf(
            Arrow(id = "a1", row = 0, column = 0, direction = Direction.RIGHT),
            Arrow(id = "a2", row = 0, column = 2, direction = Direction.DOWN)
        )
    )

    @Test
    fun `tier 1 returns a generic text clue`() {
        val hint = HintEngine.getHint(sampleBoard(), tier = 1)
        assertTrue(hint is Hint.TextClue)
    }

    @Test
    fun `tier 2 returns a region highlight`() {
        val hint = HintEngine.getHint(sampleBoard(), tier = 2)
        assertTrue(hint is Hint.RegionHighlight)
    }

    @Test
    fun `tier 3 highlights an arrow that is actually a legal move right now`() {
        val board = sampleBoard()
        val hint = HintEngine.getHint(board, tier = 3)
        require(hint is Hint.ArrowHighlight)
        assertTrue(MoveValidator.canEscape(board, hint.arrow))
    }

    @Test
    fun `tier 4 reveals an arrow that is a legal move right now`() {
        val board = sampleBoard()
        val hint = HintEngine.getHint(board, tier = 4)
        require(hint is Hint.RevealMove)
        assertTrue(MoveValidator.canEscape(board, hint.arrow))
    }

    @Test
    fun `hint always reflects current board state, not the original layout`() {
        // a1 is blocked while a2 is present. If we ask for a hint after
        // removing a2 (simulating the player having already solved that
        // part), the hint must never suggest an arrow that isn't actually
        // escapable right now.
        val original = sampleBoard()
        val afterA2Escaped = MoveValidator.applyMove(original, original.arrows.first { it.id == "a2" })

        val hint = HintEngine.getHint(afterA2Escaped, tier = 4)
        require(hint is Hint.RevealMove)
        assertTrue(MoveValidator.canEscape(afterA2Escaped, hint.arrow))
    }

    @Test
    fun `empty board returns no hint available rather than crashing`() {
        val board = Board(rows = 4, columns = 4, arrows = emptyList())
        val hint = HintEngine.getHint(board, tier = 1)
        assertTrue(hint is Hint.NoHintAvailable)
    }
}
