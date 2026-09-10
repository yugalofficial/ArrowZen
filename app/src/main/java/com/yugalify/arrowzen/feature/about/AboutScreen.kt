package com.yugalify.arrowzen.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yugalify.arrowzen.core.constants.AppConstants

/**
 * About screen (Section 39). Privacy policy / support email / website are
 * intentionally left as clearly-marked configuration placeholders rather
 * than invented URLs -- see [AppConstants] and docs/PLAY_STORE.md for where
 * to fill these in before a real store submission.
 */
@Composable
fun AboutScreen(onBackClicked: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = { IconButton(onClick = onBackClicked) { Text("←") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(AppConstants.APP_FULL_NAME, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(AppConstants.APP_TAGLINE, style = MaterialTheme.typography.bodyLarge)
            Text(
                "A relaxing logic puzzle game designed to challenge your mind and reward every smart move.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium)
            Text(AppConstants.DEVELOPER_CREDIT, style = MaterialTheme.typography.bodyLarge)
            Text(AppConstants.COPYRIGHT_NOTICE, style = MaterialTheme.typography.bodyMedium)

            Text(
                text = "Privacy Policy: ${AppConstants.PRIVACY_POLICY_URL.ifBlank { "[add in AppConstants before publishing]" }}\n" +
                    "Support: ${AppConstants.SUPPORT_EMAIL.ifBlank { "[add in AppConstants before publishing]" }}\n" +
                    "Website: ${AppConstants.WEBSITE_URL.ifBlank { "[add in AppConstants before publishing]" }}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
