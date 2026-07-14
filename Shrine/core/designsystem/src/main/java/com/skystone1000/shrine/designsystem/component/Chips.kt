package com.skystone1000.shrine.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.skystone1000.shrine.designsystem.theme.ShrineTheme

/** Selectable filter chip (figma category / filter rows). */
@Composable
fun ShrineFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        leadingIcon = if (selected) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else null,
    )
}

/** Order status kinds for [ShrineStatusChip] (figma Order history). */
enum class OrderStatusKind(val label: String, val icon: ImageVector) {
    Placed("Placed", Icons.Rounded.Schedule),
    InTransit("In transit", Icons.Rounded.LocalShipping),
    Delivered("Delivered", Icons.Rounded.CheckCircle),
}

/** Status chip whose accent color reflects the order state. */
@Composable
fun ShrineStatusChip(
    status: OrderStatusKind,
    modifier: Modifier = Modifier,
) {
    val color = when (status) {
        OrderStatusKind.Placed -> androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        OrderStatusKind.InTransit -> ShrineTheme.extendedColors.warning
        OrderStatusKind.Delivered -> ShrineTheme.extendedColors.success
    }
    AssistChip(
        onClick = {},
        modifier = modifier,
        label = { Text(status.label) },
        leadingIcon = { Icon(status.icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color) },
        colors = AssistChipDefaults.assistChipColors(labelColor = color),
    )
}
