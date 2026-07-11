package com.google.codelabs.mdc.kotlin.shrine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Splash gate state: are we still resolving the persisted session, or is it known? */
sealed interface SessionUiState {
    data object Loading : SessionUiState
    data class Resolved(val session: SessionState?) : SessionUiState
}

/**
 * App-scoped state for the navigation skeleton: exposes the persisted session (drives the
 * Splash → Auth/Main routing) and the sign-in / guest / sign-out actions.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val sessionState: StateFlow<SessionUiState> = sessionRepository.session
        .map<SessionState?, SessionUiState> { SessionUiState.Resolved(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionUiState.Loading)

    /** Demo sign-in for the skeleton (real credential auth lands with the Login screen in Phase 4). */
    fun demoSignIn() = viewModelScope.launch {
        sessionRepository.signIn(userId = 1L, name = "Ava Morgan", email = "ava@shrine.com", phone = null)
    }

    fun continueAsGuest() = viewModelScope.launch { sessionRepository.continueAsGuest() }

    fun signOut() = viewModelScope.launch { sessionRepository.signOut() }
}
