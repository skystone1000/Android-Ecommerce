package com.skystone1000.shrine.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing + elevation tokens from the figma "Foundations" sheet.
 * Spacing scale is 4dp-based: 4 / 8 / 12 / 16 / 24 / 32 / 48.
 * Elevation is "tonal first": flat (0) with subtle tonal lifts.
 */
@Immutable
data class ShrineSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,   // standard card / component padding (figma "padding:16px")
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    /**
     * Horizontal page gutter — the left/right edge padding of a screen's content.
     * Matches the figma dominant screen gutter (`padding:0 24px`). Single source of truth so the
     * whole app's gutter can be retuned here (plan_9 Phase B / F6). Card-internal padding stays [md].
     */
    val screenGutter: Dp = 24.dp,
)

@Immutable
data class ShrineElevation(
    val flat: Dp = 0.dp,
    val tonal: Dp = 1.dp,
    val sheet: Dp = 3.dp,
)

val LocalShrineSpacing = staticCompositionLocalOf { ShrineSpacing() }
val LocalShrineElevation = staticCompositionLocalOf { ShrineElevation() }
