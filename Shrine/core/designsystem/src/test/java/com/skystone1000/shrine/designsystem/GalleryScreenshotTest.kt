package com.skystone1000.shrine.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.skystone1000.shrine.designsystem.gallery.ComponentGallery
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.designsystem.theme.ThemeMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot tests for the design-system gallery (the Phase 1 exit gate), in light + dark, run on
 * the JVM via Robolectric + Roborazzi. Baselines live in `src/test/screenshots/`. Record/refresh
 * with `:core:designsystem:testDebugUnitTest -Proborazzi.test.record`; CI runs in verify mode.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class GalleryScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Composable
    private fun Gallery(mode: ThemeMode) {
        ShrineTheme(themeMode = mode) {
            Surface {
                // Bound the height so the gallery's internal verticalScroll has finite constraints
                // (Roborazzi captures the full node otherwise measures with infinite height).
                Box(Modifier.width(412.dp).height(2400.dp)) {
                    ComponentGallery()
                }
            }
        }
    }

    @Test
    fun gallery_light() {
        composeRule.setContent { Gallery(ThemeMode.LIGHT) }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/gallery_light.png")
    }

    @Test
    fun gallery_dark() {
        composeRule.setContent { Gallery(ThemeMode.DARK) }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/gallery_dark.png")
    }
}
