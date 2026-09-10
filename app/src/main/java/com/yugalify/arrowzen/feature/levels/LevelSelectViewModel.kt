package com.yugalify.arrowzen.feature.levels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yugalify.arrowzen.domain.model.Difficulty
import com.yugalify.arrowzen.domain.model.LevelProgress
import com.yugalify.arrowzen.domain.repository.LevelRepository
import com.yugalify.arrowzen.game.generator.LevelCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class LevelListItem(
    val levelId: String,
    val difficulty: Difficulty,
    val bestStars: Int,
    val isCompleted: Boolean
)

class LevelSelectViewModel(levelRepository: LevelRepository) : ViewModel() {

    private val catalogOrder = LevelCatalog.allLevelIds()
    private val fallback = MutableStateFlow(emptyMap<String, LevelProgress>())

    val groupedLevels: StateFlow<Map<Difficulty, List<LevelListItem>>> =
        combine(levelRepository.observeAllProgress(), fallback) { progressList, _ ->
            val progressById = progressList.associateBy { it.levelId }
            catalogOrder
                .map { id ->
                    val progress = progressById[id]
                    LevelListItem(
                        levelId = id,
                        difficulty = LevelCatalog.difficultyFor(id),
                        bestStars = progress?.bestStars ?: 0,
                        isCompleted = progress?.isCompleted ?: false
                    )
                }
                .groupBy { it.difficulty }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
}
