package com.google.codelabs.mdc.kotlin.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionState
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineButton
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineButtonVariant
import com.google.codelabs.mdc.kotlin.shrine.ui.AppViewModel
import com.google.codelabs.mdc.kotlin.shrine.ui.SessionUiState

/** A labelled action used by [StubScreen]. */
data class StubAction(val label: String, val variant: ShrineButtonVariant = ShrineButtonVariant.Tonal, val onClick: () -> Unit)

/**
 * Generic placeholder screen for the Phase 3 navigation skeleton: a title and buttons that walk
 * the graph. Real screens replace these one-by-one in Phase 4.
 */
@Composable
fun StubScreen(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: List<StubAction> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(8.dp))
        actions.forEach { action ->
            ShrineButton(
                text = action.label,
                onClick = action.onClick,
                variant = action.variant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

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
        androidx.compose.runtime.LaunchedEffect(Unit) { onResolved(session) }
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("SHRINE", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            CircularProgressIndicator()
        }
    }
}
