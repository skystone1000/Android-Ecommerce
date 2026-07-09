package com.google.codelabs.mdc.kotlin.shrine.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors that Material 3's [androidx.compose.material3.ColorScheme] does not model
 * (success / warning). Exposed through [LocalShrineExtendedColors] and read via
 * `MaterialTheme`-style accessors in [com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ShrineTheme].
 */
@Immutable
data class ShrineExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
)

internal val LightExtendedColors = ShrineExtendedColors(
    success = ShrineLightSuccess,
    onSuccess = ShrineLightOnPrimary,
    warning = ShrineLightWarning,
    onWarning = ShrineLightOnPrimary,
)

internal val DarkExtendedColors = ShrineExtendedColors(
    success = ShrineDarkSuccess,
    onSuccess = ShrineDarkOnPrimary,
    warning = ShrineDarkWarning,
    onWarning = ShrineDarkOnPrimary,
)

val LocalShrineExtendedColors = staticCompositionLocalOf { LightExtendedColors }
