@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.google.codelabs.mdc.kotlin.shrine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.codelabs.mdc.kotlin.shrine.core.data.CartRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.CatalogRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.WishlistRepository
import com.google.codelabs.mdc.kotlin.shrine.core.model.Money
import com.google.codelabs.mdc.kotlin.shrine.core.model.ProductEntity
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.LoadingState
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.PriceText
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ProductImagePlaceholder
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.QuantityStepper
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.RatingBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.SectionHeader
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineButton
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineFilterChip
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineIconButton
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineTopBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ShrineTheme
import com.google.codelabs.mdc.kotlin.shrine.ui.NO_USER
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.ProductDetail
import com.google.codelabs.mdc.kotlin.shrine.ui.scopeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val loading: Boolean = true,
    val product: ProductEntity? = null,
    val wishlisted: Boolean = false,
    val related: List<ProductEntity> = emptyList(),
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = savedStateHandle.toRoute<ProductDetail>().id

    private val wishlisted = sessionRepository.session.flatMapLatest { session ->
        wishlistRepository.isWishlisted(session.scopeId, productId)
    }

    val state: StateFlow<ProductDetailUiState> =
        combine(
            catalogRepository.product(productId),
            catalogRepository.products(),
            wishlisted,
        ) { product, allProducts, isWishlisted ->
            ProductDetailUiState(
                loading = product == null,
                product = product,
                wishlisted = isWishlisted,
                related = allProducts.filter { it.categoryId == product?.categoryId && it.id != productId }.take(8),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductDetailUiState())

    fun addToCart(variant: String?, quantity: Int, onDone: () -> Unit) {
        val product = state.value.product ?: return
        viewModelScope.launch {
            val userId = sessionRepository.currentUserId() ?: NO_USER
            cartRepository.add(userId, product, variant, quantity)
            onDone()
        }
    }

    fun toggleWishlist() {
        val product = state.value.product ?: return
        val target = !state.value.wishlisted
        viewModelScope.launch {
            val userId = sessionRepository.currentUserId() ?: NO_USER
            wishlistRepository.setWishlisted(userId, product.id, target)
        }
    }
}

@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onProduct: (Long) -> Unit,
    onAddedToCart: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ProductDetailContent(
        state = state,
        onBack = onBack,
        onProduct = onProduct,
        onToggleWishlist = viewModel::toggleWishlist,
        onAddToCart = { variant, qty -> viewModel.addToCart(variant, qty, onAddedToCart) },
        modifier = modifier,
    )
}

@Composable
private fun ProductDetailContent(
    state: ProductDetailUiState,
    onBack: () -> Unit,
    onProduct: (Long) -> Unit,
    onToggleWishlist: () -> Unit,
    onAddToCart: (variant: String?, quantity: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = state.product
    var selectedVariant by remember { mutableIntStateOf(0) }
    var quantity by remember { mutableIntStateOf(1) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ShrineTopBar(
                title = product?.name ?: "Product",
                onBack = onBack,
                actions = {
                    ShrineIconButton(Icons.Rounded.Share, contentDescription = "Share", onClick = {})
                    ShrineIconButton(
                        icon = if (state.wishlisted) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Wishlist",
                        onClick = onToggleWishlist,
                    )
                },
            )
        },
        bottomBar = {
            if (product != null) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        QuantityStepper(
                            quantity = quantity,
                            onDecrement = { if (quantity > 1) quantity-- },
                            onIncrement = { quantity++ },
                        )
                        ShrineButton(
                            text = "Add to cart · ${Money.format(product.priceCents * quantity)}",
                            onClick = { onAddToCart(product.variants.getOrNull(selectedVariant)?.label, quantity) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (state.loading || product == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ImagePager(pageCount = product.imageUrls.size.coerceAtLeast(1))
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(product.name, style = MaterialTheme.typography.headlineSmall)
                PriceText(
                    price = Money.format(product.priceCents),
                    originalPrice = product.originalPriceCents?.let { Money.format(it) },
                )
                if (product.rating > 0f) RatingBar(rating = product.rating, reviewCount = product.reviewCount)

                if (product.variants.isNotEmpty()) {
                    Text("Options", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        product.variants.forEachIndexed { index, variant ->
                            ShrineFilterChip(
                                selected = index == selectedVariant,
                                onClick = { selectedVariant = index },
                                label = variant.label,
                            )
                        }
                    }
                }

                Text(product.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (state.related.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader(title = "You may also like")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.related, key = { it.id }) { related ->
                            Column(
                                modifier = Modifier.size(width = 140.dp, height = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Surface(
                                    onClick = { onProduct(related.id) },
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                ) {}
                                Text(related.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                PriceText(price = Money.format(related.priceCents))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ImagePager(pageCount: Int) {
    val pagerState = rememberPagerState(pageCount = { pageCount })
    Box {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            ProductImagePlaceholder()
        }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(pageCount) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (active) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        ),
                )
            }
        }
    }
}

@Preview(name = "Product detail")
@Composable
private fun ProductDetailPreview() {
    ShrineTheme {
        ProductDetailContent(
            state = ProductDetailUiState(
                loading = false,
                product = PreviewData.product,
                wishlisted = true,
                related = PreviewData.products.drop(1),
            ),
            onBack = {}, onProduct = {}, onToggleWishlist = {}, onAddToCart = { _, _ -> },
        )
    }
}
