@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.google.codelabs.mdc.kotlin.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.google.codelabs.mdc.kotlin.shrine.core.data.CartRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.subtotalCents
import com.google.codelabs.mdc.kotlin.shrine.core.model.CartItemEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.Money
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.EmptyState
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.PriceText
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ProductImagePlaceholder
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.QuantityStepper
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineButton
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineDivider
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineIconButton
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineTopBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ShrineTheme
import com.google.codelabs.mdc.kotlin.shrine.ui.scopeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val loading: Boolean = true,
    val items: List<CartItemEntity> = emptyList(),
    val subtotalCents: Int = 0,
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    sessionRepository: SessionRepository,
) : ViewModel() {

    val state: StateFlow<CartUiState> =
        sessionRepository.session
            .flatMapLatest { session -> cartRepository.cart(session.scopeId) }
            .map { items -> CartUiState(loading = false, items = items, subtotalCents = items.subtotalCents()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun setQuantity(itemId: Long, quantity: Int) {
        viewModelScope.launch { cartRepository.setQuantity(itemId, quantity) }
    }

    fun remove(itemId: Long) {
        viewModelScope.launch { cartRepository.remove(itemId) }
    }
}

@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    onBrowse: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    CartContent(
        state = state,
        onCheckout = onCheckout,
        onBrowse = onBrowse,
        onSetQuantity = viewModel::setQuantity,
        onRemove = viewModel::remove,
        modifier = modifier,
    )
}

@Composable
private fun CartContent(
    state: CartUiState,
    onCheckout: () -> Unit,
    onBrowse: () -> Unit,
    onSetQuantity: (Long, Int) -> Unit,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "Cart") },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("Subtotal", Money.format(state.subtotalCents))
                        SummaryRow("Shipping", "Free")
                        ShrineDivider()
                        SummaryRow("Total", Money.format(state.subtotalCents), emphasize = true)
                        ShrineButton(
                            text = "Checkout · ${Money.format(state.subtotalCents)}",
                            onClick = onCheckout,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.loading -> Unit
            state.items.isEmpty() -> EmptyState(
                icon = Icons.Rounded.ShoppingCart,
                title = "Your cart is empty",
                subtitle = "Browse the catalog and add something you love.",
                modifier = Modifier.padding(padding),
                actionLabel = "Start shopping",
                onAction = onBrowse,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    CartRow(
                        item = item,
                        onIncrement = { onSetQuantity(item.id, item.quantity + 1) },
                        onDecrement = { onSetQuantity(item.id, item.quantity - 1) },
                        onRemove = { onRemove(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CartRow(
    item: CartItemEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(80.dp),
        ) { Box(modifier = Modifier.aspectRatio(1f)) { ProductImagePlaceholder() } }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            item.selectedVariant?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PriceText(price = Money.format(item.priceCents))
            QuantityStepper(quantity = item.quantity, onDecrement = onDecrement, onIncrement = onIncrement, minQuantity = 1)
        }
        ShrineIconButton(Icons.Rounded.Delete, contentDescription = "Remove", onClick = onRemove)
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasize: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        )
        if (emphasize) PriceText(price = value) else Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(name = "Cart")
@Composable
private fun CartPreview() {
    ShrineTheme {
        CartContent(
            state = CartUiState(loading = false, items = PreviewData.cartItems, subtotalCents = 179_700),
            onCheckout = {}, onBrowse = {}, onSetQuantity = { _, _ -> }, onRemove = {},
        )
    }
}

@Preview(name = "Cart · empty")
@Composable
private fun CartEmptyPreview() {
    ShrineTheme {
        CartContent(
            state = CartUiState(loading = false, items = emptyList(), subtotalCents = 0),
            onCheckout = {}, onBrowse = {}, onSetQuantity = { _, _ -> }, onRemove = {},
        )
    }
}
