package com.google.codelabs.mdc.kotlin.shrine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionState
import com.google.codelabs.mdc.kotlin.shrine.core.data.SettingsRepository
import com.google.codelabs.mdc.kotlin.shrine.core.model.ThemePreference
import com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ThemeMode
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
 * App-scoped state: exposes the persisted session (drives Splash → Auth/Main routing), the
 * user's theme preference (drives [ShrineTheme]), and sign-out. Per-screen sign-in/registration
 * now lives in the Login/Register ViewModels (Phase 4).
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val sessionState: StateFlow<SessionUiState> = sessionRepository.session
        .map<SessionState?, SessionUiState> { SessionUiState.Resolved(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionUiState.Loading)

    val themeMode: StateFlow<ThemeMode> = settingsRepository.settings
        .map { settings ->
            when (settings.theme) {
                ThemePreference.SYSTEM -> ThemeMode.SYSTEM
                ThemePreference.LIGHT -> ThemeMode.LIGHT
                ThemePreference.DARK -> ThemeMode.DARK
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    fun signOut() = viewModelScope.launch { sessionRepository.signOut() }
}
