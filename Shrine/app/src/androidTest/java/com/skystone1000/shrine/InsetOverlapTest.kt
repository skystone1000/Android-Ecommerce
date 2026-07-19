package com.skystone1000.shrine

import android.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented edge-to-edge / inset checks (plan_9 Phase D / item 17). These run **on a device or
 * emulator** (`connectedDebugAndroidTest`) — Robolectric does not render the system bars, so the
 * JVM screenshot suite ([com.skystone1000.shrine.ui.screens.ScreenScreenshotTest]) cannot reproduce
 * F1/F3. This is the layer that does.
 *
 * It is **not** part of the JVM CI job; run it via the emulator matrix or as a manual pre-release
 * gate (see `.github/workflows/ci.yml`).
 *
 * To extend with a true CTA-overlap assertion: drive the app to a pushed screen (e.g. Product
 * detail), read the navigation-bar inset height from the decor view's `WindowInsets`, fetch the
 * sticky CTA node's `getBoundsInRoot()`, and assert `cta.bottom <= rootHeight - navBarInset` across
 * gesture and 3-button nav modes (and a notch/cutout device profile).
 */
@RunWith(AndroidJUnit4::class)
class InsetOverlapTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun activityOptsIntoEdgeToEdge_systemBarsAreTransparent() {
        // MainActivity.onCreate calls enableEdgeToEdge(), which makes the system bars transparent
        // so the app draws behind them and owns its own insets (Phase A). If this regresses, the
        // bars go opaque and the app stops being edge-to-edge.
        composeRule.runOnUiThread {
            val window = composeRule.activity.window
            assertEquals(Color.TRANSPARENT, window.statusBarColor)
            assertEquals(Color.TRANSPARENT, window.navigationBarColor)
        }
    }

    @Test
    fun appLaunches_withEdgeToEdgeInsetWiring() {
        // Smoke: the real edge-to-edge + per-screen inset wiring composes on a device without
        // crashing (the inset code paths only run under a real WindowInsets dispatch).
        composeRule.onRoot().assertExists()
    }
}
