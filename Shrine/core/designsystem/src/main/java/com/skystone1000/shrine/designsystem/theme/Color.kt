package com.skystone1000.shrine.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Raw Shrine color tokens, taken 1:1 from the figma design doc
 * (`figma/Shrine.dc.html` — "Color Tokens — Light/Dark").
 * Premium-minimal retail palette with a single muted terracotta accent.
 */

// ---- Light ----
val ShrineLightBackground = Color(0xFFFAFAFA)
val ShrineLightSurface = Color(0xFFFFFFFF)
val ShrineLightSurfaceVariant = Color(0xFFF2F1EF)
val ShrineLightPrimary = Color(0xFFA65A43) // accent — price, actions, active
val ShrineLightOnPrimary = Color(0xFFFFFFFF)
val ShrineLightSecondary = Color(0xFF6B6B6B)
val ShrineLightTextPrimary = Color(0xFF1A1A1A)
val ShrineLightTextSecondary = Color(0xFF6B6B6B)
val ShrineLightTextDisabled = Color(0xFFB0AFAD)
val ShrineLightOutline = Color(0xFFDAD9D6)
val ShrineLightDivider = Color(0xFFECECEC)
val ShrineLightSuccess = Color(0xFF3F7D5A)
val ShrineLightError = Color(0xFFB23B33)
val ShrineLightWarning = Color(0xFFB07D2E)

// ---- Dark ----
val ShrineDarkBackground = Color(0xFF0E0E0E)
val ShrineDarkSurface = Color(0xFF1A1A1A)
val ShrineDarkSurfaceVariant = Color(0xFF242322)
val ShrineDarkPrimary = Color(0xFFD08C70) // accent re-tuned lighter for dark
val ShrineDarkOnPrimary = Color(0xFF1A1A1A)
val ShrineDarkSecondary = Color(0xFFA6A4A1)
val ShrineDarkTextPrimary = Color(0xFFF2F2F2)
val ShrineDarkTextSecondary = Color(0xFFA6A4A1)
val ShrineDarkTextDisabled = Color(0xFF5E5C5A)
val ShrineDarkOutline = Color(0xFF3A3A3A)
val ShrineDarkDivider = Color(0xFF2C2C2C)
val ShrineDarkSuccess = Color(0xFF6FB68C)
val ShrineDarkError = Color(0xFFE0817A)
val ShrineDarkWarning = Color(0xFFD8B45F)

internal val ShrineLightColorScheme = lightColorScheme(
    primary = ShrineLightPrimary,
    onPrimary = ShrineLightOnPrimary,
    primaryContainer = ShrineLightSurfaceVariant,
    onPrimaryContainer = ShrineLightPrimary,
    secondary = ShrineLightSecondary,
    onSecondary = ShrineLightOnPrimary,
    background = ShrineLightBackground,
    onBackground = ShrineLightTextPrimary,
    surface = ShrineLightSurface,
    onSurface = ShrineLightTextPrimary,
    surfaceVariant = ShrineLightSurfaceVariant,
    onSurfaceVariant = ShrineLightTextSecondary,
    surfaceContainer = ShrineLightSurfaceVariant,
    surfaceContainerHigh = ShrineLightSurfaceVariant,
    surfaceContainerLow = ShrineLightSurface,
    outline = ShrineLightOutline,
    outlineVariant = ShrineLightDivider,
    error = ShrineLightError,
    onError = ShrineLightOnPrimary,
    scrim = Color(0x99000000),
)

internal val ShrineDarkColorScheme = darkColorScheme(
    primary = ShrineDarkPrimary,
    onPrimary = ShrineDarkOnPrimary,
    primaryContainer = ShrineDarkSurfaceVariant,
    onPrimaryContainer = ShrineDarkPrimary,
    secondary = ShrineDarkSecondary,
    onSecondary = ShrineDarkOnPrimary,
    background = ShrineDarkBackground,
    onBackground = ShrineDarkTextPrimary,
    surface = ShrineDarkSurface,
    onSurface = ShrineDarkTextPrimary,
    surfaceVariant = ShrineDarkSurfaceVariant,
    onSurfaceVariant = ShrineDarkTextSecondary,
    surfaceContainer = ShrineDarkSurfaceVariant,
    surfaceContainerHigh = ShrineDarkSurfaceVariant,
    surfaceContainerLow = ShrineDarkSurface,
    outline = ShrineDarkOutline,
    outlineVariant = ShrineDarkDivider,
    error = ShrineDarkError,
    onError = ShrineDarkOnPrimary,
    scrim = Color(0x99000000),
)
