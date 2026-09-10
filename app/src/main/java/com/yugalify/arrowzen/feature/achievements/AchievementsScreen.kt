package com.yugalify.arrowzen.feature.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yugalify.arrowzen.ArrowZenApplication
import com.yugalify.arrowzen.core.util.ShareUtil
import com.yugalify.arrowzen.domain.model.Achievement

/** Achievements screen (Section 19): local, extensible achievement list with progress. */
@Composable
fun AchievementsScreen(onBackClicked: () -> Unit) {
    val application = LocalContext.current.applicationContext as ArrowZenApplication
    val context = LocalContext.current
    val achievements by application.container.achievementRepository.observeAll()
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = { IconButton(onClick = onBackClicked) { Text("←") } }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements, key = { it.id }) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    onShareClicked = { ShareUtil.shareText(context, ShareUtil.achievementShareText(achievement.title)) }
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, onShareClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = if (achievement.isUnlocked) "🏆 ${achievement.title}" else achievement.title,
            style = MaterialTheme.typography.titleLarge
        )
        Text(text = achievement.description, style = MaterialTheme.typography.bodyMedium)
        if (!achievement.isUnlocked) {
            LinearProgressIndicator(
                progress = { achievement.progress.toFloat() / achievement.target.toFloat() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Text(
                text = "${achievement.progress} / ${achievement.target}",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onShareClicked) { Text("Share") }
            }
        }
    }
}
