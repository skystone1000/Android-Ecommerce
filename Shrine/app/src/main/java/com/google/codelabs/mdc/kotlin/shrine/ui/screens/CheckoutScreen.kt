@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.google.codelabs.mdc.kotlin.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.google.codelabs.mdc.kotlin.shrine.core.data.AddressRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.CartRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.OrderRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.PaymentRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.subtotalCents
import com.google.codelabs.mdc.kotlin.shrine.core.model.AddressEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.DeliveryOption
import com.google.codelabs.mdc.kotlin.shrine.core.model.Money
import com.google.codelabs.mdc.kotlin.shrine.core.model.PaymentMethodEntity
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.PriceText
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineButton
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineDivider
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineRadioRow
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineTopBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ShrineTheme
import com.google.codelabs.mdc.kotlin.shrine.ui.NO_USER
import com.google.codelabs.mdc.kotlin.shrine.ui.scopeId
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

data class CheckoutUiState(
    val loading: Boolean = true,
    val address: AddressEntity? = null,
    val payment: PaymentMethodEntity? = null,
    val delivery: DeliveryOption = DeliveryOption.STANDARD,
    val subtotalCents: Int = 0,
    val itemCount: Int = 0,
    val placing: Boolean = false,
) {
    val totalCents: Int get() = subtotalCents + delivery.shippingCents
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val sessionRepository: SessionRepository,
    addressRepository: AddressRepository,
    paymentRepository: PaymentRepository,
    cartRepository: CartRepository,
) : ViewModel() {

    private val delivery = MutableStateFlow(DeliveryOption.STANDARD)
    private val placing = MutableStateFlow(false)

    val state: StateFlow<CheckoutUiState> =
        sessionRepository.session.flatMapLatest { session ->
            val uid = session.scopeId
            combine(
                addressRepository.addresses(uid),
                paymentRepository.paymentMethods(uid),
                cartRepository.cart(uid),
                delivery,
                placing,
            ) { addresses, payments, cart, deliveryOption, isPlacing ->
                CheckoutUiState(
                    loading = false,
                    address = addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull(),
                    payment = payments.firstOrNull { it.isDefault } ?: payments.firstOrNull(),
                    delivery = deliveryOption,
                    subtotalCents = cart.subtotalCents(),
                    itemCount = cart.sumOf { it.quantity },
                    placing = isPlacing,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckoutUiState())

    fun setDelivery(option: DeliveryOption) { delivery.value = option }

    fun placeOrder(onPlaced: (Long) -> Unit) {
        placing.value = true
        viewModelScope.launch {
            val userId = sessionRepository.currentUserId() ?: NO_USER
            val orderId = orderRepository.placeOrder(userId, delivery.value)
            placing.value = false
            if (orderId != null) onPlaced(orderId)
        }
    }
}

@Composable
fun CheckoutScreen(
    onPlaced: (Long) -> Unit,
    onBack: () -> Unit,
    onChangeAddress: () -> Unit,
    onChangePayment: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    CheckoutContent(
        state = state,
        onBack = onBack,
        onChangeAddress = onChangeAddress,
        onChangePayment = onChangePayment,
        onSetDelivery = viewModel::setDelivery,
        onPlaceOrder = { viewModel.placeOrder(onPlaced) },
        modifier = modifier,
    )
}

@Composable
private fun CheckoutContent(
    state: CheckoutUiState,
    onBack: () -> Unit,
    onChangeAddress: () -> Unit,
    onChangePayment: () -> Unit,
    onSetDelivery: (DeliveryOption) -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "Checkout", onBack = onBack) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    ShrineButton(
                        text = "Place order · ${Money.format(state.totalCents)}",
                        onClick = onPlaceOrder,
                        enabled = state.itemCount > 0,
                        loading = state.placing,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SectionCard(title = "Shipping address", actionLabel = "Change", onAction = onChangeAddress) {
                val a = state.address
                if (a != null) {
                    Text(a.fullName, style = MaterialTheme.typography.titleSmall)
                    Text("${a.line1}, ${a.city}, ${a.state} ${a.zip}", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("No address yet — tap Change to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SectionCard(title = "Delivery") {
                ShrineRadioRow(
                    selected = state.delivery == DeliveryOption.STANDARD,
                    onSelect = { onSetDelivery(DeliveryOption.STANDARD) },
                    label = "Standard (3–5 days)",
                    trailing = "Free",
                )
                ShrineRadioRow(
                    selected = state.delivery == DeliveryOption.EXPRESS,
                    onSelect = { onSetDelivery(DeliveryOption.EXPRESS) },
                    label = "Express (1–2 days)",
                    trailing = Money.format(DeliveryOption.EXPRESS.shippingCents),
                )
            }

            SectionCard(title = "Payment method", actionLabel = "Change", onAction = onChangePayment) {
                val p = state.payment
                if (p != null) {
                    Text("${p.brand} •••• ${p.last4}", style = MaterialTheme.typography.titleSmall)
                    Text("Expires ${p.expiry}", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("No payment method — tap Change to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SectionCard(title = "Order summary") {
                SummaryLine("Items (${state.itemCount})", Money.format(state.subtotalCents))
                SummaryLine("Shipping", if (state.delivery.shippingCents == 0) "Free" else Money.format(state.delivery.shippingCents))
                ShrineDivider()
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Total", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    PriceText(price = Money.format(state.totalCents))
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(name = "Checkout")
@Composable
private fun CheckoutPreview() {
    ShrineTheme {
        CheckoutContent(
            state = CheckoutUiState(
                loading = false,
                address = PreviewData.address,
                payment = PreviewData.payment,
                delivery = DeliveryOption.STANDARD,
                subtotalCents = 179_700,
                itemCount = 3,
            ),
            onBack = {}, onChangeAddress = {}, onChangePayment = {}, onSetDelivery = {}, onPlaceOrder = {},
        )
    }
}
