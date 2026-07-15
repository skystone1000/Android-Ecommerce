package com.skystone1000.shrine.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.skystone1000.shrine.core.model.ThemePreference
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.testing.FakeAuthRepository
import com.skystone1000.shrine.testing.FakeSessionRepository
import com.skystone1000.shrine.testing.FakeSettingsRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose UI tests run on the JVM via Robolectric (so they execute in `testDebugUnitTest`, no
 * device needed). They drive the public `XxxScreen` composables with hand-built ViewModels over
 * the in-memory fakes — exercising the real Compose tree.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ScreenRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginScreen_showsItsPrimaryActions() {
        composeRule.setContent {
            ShrineTheme {
                LoginScreen(
                    onSignedIn = {},
                    onCreateAccount = {},
                    onForgotPassword = {},
                    viewModel = LoginViewModel(FakeAuthRepository(), FakeSessionRepository()),
                )
            }
        }

        composeRule.onNodeWithText("Sign in").assertExists()
        composeRule.onNodeWithText("Create account").assertExists()
        composeRule.onNodeWithText("Skip — browse as guest").assertExists()
    }

    @Test
    fun settingsScreen_rendersAppearanceAndThemeOptions() {
        composeRule.setContent {
            ShrineTheme {
                SettingsScreen(
                    onBack = {},
                    viewModel = SettingsViewModel(FakeSettingsRepository(FakeSettingsState())),
                )
            }
        }

        composeRule.onNodeWithText("Appearance").assertExists()
        composeRule.onNodeWithText("System").assertExists()
        composeRule.onNodeWithText("Dark").assertExists()
    }

    // Local helper so the test reads clearly; default theme is SYSTEM.
    private fun FakeSettingsState() = com.skystone1000.shrine.core.data.SettingsState(theme = ThemePreference.SYSTEM)
}
