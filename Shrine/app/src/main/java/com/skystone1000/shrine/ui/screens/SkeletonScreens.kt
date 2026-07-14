package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skystone1000.shrine.core.data.SessionState
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.ui.AppViewModel
import com.skystone1000.shrine.ui.SessionUiState

/**
 * Splash gate: shows the brand mark while the persisted session resolves, then routes to the
 * main app (signed in / guest) or the login flow via [onResolved].
 */
@Composable
fun SplashScreen(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier,
    onResolved: (session: SessionState?) -> Unit,
) {
    val state by appViewModel.sessionState.collectAsState()
    if (state is SessionUiState.Resolved) {
        val session = (state as SessionUiState.Resolved).session
        // Route once the session is known.
        LaunchedEffect(Unit) { onResolved(session) }
    }
    SplashContent(modifier = modifier)
}

@Composable
private fun SplashContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("SHRINE", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            CircularProgressIndicator()
        }
    }
}

@Preview(name = "Splash")
@Composable
private fun SplashPreview() {
    ShrineTheme { SplashContent() }
}
