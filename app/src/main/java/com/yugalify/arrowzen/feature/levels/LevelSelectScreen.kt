package com.yugalify.arrowzen.feature.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yugalify.arrowzen.ArrowZenApplication
import com.yugalify.arrowzen.domain.model.Difficulty

@Composable
fun LevelSelectScreen(onLevelSelected: (levelId: String) -> Unit, onBackClicked: () -> Unit) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    val factory = viewModelFactory {
        initializer { LevelSelectViewModel(application.container.levelRepository) }
    }
    val viewModel: LevelSelectViewModel = viewModel(factory = factory)
    val grouped by viewModel.groupedLevels.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Classic") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) { Text("←") }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (difficulty in Difficulty.entries) {
                val levels = grouped[difficulty].orEmpty()
                if (levels.isEmpty()) continue

                item {
                    Text(
                        text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                items(levels, key = { it.levelId }) { level ->
                    LevelRow(level = level, onClick = { onLevelSelected(level.levelId) })
                }
            }
        }
    }
}

@Composable
private fun LevelRow(level: LevelListItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = level.levelId, style = MaterialTheme.typography.titleLarge)
        Text(
            text = if (level.isCompleted) "${"⭐".repeat(level.bestStars)} completed" else "Not yet completed",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
