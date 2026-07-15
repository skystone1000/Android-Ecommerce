package com.skystone1000.shrine.ui.screens

import app.cash.turbine.test
import com.skystone1000.shrine.core.model.ThemePreference
import com.skystone1000.shrine.testing.FakeSettingsRepository
import com.skystone1000.shrine.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun setTheme_andSwitches_persistIntoState() = runTest(mainRule.dispatcher) {
        val vm = SettingsViewModel(FakeSettingsRepository())
        vm.state.test {
            var state = awaitItem()

            vm.setTheme(ThemePreference.DARK)
            while (state.theme != ThemePreference.DARK) state = awaitItem()
            assertEquals(ThemePreference.DARK, state.theme)

            vm.setPromotions(true)
            while (!state.promotions) state = awaitItem()
            assertTrue(state.promotions)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
