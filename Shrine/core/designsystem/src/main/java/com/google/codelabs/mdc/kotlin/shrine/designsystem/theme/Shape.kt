package com.google.codelabs.mdc.kotlin.shrine.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Soft, consistent radii from the figma "Shape" foundation:
 * card 16 · button 12 · sheet 24 · chip full.
 */
val ShrineShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),   // buttons
    medium = RoundedCornerShape(16.dp),  // cards
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp), // sheets
)

/** Pill / fully-rounded shape for chips and the search bar. */
val ShrineChipShape = RoundedCornerShape(percent = 50)
