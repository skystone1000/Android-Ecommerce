@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.skystone1000.shrine.core.data.PaymentRepository
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.core.model.PaymentMethodEntity
import com.skystone1000.shrine.designsystem.component.EmptyState
import com.skystone1000.shrine.designsystem.component.ShrineButton
import com.skystone1000.shrine.designsystem.component.ShrineIconButton
import com.skystone1000.shrine.designsystem.component.ShrineTextField
import com.skystone1000.shrine.designsystem.component.ShrineTopBar
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.ui.NO_USER
import com.skystone1000.shrine.ui.scopeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val methods: StateFlow<List<PaymentMethodEntity>> =
        sessionRepository.session.flatMapLatest { paymentRepository.paymentMethods(it.scopeId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(brand: String, last4: String, expiry: String) {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId() ?: NO_USER
            val makeDefault = paymentRepository.getDefault(uid) == null
            paymentRepository.add(
                PaymentMethodEntity(
                    userId = uid, brand = brand, last4 = last4.takeLast(4), expiry = expiry, isDefault = makeDefault,
                ),
            )
        }
    }

    fun setDefault(methodId: Long) {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId() ?: NO_USER
            paymentRepository.setDefault(uid, methodId)
        }
    }

    fun delete(method: PaymentMethodEntity) {
        viewModelScope.launch { paymentRepository.delete(method) }
    }
}

@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentMethodsViewModel = hiltViewModel(),
) {
    val methods by viewModel.methods.collectAsState()
    PaymentMethodsContent(
        methods = methods,
        onBack = onBack,
        onSetDefault = viewModel::setDefault,
        onDelete = viewModel::delete,
        onAdd = viewModel::add,
        modifier = modifier,
    )
}

@Composable
private fun PaymentMethodsContent(
    methods: List<PaymentMethodEntity>,
    onBack: () -> Unit,
    onSetDefault: (Long) -> Unit,
    onDelete: (PaymentMethodEntity) -> Unit,
    onAdd: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "Payment methods", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add card")
            }
        },
    ) { padding ->
        if (methods.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.CreditCard,
                title = "No payment methods",
                subtitle = "Add a card for faster checkout. No real card data is stored.",
                modifier = Modifier.padding(padding),
                actionLabel = "Add card",
                onAction = { showForm = true },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = ShrineTheme.spacing.screenGutter, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(methods, key = { it.id }) { method ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().clickable { onSetDefault(method.id) },
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CreditCard, contentDescription = null)
                            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                                Text("${method.brand} •••• ${method.last4}", style = MaterialTheme.typography.titleSmall)
                                Text("Expires ${method.expiry}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (method.isDefault) AssistChip(onClick = {}, label = { Text("Default") })
                            }
                            ShrineIconButton(Icons.Rounded.Delete, contentDescription = "Delete", onClick = { onDelete(method) })
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        CardFormSheet(
            onDismiss = { showForm = false },
            onSave = { brand, number, expiry ->
                onAdd(brand, number, expiry)
                showForm = false
            },
        )
    }
}

@Composable
private fun CardFormSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    var brand by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add card", style = MaterialTheme.typography.titleLarge)
            ShrineTextField(value = brand, onValueChange = { brand = it }, label = "Card brand", placeholder = "Visa")
            ShrineTextField(value = number, onValueChange = { number = it }, label = "Card number")
            ShrineTextField(value = expiry, onValueChange = { expiry = it }, label = "Expiry", placeholder = "MM/YY")
            ShrineButton(
                text = "Save card",
                onClick = { onSave(brand, number, expiry) },
                enabled = brand.isNotBlank() && number.length >= 4 && expiry.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Payment methods")
@Composable
private fun PaymentMethodsPreview() {
    ShrineTheme {
        PaymentMethodsContent(
            methods = listOf(PreviewData.payment),
            onBack = {}, onSetDefault = {}, onDelete = {}, onAdd = { _, _, _ -> },
        )
    }
}
