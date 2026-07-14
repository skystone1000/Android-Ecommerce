@file:OptIn(ExperimentalMaterial3Api::class)

package com.skystone1000.shrine.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.skystone1000.shrine.designsystem.theme.ShrineChipShape
import com.skystone1000.shrine.designsystem.theme.ShrineTheme

/** Standard outlined text field with label / helper / error support. */
@Composable
fun ShrineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    val isError = errorText != null
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = (errorText ?: helperText)?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        enabled = enabled,
    )
}

/** Password field with a visibility toggle (figma: `visibility` / `visibility_off`). */
@Composable
fun ShrinePasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    modifier: Modifier = Modifier,
    errorText: String? = null,
) {
    var visible by remember { mutableStateOf(false) }
    val isError = errorText != null
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = errorText?.let { { Text(it) } },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            ShrineIconButton(
                icon = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                contentDescription = if (visible) "Hide password" else "Show password",
                onClick = { visible = !visible },
            )
        },
    )
}

/** Strength levels for [PasswordStrengthMeter]. */
enum class PasswordStrength(val label: String, val fraction: Float) {
    Weak("Weak", 0.33f),
    Medium("Medium", 0.66f),
    Strong("Strong", 1f),
}

/** Small bar + label communicating password strength (figma Register: "Strong"). */
@Composable
fun PasswordStrengthMeter(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val color = when (strength) {
        PasswordStrength.Weak -> ShrineTheme.extendedColors.warning
        PasswordStrength.Medium -> ShrineTheme.extendedColors.warning
        PasswordStrength.Strong -> ShrineTheme.extendedColors.success
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LinearProgressIndicator(
            progress = { strength.fraction },
            modifier = Modifier.weight(1f).height(4.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
        )
        Text(strength.label, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = color)
    }
}

/** Inactive search entry (Home / Category) — a pill with search + optional filter (`tune`) action. */
@Composable
fun ShrineSearchBar(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTuneClick: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = ShrineChipShape,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                placeholder,
                modifier = Modifier.weight(1f),
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            )
            if (onTuneClick != null) {
                ShrineIconButton(Icons.Rounded.Tune, contentDescription = "Filters", onClick = onTuneClick)
            }
        }
    }
}

/** Active search field with a clear action. */
@Composable
fun ShrineSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search Shrine",
    onClear: () -> Unit = { onQueryChange("") },
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = ShrineChipShape,
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                ShrineIconButton(Icons.Rounded.Close, contentDescription = "Clear", onClick = onClear)
            }
        },
    )
}

/** Read-only date field that opens a date picker on tap (figma Edit profile: "Date of birth"). */
@Composable
fun ShrineDateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), color = Color.Transparent) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            enabled = false,
            singleLine = true,
            trailingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
        )
    }
}
