@file:OptIn(ExperimentalMaterial3Api::class)

package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.skystone1000.shrine.designsystem.component.EmptyState
import com.skystone1000.shrine.designsystem.component.ShrineListRow
import com.skystone1000.shrine.designsystem.component.ShrineTopBar
import com.skystone1000.shrine.designsystem.theme.ShrineTheme

/** Static help-center placeholder (no backend in this demo). */
@Composable
fun HelpCenterScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "Help center", onBack = onBack) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.Top) {
            EmptyState(
                icon = Icons.Rounded.HelpOutline,
                title = "How can we help?",
                subtitle = "These topics are placeholders — there is no backend in this demo.",
            )
            ShrineListRow(title = "Track my order", onClick = {})
            ShrineListRow(title = "Returns & refunds", onClick = {})
            ShrineListRow(title = "Shipping information", onClick = {})
            ShrineListRow(title = "Contact support", onClick = {})
        }
    }
}

@Preview(name = "Help center")
@Composable
private fun HelpCenterPreview() {
    ShrineTheme { HelpCenterScreen(onBack = {}) }
}
