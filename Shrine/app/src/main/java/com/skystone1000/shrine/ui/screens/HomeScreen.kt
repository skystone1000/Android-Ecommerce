@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingBag
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
import com.skystone1000.shrine.core.data.CartRepository
import com.skystone1000.shrine.core.data.CatalogRepository
import com.skystone1000.shrine.core.data.PromotionRepository
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.core.data.SettingsRepository
import com.skystone1000.shrine.core.data.WishlistRepository
import com.skystone1000.shrine.core.model.CategoryEntity
import com.skystone1000.shrine.core.model.Money
import com.skystone1000.shrine.core.model.ProductEntity
import com.skystone1000.shrine.core.model.PromotionEntity
import com.skystone1000.shrine.designsystem.component.CategoryTile
import com.skystone1000.shrine.designsystem.component.HeroBanner
import com.skystone1000.shrine.designsystem.component.LoadingState
import com.skystone1000.shrine.designsystem.component.ProductCard
import com.skystone1000.shrine.designsystem.component.SectionHeader
import com.skystone1000.shrine.designsystem.component.ShrineIconButton
import com.skystone1000.shrine.designsystem.component.ShrineTopBar
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.ui.NO_USER
import com.skystone1000.shrine.ui.categoryIcon
import com.skystone1000.shrine.ui.greeting
import com.skystone1000.shrine.ui.scopeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val greeting: String = "",
    val userName: String = "",
    val promo: PromotionEntity? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val cartCount: Int = 0,
    val largeImagery: Boolean = true,
    val wishlistedIds: Set<Long> = emptySet(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val sessionRepository: SessionRepository,
    private val wishlistRepository: WishlistRepository,
    promotionRepository: PromotionRepository,
    settingsRepository: SettingsRepository,
    cartRepository: CartRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { catalogRepository.ensureSeeded() }
    }

    private val cartCount = sessionRepository.session.flatMapLatest { cartRepository.itemCount(it.scopeId) }
    private val wishlistedIds = sessionRepository.session.flatMapLatest { session ->
        wishlistRepository.wishlist(session.scopeId).map { items -> items.map { it.productId }.toSet() }
    }

    val state: StateFlow<HomeUiState> =
        combine(
            catalogRepository.products(),
            catalogRepository.categories(),
            promotionRepository.promotions(),
            sessionRepository.session,
            settingsRepository.settings,
        ) { products, categories, promos, session, settings ->
            HomeUiState(
                loading = products.isEmpty(),
                greeting = greeting(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)),
                userName = session?.name?.takeIf { it.isNotBlank() } ?: "there",
                promo = promos.firstOrNull(),
                categories = categories,
                products = products,
                largeImagery = settings.largeImagery,
            )
        }
            .combine(cartCount) { ui, count -> ui.copy(cartCount = count) }
            .combine(wishlistedIds) { ui, ids -> ui.copy(wishlistedIds = ids) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setWishlisted(productId: Long, wishlisted: Boolean) {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId() ?: NO_USER
            wishlistRepository.setWishlisted(uid, productId, wishlisted)
        }
    }
}

@Composable
fun HomeScreen(
    onCategory: (String) -> Unit,
    onProduct: (Long) -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    HomeContent(
        state = state,
        onCategory = onCategory,
        onProduct = onProduct,
        onSearch = onSearch,
        onCart = onCart,
        onWishlistToggle = viewModel::setWishlisted,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onCategory: (String) -> Unit,
    onProduct: (Long) -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    onWishlistToggle: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ShrineTopBar(
                title = "Shrine",
                actions = {
                    ShrineIconButton(icon = Icons.Rounded.ShoppingBag, contentDescription = "Cart", onClick = onCart)
                },
            )
        },
    ) { padding ->
        if (state.loading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        val columns = if (state.largeImagery) 2 else 3
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("${state.greeting}, ${state.userName}", style = MaterialTheme.typography.headlineSmall)
                    state.promo?.let { promo ->
                        HeroBanner(
                            eyebrow = promo.eyebrow,
                            title = promo.title,
                            ctaLabel = promo.ctaLabel,
                            onCtaClick = onSearch,
                        )
                    }
                    if (state.categories.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            state.categories.forEach { category ->
                                CategoryTile(
                                    label = category.name,
                                    icon = categoryIcon(category.iconKey),
                                    onClick = { onCategory(category.id) },
                                )
                            }
                        }
                    }
                    SectionHeader(title = "New arrivals", subtitle = "Fresh in this week", onSeeAll = onSearch)
                }
            }
            items(state.products, key = { it.id }) { product ->
                ProductCard(
                    name = product.name,
                    price = Money.format(product.priceCents),
                    onClick = { onProduct(product.id) },
                    rating = product.rating.takeIf { it > 0f },
                    wishlisted = product.id in state.wishlistedIds,
                    onWishlistToggle = { checked -> onWishlistToggle(product.id, checked) },
                    isNew = product.isNew,
                )
            }
        }
    }
}

@Preview(name = "Home")
@Composable
private fun HomePreview() {
    ShrineTheme {
        HomeContent(
            state = HomeUiState(
                loading = false,
                greeting = "Good morning",
                userName = "Ava",
                promo = PreviewData.promotion,
                categories = PreviewData.categories,
                products = PreviewData.products,
                wishlistedIds = setOf(1L),
            ),
            onCategory = {}, onProduct = {}, onSearch = {}, onCart = {}, onWishlistToggle = { _, _ -> },
        )
    }
}
