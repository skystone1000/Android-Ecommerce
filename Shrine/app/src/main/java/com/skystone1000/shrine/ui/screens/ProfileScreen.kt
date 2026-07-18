@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skystone1000.shrine.core.data.OrderRepository
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.core.data.WishlistRepository
import com.skystone1000.shrine.designsystem.component.ShrineAvatar
import com.skystone1000.shrine.designsystem.component.ShrineButton
import com.skystone1000.shrine.designsystem.component.ShrineButtonVariant
import com.skystone1000.shrine.designsystem.component.ShrineDivider
import com.skystone1000.shrine.designsystem.component.ShrineListRow
import com.skystone1000.shrine.designsystem.component.ShrineTopBar
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.ui.initials
import com.skystone1000.shrine.ui.scopeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val isGuest: Boolean = false,
    val orderCount: Int = 0,
    val savedCount: Int = 0,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    orderRepository: OrderRepository,
    wishlistRepository: WishlistRepository,
) : ViewModel() {

    val state: StateFlow<ProfileUiState> =
        sessionRepository.session.flatMapLatest { session ->
            val uid = session.scopeId
            combine(
                orderRepository.orders(uid),
                wishlistRepository.wishlist(uid),
            ) { orders, saved ->
                ProfileUiState(
                    name = session?.name?.takeIf { it.isNotBlank() } ?: "Guest",
                    email = session?.email?.takeIf { it.isNotBlank() } ?: "Browsing as guest",
                    isGuest = session?.isGuest ?: true,
                    orderCount = orders.size,
                    savedCount = saved.size,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}

@Composable
fun ProfileScreen(
    onOrders: () -> Unit,
    onAddresses: () -> Unit,
    onPayments: () -> Unit,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onSignIn: () -> Unit,
    onRegister: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ProfileContent(
        state = state,
        onOrders = onOrders,
        onAddresses = onAddresses,
        onPayments = onPayments,
        onEditProfile = onEditProfile,
        onSettings = onSettings,
        onHelp = onHelp,
        onSignIn = onSignIn,
        onRegister = onRegister,
        onSignOut = onSignOut,
        modifier = modifier,
    )
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onOrders: () -> Unit,
    onAddresses: () -> Unit,
    onPayments: () -> Unit,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onSignIn: () -> Unit,
    onRegister: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "Profile") },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = ShrineTheme.spacing.screenGutter, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShrineAvatar(initials = initials(state.name), size = 64)
                Column {
                    Text(state.name, style = MaterialTheme.typography.titleLarge)
                    Text(state.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat("Orders", state.orderCount)
                Stat("Saved", state.savedCount)
                Stat("Reviews", 0)
            }

            ShrineDivider()

            ShrineListRow(title = "My orders", onClick = onOrders, leadingIcon = Icons.Rounded.Receipt)
            ShrineListRow(title = "Addresses", onClick = onAddresses, leadingIcon = Icons.Rounded.LocationOn)
            ShrineListRow(title = "Payment methods", onClick = onPayments, leadingIcon = Icons.Rounded.CreditCard)
            ShrineListRow(title = "Edit profile", onClick = onEditProfile, leadingIcon = Icons.Rounded.Person)
            ShrineListRow(title = "Settings", onClick = onSettings, leadingIcon = Icons.Rounded.Settings)
            ShrineListRow(title = "Help center", onClick = onHelp, leadingIcon = Icons.Rounded.HelpOutline)

            Spacer(Modifier.height(8.dp))
            if (state.isGuest) {
                // Guests have no account to sign out of — offer sign-in / registration instead.
                ShrineButton(
                    text = "Sign in",
                    onClick = onSignIn,
                    icon = Icons.Rounded.Login,
                    modifier = Modifier.fillMaxWidth(),
                )
                ShrineButton(
                    text = "Create account",
                    onClick = onRegister,
                    variant = ShrineButtonVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                ShrineButton(
                    text = "Sign out",
                    onClick = onSignOut,
                    variant = ShrineButtonVariant.Outlined,
                    icon = Icons.Rounded.Logout,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(name = "Profile · signed in")
@Composable
private fun ProfileSignedInPreview() {
    ShrineTheme {
        ProfileContent(
            state = ProfileUiState(name = "Ava Morgan", email = "ava@shrine.com", isGuest = false, orderCount = 3, savedCount = 5),
            onOrders = {}, onAddresses = {}, onPayments = {}, onEditProfile = {},
            onSettings = {}, onHelp = {}, onSignIn = {}, onRegister = {}, onSignOut = {},
        )
    }
}

@Preview(name = "Profile · guest")
@Composable
private fun ProfileGuestPreview() {
    ShrineTheme {
        ProfileContent(
            state = ProfileUiState(name = "Guest", email = "Browsing as guest", isGuest = true),
            onOrders = {}, onAddresses = {}, onPayments = {}, onEditProfile = {},
            onSettings = {}, onHelp = {}, onSignIn = {}, onRegister = {}, onSignOut = {},
        )
    }
}
