@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skystone1000.shrine.core.data.CartRepository
import com.skystone1000.shrine.core.data.CatalogRepository
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.core.data.WishlistRepository
import com.skystone1000.shrine.core.model.Money
import com.skystone1000.shrine.core.model.ProductEntity
import com.skystone1000.shrine.designsystem.component.EmptyState
import com.skystone1000.shrine.designsystem.component.ProductCard
import com.skystone1000.shrine.designsystem.component.QuickAddButton
import com.skystone1000.shrine.designsystem.component.ShrineTopBar
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.ui.NO_USER
import com.skystone1000.shrine.ui.scopeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WishlistUiState(
    val loading: Boolean = true,
    val products: List<ProductEntity> = emptyList(),
)

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository,
    private val sessionRepository: SessionRepository,
    catalogRepository: CatalogRepository,
) : ViewModel() {

    val state: StateFlow<WishlistUiState> =
        sessionRepository.session.flatMapLatest { session ->
            combine(
                wishlistRepository.wishlist(session.scopeId),
                catalogRepository.products(),
            ) { saved, products ->
                val ids = saved.map { it.productId }.toSet()
                WishlistUiState(loading = false, products = products.filter { it.id in ids })
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WishlistUiState())

    fun remove(productId: Long) {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId() ?: NO_USER
            wishlistRepository.setWishlisted(uid, productId, false)
        }
    }

    fun addToCart(product: ProductEntity) {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId() ?: NO_USER
            cartRepository.add(uid, product, product.variants.firstOrNull()?.label, 1)
        }
    }
}

@Composable
fun WishlistScreen(
    onProduct: (Long) -> Unit,
    onBrowse: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WishlistViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WishlistContent(
        state = state,
        onProduct = onProduct,
        onBrowse = onBrowse,
        onRemove = viewModel::remove,
        onAddToCart = viewModel::addToCart,
        modifier = modifier,
    )
}

@Composable
private fun WishlistContent(
    state: WishlistUiState,
    onProduct: (Long) -> Unit,
    onBrowse: () -> Unit,
    onRemove: (Long) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "Saved (${state.products.size})") },
    ) { padding ->
        when {
            state.loading -> Unit
            state.products.isEmpty() -> EmptyState(
                icon = Icons.Rounded.FavoriteBorder,
                title = "Nothing saved yet",
                subtitle = "Tap the heart on a product to save it here.",
                modifier = Modifier.padding(padding),
                actionLabel = "Browse products",
                onAction = onBrowse,
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = ShrineTheme.spacing.screenGutter, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.products, key = { it.id }) { product ->
                    Box {
                        ProductCard(
                            name = product.name,
                            price = Money.format(product.priceCents),
                            onClick = { onProduct(product.id) },
                            rating = product.rating.takeIf { it > 0f },
                            wishlisted = true,
                            onWishlistToggle = { onRemove(product.id) },
                            isNew = product.isNew,
                            reserveQuickAddSpace = true,
                        )
                        QuickAddButton(
                            onClick = { onAddToCart(product) },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Wishlist")
@Composable
private fun WishlistPreview() {
    ShrineTheme {
        WishlistContent(
            state = WishlistUiState(loading = false, products = PreviewData.products),
            onProduct = {}, onBrowse = {}, onRemove = {}, onAddToCart = {},
        )
    }
}

@Preview(name = "Wishlist · empty")
@Composable
private fun WishlistEmptyPreview() {
    ShrineTheme {
        WishlistContent(
            state = WishlistUiState(loading = false, products = emptyList()),
            onProduct = {}, onBrowse = {}, onRemove = {}, onAddToCart = {},
        )
    }
}
