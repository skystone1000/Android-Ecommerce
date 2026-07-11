@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.google.codelabs.mdc.kotlin.shrine.ui.screens

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
import androidx.compose.material.icons.rounded.SwapVert
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.codelabs.mdc.kotlin.shrine.core.data.CatalogRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.WishlistRepository
import com.google.codelabs.mdc.kotlin.shrine.core.model.Money
import com.google.codelabs.mdc.kotlin.shrine.core.model.ProductEntity
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.LoadingState
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ProductCard
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineFilterChip
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineIconButton
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineTopBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ShrineTheme
import com.google.codelabs.mdc.kotlin.shrine.ui.NO_USER
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Category
import com.google.codelabs.mdc.kotlin.shrine.ui.scopeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Sort options (figma Category swap_vert). */
enum class SortOption(val label: String) {
    Featured("Featured"),
    PriceLowHigh("Price ↑"),
    PriceHighLow("Price ↓"),
    TopRated("Top rated"),
}

data class CategoryUiState(
    val loading: Boolean = true,
    val categoryName: String = "",
    val products: List<ProductEntity> = emptyList(),
    val sort: SortOption = SortOption.Featured,
    val wishlistedIds: Set<Long> = emptySet(),
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    catalogRepository: CatalogRepository,
    private val sessionRepository: SessionRepository,
    private val wishlistRepository: WishlistRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val categoryId: String = savedStateHandle.toRoute<Category>().id
    private val sort = MutableStateFlow(SortOption.Featured)

    private val wishlistedIds = sessionRepository.session.flatMapLatest { session ->
        wishlistRepository.wishlist(session.scopeId).map { items -> items.map { it.productId }.toSet() }
    }

    val state: StateFlow<CategoryUiState> =
        combine(
            catalogRepository.productsByCategory(categoryId),
            catalogRepository.categories(),
            sort,
            wishlistedIds,
        ) { products, categories, sortOption, ids ->
            val sorted = when (sortOption) {
                SortOption.Featured -> products
                SortOption.PriceLowHigh -> products.sortedBy { it.priceCents }
                SortOption.PriceHighLow -> products.sortedByDescending { it.priceCents }
                SortOption.TopRated -> products.sortedByDescending { it.rating }
            }
            CategoryUiState(
                loading = false,
                categoryName = categories.firstOrNull { it.id == categoryId }?.name
                    ?: categoryId.replaceFirstChar { it.uppercase() },
                products = sorted,
                sort = sortOption,
                wishlistedIds = ids,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryUiState())

    fun cycleSort() {
        sort.value = SortOption.entries[(sort.value.ordinal + 1) % SortOption.entries.size]
    }

    fun setSort(option: SortOption) { sort.value = option }

    fun setWishlisted(productId: Long, wishlisted: Boolean) {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId() ?: NO_USER
            wishlistRepository.setWishlisted(uid, productId, wishlisted)
        }
    }
}

@Composable
fun CategoryScreen(
    onProduct: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    CategoryContent(
        state = state,
        onProduct = onProduct,
        onBack = onBack,
        onSort = viewModel::setSort,
        onCycleSort = viewModel::cycleSort,
        onWishlistToggle = viewModel::setWishlisted,
        modifier = modifier,
    )
}

@Composable
private fun CategoryContent(
    state: CategoryUiState,
    onProduct: (Long) -> Unit,
    onBack: () -> Unit,
    onSort: (SortOption) -> Unit,
    onCycleSort: () -> Unit,
    onWishlistToggle: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ShrineTopBar(
                title = state.categoryName,
                onBack = onBack,
                actions = {
                    ShrineIconButton(Icons.Rounded.SwapVert, contentDescription = "Sort", onClick = onCycleSort)
                },
            )
        },
    ) { padding ->
        if (state.loading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SortOption.entries.forEach { option ->
                            ShrineFilterChip(
                                selected = state.sort == option,
                                onClick = { onSort(option) },
                                label = option.label,
                            )
                        }
                    }
                    Text(
                        "${state.products.size} results",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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

@Preview(name = "Category")
@Composable
private fun CategoryPreview() {
    ShrineTheme {
        CategoryContent(
            state = CategoryUiState(
                loading = false,
                categoryName = "Audio",
                products = PreviewData.products,
                wishlistedIds = setOf(2L),
            ),
            onProduct = {}, onBack = {}, onSort = {}, onCycleSort = {}, onWishlistToggle = { _, _ -> },
        )
    }
}
