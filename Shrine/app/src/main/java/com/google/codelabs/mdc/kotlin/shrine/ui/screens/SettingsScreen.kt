@file:OptIn(ExperimentalMaterial3Api::class)

package com.google.codelabs.mdc.kotlin.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.codelabs.mdc.kotlin.shrine.core.data.SettingsRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SettingsState
import com.google.codelabs.mdc.kotlin.shrine.core.model.ThemePreference
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.SegmentOption
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineDivider
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineSegmentedButtons
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineSwitchRow
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineTopBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ShrineTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<SettingsState> =
        settingsRepository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun setTheme(theme: ThemePreference) = viewModelScope.launch { settingsRepository.setTheme(theme) }
    fun setLargeImagery(enabled: Boolean) = viewModelScope.launch { settingsRepository.setLargeImagery(enabled) }
    fun setOrderUpdates(enabled: Boolean) = viewModelScope.launch { settingsRepository.setOrderUpdates(enabled) }
    fun setPromotions(enabled: Boolean) = viewModelScope.launch { settingsRepository.setPromotions(enabled) }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    SettingsContent(
        state = state,
        onBack = onBack,
        onTheme = viewModel::setTheme,
        onLargeImagery = viewModel::setLargeImagery,
        onOrderUpdates = viewModel::setOrderUpdates,
        onPromotions = viewModel::setPromotions,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    onBack: () -> Unit,
    onTheme: (ThemePreference) -> Unit,
    onLargeImagery: (Boolean) -> Unit,
    onOrderUpdates: (Boolean) -> Unit,
    onPromotions: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "Settings", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleSmall)
            ShrineSegmentedButtons(
                options = listOf(
                    SegmentOption("System", Icons.Rounded.SettingsBrightness),
                    SegmentOption("Light", Icons.Rounded.LightMode),
                    SegmentOption("Dark", Icons.Rounded.DarkMode),
                ),
                selectedIndex = state.theme.ordinal,
                onSelect = { onTheme(ThemePreference.entries[it]) },
            )
            ShrineSwitchRow(
                checked = state.largeImagery,
                onCheckedChange = onLargeImagery,
                title = "Large imagery",
                subtitle = "Show a roomier 2-column product grid",
            )

            ShrineDivider()
            Text("Notifications", style = MaterialTheme.typography.titleSmall)
            ShrineSwitchRow(
                checked = state.orderUpdates,
                onCheckedChange = onOrderUpdates,
                title = "Order updates",
                subtitle = "Shipping and delivery alerts",
            )
            ShrineSwitchRow(
                checked = state.promotions,
                onCheckedChange = onPromotions,
                title = "Promotions",
                subtitle = "Sales and new arrivals",
            )

            ShrineDivider()
            Text("About", style = MaterialTheme.typography.titleSmall)
            Text("Shrine · version 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Terms & Privacy Policy", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(name = "Settings")
@Composable
private fun SettingsPreview() {
    ShrineTheme {
        SettingsContent(
            state = SettingsState(theme = ThemePreference.SYSTEM, largeImagery = true, orderUpdates = true, promotions = false),
            onBack = {}, onTheme = {}, onLargeImagery = {}, onOrderUpdates = {}, onPromotions = {},
        )
    }
}
