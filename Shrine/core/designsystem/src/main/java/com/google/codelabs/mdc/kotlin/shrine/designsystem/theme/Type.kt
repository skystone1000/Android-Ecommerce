package com.google.codelabs.mdc.kotlin.shrine.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale from the figma "Type Scale" sheet (Plus Jakarta Sans).
 *
 * NOTE: Plus Jakarta Sans is not yet bundled; we fall back to [FontFamily.SansSerif] so the
 * module builds offline. Sizes / weights / line-heights / tabular-figures match the design, so
 * swapping in the real font later only changes [ShrineFontFamily].
 */
val ShrineFontFamily: FontFamily = FontFamily.SansSerif

val ShrineTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ShrineFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = ShrineFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = ShrineFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ShrineFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = ShrineFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ShrineFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = ShrineFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = ShrineFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
)

/** Price text style — uses tabular figures so prices align (figma "Price · tabular 18 / 700 · tnum"). */
val ShrinePriceTextStyle = TextStyle(
    fontFamily = ShrineFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    fontFeatureSettings = "tnum",
)
