package com.skystone1000.shrine.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/** User-controllable theme preference (Settings screen). Brand palette always wins — no dynamic color. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Root Shrine theme. Wraps [MaterialTheme] with the brand color schemes, typography and shapes,
 * and provides the extended (success/warning) colors, spacing and elevation tokens.
 *
 * Dynamic color is intentionally **not** supported — the Shrine brand palette is the identity.
 * Dark mode follows the system by default and can be overridden via [themeMode].
 */
@Composable
fun ShrineTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) ShrineDarkColorScheme else ShrineLightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalShrineExtendedColors provides extendedColors,
        LocalShrineSpacing provides remember { ShrineSpacing() },
        LocalShrineElevation provides remember { ShrineElevation() },
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ShrineTypography,
            shapes = ShrineShapes,
            content = content,
        )
    }
}

/** Convenience accessors for the Shrine-specific tokens, mirroring `MaterialTheme.*`. */
object ShrineTheme {
    val extendedColors: ShrineExtendedColors
        @Composable @ReadOnlyComposable get() = LocalShrineExtendedColors.current
    val spacing: ShrineSpacing
        @Composable @ReadOnlyComposable get() = LocalShrineSpacing.current
    val elevation: ShrineElevation
        @Composable @ReadOnlyComposable get() = LocalShrineElevation.current
}
