package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.github.takahirom.roborazzi.captureRoboImage
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.designsystem.theme.ThemeMode
import com.skystone1000.shrine.testing.FakeAuthRepository
import com.skystone1000.shrine.testing.FakeCatalogRepository
import com.skystone1000.shrine.testing.FakeOrderRepository
import com.skystone1000.shrine.testing.FakeSessionRepository
import com.skystone1000.shrine.testing.FakeSettingsRepository
import com.skystone1000.shrine.testing.FakeWishlistRepository
import com.skystone1000.shrine.testing.TestData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Per-screen screenshot tests (plan_9 Phase D / item 16), run on the JVM via Robolectric +
 * Roborazzi — the same harness as the design-system `GalleryScreenshotTest`, applied to the public
 * `XxxScreen` composables over the in-memory fakes. Each screen is captured in **light + dark** to
 * catch layout / padding / gutter regressions. Baselines live in `src/test/screenshots/`.
 *
 * Record/refresh:  `./gradlew :app:testDebugUnitTest -Proborazzi.test.record=true`
 * Verify (CI):     `./gradlew :app:testDebugUnitTest -Proborazzi.test.verify=true`
 *
 * Note: Robolectric does not draw the system bars, so these guard *content* layout, not the
 * edge-to-edge inset behaviour — that is covered by the instrumented `InsetOverlapTest` (item 17).
 * This is a representative archetype set (auth form, settings list, profile, catalog grid);
 * extending it to the remaining screens is mechanical (build the screen's VM from fakes here).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class ScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(name: String, dark: Boolean, content: @Composable () -> Unit) {
        composeRule.setContent {
            ShrineTheme(themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT) {
                Surface {
                    // Fixed bounds so scrollable screens have finite constraints under Roborazzi.
                    Box(Modifier.width(412.dp).height(900.dp)) { content() }
                }
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    // ---- composable builders over fakes ----

    @Composable
    private fun Login() = LoginScreen(
        onSignedIn = {}, onCreateAccount = {}, onForgotPassword = {},
        viewModel = LoginViewModel(FakeAuthRepository(), FakeSessionRepository()),
    )

    @Composable
    private fun Register() = RegisterScreen(
        onRegistered = {}, onBackToSignIn = {},
        viewModel = RegisterViewModel(FakeAuthRepository(), FakeSessionRepository()),
    )

    @Composable
    private fun Settings() = SettingsScreen(
        onBack = {},
        viewModel = SettingsViewModel(FakeSettingsRepository()),
    )

    @Composable
    private fun Profile() = ProfileScreen(
        onOrders = {}, onAddresses = {}, onPayments = {}, onEditProfile = {}, onSettings = {},
        onHelp = {}, onSignIn = {}, onRegister = {}, onSignOut = {},
        viewModel = ProfileViewModel(
            FakeSessionRepository().apply { signedIn(name = "Ava Morgan", email = "ava@shrine.com") },
            FakeOrderRepository(),
            FakeWishlistRepository(),
        ),
    )

    @Composable
    private fun Category() = CategoryScreen(
        onProduct = {}, onBack = {},
        viewModel = CategoryViewModel(
            catalogRepository = FakeCatalogRepository(products = TestData.products, categories = TestData.categories),
            sessionRepository = FakeSessionRepository().apply { signedIn(userId = 1) },
            wishlistRepository = FakeWishlistRepository(),
            savedStateHandle = SavedStateHandle(mapOf("id" to "audio")),
        ),
    )

    // ---- light + dark captures ----

    @Test fun login_light() = capture("login_light", dark = false) { Login() }
    @Test fun login_dark() = capture("login_dark", dark = true) { Login() }

    @Test fun register_light() = capture("register_light", dark = false) { Register() }
    @Test fun register_dark() = capture("register_dark", dark = true) { Register() }

    @Test fun settings_light() = capture("settings_light", dark = false) { Settings() }
    @Test fun settings_dark() = capture("settings_dark", dark = true) { Settings() }

    @Test fun profile_light() = capture("profile_light", dark = false) { Profile() }
    @Test fun profile_dark() = capture("profile_dark", dark = true) { Profile() }

    @Test fun category_light() = capture("category_light", dark = false) { Category() }
    @Test fun category_dark() = capture("category_dark", dark = true) { Category() }
}
