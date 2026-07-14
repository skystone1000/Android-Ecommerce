package com.skystone1000.shrine.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Visual variants for [ShrineButton], mapping to the figma button row (Filled / Tonal / Outlined / Text). */
enum class ShrineButtonVariant { Filled, Tonal, Outlined, Text }

/**
 * Primary action button. Supports a [loading] state that swaps the label for a spinner and
 * disables interaction, and an optional leading [icon].
 */
@Composable
fun ShrineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ShrineButtonVariant = ShrineButtonVariant.Filled,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text)
            }
        }
    }
    val realEnabled = enabled && !loading
    when (variant) {
        ShrineButtonVariant.Filled ->
            Button(onClick = onClick, modifier = modifier, enabled = realEnabled) { content() }
        ShrineButtonVariant.Tonal ->
            FilledTonalButton(onClick = onClick, modifier = modifier, enabled = realEnabled) { content() }
        ShrineButtonVariant.Outlined ->
            OutlinedButton(onClick = onClick, modifier = modifier, enabled = realEnabled) { content() }
        ShrineButtonVariant.Text ->
            TextButton(onClick = onClick, modifier = modifier, enabled = realEnabled) { content() }
    }
}

/** Icon-only button (e.g. share / back / more). */
@Composable
fun ShrineIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(icon, contentDescription = contentDescription)
    }
}
