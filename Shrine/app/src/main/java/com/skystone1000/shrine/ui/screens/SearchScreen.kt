@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skystone1000.shrine.core.data.SearchRepository
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.core.data.WishlistRepository
import com.skystone1000.shrine.core.model.Money
import com.skystone1000.shrine.core.model.ProductEntity
import com.skystone1000.shrine.designsystem.component.EmptyState
import com.skystone1000.shrine.designsystem.component.ProductCard
import com.skystone1000.shrine.designsystem.component.ShrineButton
import com.skystone1000.shrine.designsystem.component.ShrineButtonVariant
import com.skystone1000.shrine.designsystem.component.ShrineFilterChip
import com.skystone1000.shrine.designsystem.component.ShrineIconButton
import com.skystone1000.shrine.designsystem.component.ShrinePriceRangeSlider
import com.skystone1000.shrine.designsystem.component.ShrineSearchField
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.ui.NO_USER
import com.skystone1000.shrine.ui.scopeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Live filters for the search results (figma filter sheet). */
data class SearchFilters(
    val priceRange: ClosedFloatingPointRange<Float> = PRICE_MIN..PRICE_MAX,
    val minRating: Int = 0,
    val sort: SortOption = SortOption.Featured,
) {
    companion object {
        const val PRICE_MIN = 0f
        const val PRICE_MAX = 1500f
    }
}

data class SearchUiState(
    val query: String = "",
    val recent: List<String> = emptyList(),
    val results: List<ProductEntity> = emptyList(),
    val filters: SearchFilters = SearchFilters(),
    val wishlistedIds: Set<Long> = emptySet(),
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val sessionRepository: SessionRepository,
    private val wishlistRepository: WishlistRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filters = MutableStateFlow(SearchFilters())

    private val recent = sessionRepository.session.flatMapLatest { session ->
        searchRepository.recentSearches(session.scopeId).map { list -> list.map { it.query } }
    }
    private val wishlistedIds = sessionRepository.session.flatMapLatest { session ->
        wishlistRepository.wishlist(session.scopeId).map { items -> items.map { it.productId }.toSet() }
    }

    // Results are keyed off a *debounced* query so a DB query doesn't run on every keystroke;
    // distinctUntilChanged collapses repeats (e.g. fast type-then-delete). Filters are not
    // debounced — changing a filter re-queries immediately. The visible query (below) stays the
    // live value so the text field never lags.
    private val results: Flow<List<ProductEntity>> =
        combine(query.debounce(SEARCH_DEBOUNCE_MS).distinctUntilChanged(), filters) { q, f ->
            val raw = if (q.isBlank()) emptyList() else searchRepository.results(q)
            raw
                .filter { it.priceCents in (f.priceRange.start.toInt() * 100)..(f.priceRange.endInclusive.toInt() * 100) }
                .filter { it.rating >= f.minRating }
                .let { products ->
                    when (f.sort) {
                        SortOption.Featured -> products
                        SortOption.PriceLowHigh -> products.sortedBy { it.priceCents }
                        SortOption.PriceHighLow -> products.sortedByDescending { it.priceCents }
                        SortOption.TopRated -> products.sortedByDescending { it.rating }
                    }
                }
        }

    val state: StateFlow<SearchUiState> =
        combine(query, recent, filters, wishlistedIds, results) { q, recentQueries, f, ids, res ->
            SearchUiState(query = q, recent = recentQueries, results = res, filters = f, wishlistedIds = ids)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
    }

    fun onQuery(value: String) { query.value = value }

    fun recordCurrent() {
        val q = query.value
        if (q.isBlank()) return
        viewModelScope.launch {
            val userId = sessionRepository.currentUserId() ?: NO_USER
            searchRepository.recordSearch(userId, q)
        }
    }

    fun selectRecent(value: String) { query.value = value }

    fun clearRecent() {
        viewModelScope.launch {
            val userId = sessionRepository.currentUserId() ?: NO_USER
            searchRepository.clearRecent(userId)
        }
    }

    fun setFilters(value: SearchFilters) { filters.value = value }

    fun resetFilters() { filters.value = SearchFilters() }

    fun setWishlisted(productId: Long, wishlisted: Boolean) {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId() ?: NO_USER
            wishlistRepository.setWishlisted(uid, productId, wishlisted)
        }
    }
}

@Composable
fun SearchScreen(
    onProduct: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchContent(
        state = state,
        onQuery = viewModel::onQuery,
        onSelectRecent = viewModel::selectRecent,
        onClearRecent = viewModel::clearRecent,
        onProduct = { id -> viewModel.recordCurrent(); onProduct(id) },
        onApplyFilters = viewModel::setFilters,
        onResetFilters = viewModel::resetFilters,
        onWishlistToggle = viewModel::setWishlisted,
        modifier = modifier,
    )
}

@Composable
private fun SearchContent(
    state: SearchUiState,
    onQuery: (String) -> Unit,
    onSelectRecent: (String) -> Unit,
    onClearRecent: () -> Unit,
    onProduct: (Long) -> Unit,
    onApplyFilters: (SearchFilters) -> Unit,
    onResetFilters: () -> Unit,
    onWishlistToggle: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding()
                .padding(horizontal = ShrineTheme.spacing.screenGutter, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShrineSearchField(query = state.query, onQueryChange = onQuery, modifier = Modifier.weight(1f))
                ShrineIconButton(Icons.Rounded.Tune, contentDescription = "Filters", onClick = { showFilters = true })
            }

            when {
                state.query.isBlank() -> RecentAndSuggestions(state, onSelectRecent, onClearRecent)
                state.results.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = "No results",
                    subtitle = "Try a different search or adjust your filters.",
                    modifier = Modifier.fillMaxWidth(),
                )
                else -> {
                    Text(
                        "${state.results.size} results",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(state.results, key = { it.id }) { product ->
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
        }
    }

    if (showFilters) {
        FilterSheet(
            filters = state.filters,
            resultCount = state.results.size,
            onApply = { onApplyFilters(it); showFilters = false },
            onReset = onResetFilters,
            onDismiss = { showFilters = false },
        )
    }
}

@Composable
private fun RecentAndSuggestions(
    state: SearchUiState,
    onSelectRecent: (String) -> Unit,
    onClearRecent: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.recent.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("RECENT", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onClearRecent) { Text("Clear") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.recent.take(6).forEach { term ->
                    ShrineFilterChip(selected = false, onClick = { onSelectRecent(term) }, label = term)
                }
            }
        }
        Text("SUGGESTIONS", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Aether", "Buds", "Watch", "Diffuser").forEach { term ->
                ShrineFilterChip(selected = false, onClick = { onSelectRecent(term) }, label = term)
            }
        }
    }
}

@Composable
private fun FilterSheet(
    filters: SearchFilters,
    resultCount: Int,
    onApply: (SearchFilters) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var working by remember { mutableStateOf(filters) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Filters", style = MaterialTheme.typography.titleLarge)

            Text("Price", style = MaterialTheme.typography.titleSmall)
            ShrinePriceRangeSlider(
                value = working.priceRange,
                onValueChange = { working = working.copy(priceRange = it) },
                valueRange = SearchFilters.PRICE_MIN..SearchFilters.PRICE_MAX,
            )

            Text("Rating", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Any", 3 to "3+", 4 to "4+").forEach { (value, label) ->
                    ShrineFilterChip(
                        selected = working.minRating == value,
                        onClick = { working = working.copy(minRating = value) },
                        label = label,
                    )
                }
            }

            Text("Sort", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOption.entries.forEach { option ->
                    ShrineFilterChip(
                        selected = working.sort == option,
                        onClick = { working = working.copy(sort = option) },
                        label = option.label,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShrineButton(
                    text = "Reset",
                    onClick = { working = SearchFilters(); onReset() },
                    variant = ShrineButtonVariant.Outlined,
                    modifier = Modifier.weight(1f),
                )
                ShrineButton(
                    text = "Show $resultCount results",
                    onClick = { onApply(working) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(name = "Search · results")
@Composable
private fun SearchResultsPreview() {
    ShrineTheme {
        SearchContent(
            state = SearchUiState(query = "a", results = PreviewData.products, wishlistedIds = setOf(1L)),
            onQuery = {}, onSelectRecent = {}, onClearRecent = {}, onProduct = {},
            onApplyFilters = {}, onResetFilters = {}, onWishlistToggle = { _, _ -> },
        )
    }
}

@Preview(name = "Search · recent")
@Composable
private fun SearchRecentPreview() {
    ShrineTheme {
        SearchContent(
            state = SearchUiState(query = "", recent = listOf("Aether", "Watch")),
            onQuery = {}, onSelectRecent = {}, onClearRecent = {}, onProduct = {},
            onApplyFilters = {}, onResetFilters = {}, onWishlistToggle = { _, _ -> },
        )
    }
}
