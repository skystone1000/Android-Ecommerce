package com.skystone1000.shrine.ui

import app.cash.turbine.test
import com.skystone1000.shrine.core.data.SettingsState
import com.skystone1000.shrine.core.model.ThemePreference
import com.skystone1000.shrine.designsystem.theme.ThemeMode
import com.skystone1000.shrine.testing.FakeSessionRepository
import com.skystone1000.shrine.testing.FakeSettingsRepository
import com.skystone1000.shrine.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun themeMode_reflectsSettingsPreference() = runTest(mainRule.dispatcher) {
        val vm = AppViewModel(FakeSessionRepository(), FakeSettingsRepository(SettingsState(theme = ThemePreference.DARK)))
        vm.themeMode.test {
            var mode = awaitItem()
            if (mode == ThemeMode.SYSTEM) mode = awaitItem() // skip the stateIn seed
            assertEquals(ThemeMode.DARK, mode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun sessionState_resolvesAndTracksSignIn() = runTest(mainRule.dispatcher) {
        val session = FakeSessionRepository()
        val vm = AppViewModel(session, FakeSettingsRepository())
        vm.sessionState.test {
            var item = awaitItem()
            if (item is SessionUiState.Loading) item = awaitItem()
            assertEquals(SessionUiState.Resolved(null), item)

            session.signedIn(userId = 7)
            assertTrue((awaitItem() as SessionUiState.Resolved).session?.userId == 7L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signOut_clearsSession() = runTest(mainRule.dispatcher) {
        val session = FakeSessionRepository().apply { signedIn(3) }
        val vm = AppViewModel(session, FakeSettingsRepository())
        vm.signOut()
        assertNull(session.session.first())
    }
}
