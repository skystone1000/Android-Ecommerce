@file:OptIn(ExperimentalMaterial3Api::class)

package com.skystone1000.shrine.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/** Small top app bar with optional back and trailing actions. */
@Composable
fun ShrineTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                ShrineIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                )
            }
        },
        actions = { actions() },
    )
}

/** Large (editorial) top app bar for landing screens like Home. */
@Composable
fun ShrineLargeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    LargeTopAppBar(
        modifier = modifier,
        title = { Text(title) },
        actions = { actions() },
    )
}

/** A bottom navigation destination. */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
)

/** Bottom navigation bar (figma: Home · Search · Cart · Saved · Profile). */
@Composable
fun ShrineBottomBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                icon = {
                    if (item.badgeCount > 0) {
                        ShrineBadgedIcon(item.icon, contentDescription = item.label, count = item.badgeCount)
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
            )
        }
    }
}

/** Simple text tab row (figma Order history: All / Active / Delivered). */
@Composable
fun ShrineTabRow(
    titles: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabRow(selectedTabIndex = selectedIndex, modifier = modifier) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                text = { Text(title) },
            )
        }
    }
}
