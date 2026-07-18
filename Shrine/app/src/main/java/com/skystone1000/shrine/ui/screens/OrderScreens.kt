@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.skystone1000.shrine.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skystone1000.shrine.core.data.OrderRepository
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.core.model.Money
import com.skystone1000.shrine.core.model.OrderStatus
import com.skystone1000.shrine.core.model.OrderWithLines
import com.skystone1000.shrine.designsystem.component.EmptyState
import com.skystone1000.shrine.designsystem.component.OrderStatusKind
import com.skystone1000.shrine.designsystem.component.PriceText
import com.skystone1000.shrine.designsystem.component.ProductImagePlaceholder
import com.skystone1000.shrine.designsystem.component.ShrineButton
import com.skystone1000.shrine.designsystem.component.ShrineButtonVariant
import com.skystone1000.shrine.designsystem.component.ShrineDivider
import com.skystone1000.shrine.designsystem.component.ShrineStatusChip
import com.skystone1000.shrine.designsystem.component.ShrineTabRow
import com.skystone1000.shrine.designsystem.component.ShrineTopBar
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.ui.navigation.OrderDetail
import com.skystone1000.shrine.ui.navigation.OrderPlaced
import com.skystone1000.shrine.ui.scopeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Maps the domain [OrderStatus] to the design-system [OrderStatusKind] (chip styling). */
fun OrderStatus.toKind(): OrderStatusKind = when (this) {
    OrderStatus.PLACED -> OrderStatusKind.Placed
    OrderStatus.IN_TRANSIT -> OrderStatusKind.InTransit
    OrderStatus.DELIVERED -> OrderStatusKind.Delivered
}

// ---------------------------------------------------------------------------
// Order placed (confirmation)
// ---------------------------------------------------------------------------

@HiltViewModel
class OrderPlacedViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val orderId: Long = savedStateHandle.toRoute<OrderPlaced>().orderId
    private val _order = MutableStateFlow<OrderWithLines?>(null)
    val order: StateFlow<OrderWithLines?> = _order

    init {
        viewModelScope.launch { _order.value = orderRepository.getOrder(orderId) }
    }
}

@Composable
fun OrderPlacedScreen(
    onContinueShopping: () -> Unit,
    onViewOrders: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderPlacedViewModel = hiltViewModel(),
) {
    val order by viewModel.order.collectAsState()
    OrderPlacedContent(order = order, onContinueShopping = onContinueShopping, onViewOrders = onViewOrders, modifier = modifier)
}

@Composable
private fun OrderPlacedContent(
    order: OrderWithLines?,
    onContinueShopping: () -> Unit,
    onViewOrders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "check-scale",
    )

    Column(
        // plan_9 Phase A (F1): pushed screen, no app bottom bar — inset both system bars so the
        // confirmation content and CTAs clear the status and navigation bars.
        modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Rounded.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(96.dp).scale(scale),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("Order confirmed", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            "Order ${order?.order?.orderNumber ?: "…"}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        order?.order?.estimatedArrival?.let {
            Text("Estimated arrival: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(32.dp))
        ShrineButton(text = "Continue shopping", onClick = onContinueShopping, modifier = Modifier.fillMaxWidth())
        ShrineButton(text = "View my orders", onClick = onViewOrders, variant = ShrineButtonVariant.Text, modifier = Modifier.fillMaxWidth())
    }
}

// ---------------------------------------------------------------------------
// Order history
// ---------------------------------------------------------------------------

enum class OrderTab(val label: String) { All("All"), Active("Active"), Delivered("Delivered") }

data class OrderHistoryUiState(
    val loading: Boolean = true,
    val tab: OrderTab = OrderTab.All,
    val orders: List<OrderWithLines> = emptyList(),
)

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    orderRepository: OrderRepository,
    sessionRepository: SessionRepository,
) : ViewModel() {

    private val tab = MutableStateFlow(OrderTab.All)

    private val allOrders = sessionRepository.session.flatMapLatest { session ->
        orderRepository.orders(session.scopeId)
    }

    val state: StateFlow<OrderHistoryUiState> =
        combine(allOrders, tab) { orders, currentTab ->
            val filtered = when (currentTab) {
                OrderTab.All -> orders
                OrderTab.Active -> orders.filter { it.order.status != OrderStatus.DELIVERED }
                OrderTab.Delivered -> orders.filter { it.order.status == OrderStatus.DELIVERED }
            }
            OrderHistoryUiState(loading = false, tab = currentTab, orders = filtered)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderHistoryUiState())

    fun selectTab(tab: OrderTab) { this.tab.value = tab }
}

@Composable
fun OrderHistoryScreen(
    onOrder: (Long) -> Unit,
    onBack: () -> Unit,
    onBrowse: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    OrderHistoryContent(state = state, onOrder = onOrder, onBack = onBack, onBrowse = onBrowse, onSelectTab = viewModel::selectTab, modifier = modifier)
}

@Composable
private fun OrderHistoryContent(
    state: OrderHistoryUiState,
    onOrder: (Long) -> Unit,
    onBack: () -> Unit,
    onBrowse: () -> Unit,
    onSelectTab: (OrderTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "My orders", onBack = onBack) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ShrineTabRow(
                titles = OrderTab.entries.map { it.label },
                selectedIndex = state.tab.ordinal,
                onSelect = { onSelectTab(OrderTab.entries[it]) },
            )
            if (!state.loading && state.orders.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.Receipt,
                    title = "No orders yet",
                    subtitle = "When you place an order it will appear here.",
                    actionLabel = "Start shopping",
                    onAction = onBrowse,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = ShrineTheme.spacing.screenGutter, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.orders, key = { it.order.id }) { order ->
                        OrderRow(order = order, onClick = { onOrder(order.order.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderRow(order: OrderWithLines, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(56.dp),
            ) { Box(modifier = Modifier.aspectRatio(1f)) { ProductImagePlaceholder() } }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(order.order.orderNumber, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${order.lines.sumOf { it.quantity }} items · ${Money.format(order.order.totalCents)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ShrineStatusChip(status = order.order.status.toKind())
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Order detail
// ---------------------------------------------------------------------------

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val orderId: Long = savedStateHandle.toRoute<OrderDetail>().orderId
    private val _order = MutableStateFlow<OrderWithLines?>(null)
    val order: StateFlow<OrderWithLines?> = _order

    init {
        viewModelScope.launch { _order.value = orderRepository.getOrder(orderId) }
    }
}

@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val order by viewModel.order.collectAsState()
    OrderDetailContent(order = order, onBack = onBack, modifier = modifier)
}

@Composable
private fun OrderDetailContent(
    order: OrderWithLines?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = order?.order?.orderNumber ?: "Order", onBack = onBack) },
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = ShrineTheme.spacing.screenGutter, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        order.order.estimatedArrival?.let { Text("Arrives in $it", style = MaterialTheme.typography.bodyMedium) }
                    }
                    ShrineStatusChip(status = order.order.status.toKind())
                }
            }
            items(order.lines, key = { it.id }) { line ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(56.dp),
                    ) { Box(modifier = Modifier.aspectRatio(1f)) { ProductImagePlaceholder() } }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(line.name, style = MaterialTheme.typography.titleSmall)
                        Text("Qty ${line.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    PriceText(price = Money.format(line.priceCents * line.quantity))
                }
            }
            item { ShrineDivider() }
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Total", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    PriceText(price = Money.format(order.order.totalCents))
                }
            }
        }
    }
}

@Preview(name = "Order placed")
@Composable
private fun OrderPlacedPreview() {
    ShrineTheme { OrderPlacedContent(order = PreviewData.order, onContinueShopping = {}, onViewOrders = {}) }
}

@Preview(name = "Order history")
@Composable
private fun OrderHistoryPreview() {
    ShrineTheme {
        OrderHistoryContent(
            state = OrderHistoryUiState(loading = false, orders = listOf(PreviewData.order)),
            onOrder = {}, onBack = {}, onBrowse = {}, onSelectTab = {},
        )
    }
}

@Preview(name = "Order detail")
@Composable
private fun OrderDetailPreview() {
    ShrineTheme { OrderDetailContent(order = PreviewData.order, onBack = {}) }
}
