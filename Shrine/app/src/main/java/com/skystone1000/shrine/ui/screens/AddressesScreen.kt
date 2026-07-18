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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocationOn
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
import com.skystone1000.shrine.core.data.AddressRepository
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.core.model.AddressEntity
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
class AddressesViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val addresses: StateFlow<List<AddressEntity>> =
        sessionRepository.session.flatMapLatest { addressRepository.addresses(it.scopeId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(fullName: String, line1: String, city: String, state: String, zip: String) {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId() ?: NO_USER
            val makeDefault = addressRepository.getDefault(uid) == null
            addressRepository.add(
                AddressEntity(
                    userId = uid, fullName = fullName, line1 = line1, city = city, state = state, zip = zip,
                    isDefault = makeDefault,
                ),
            )
        }
    }

    fun setDefault(addressId: Long) {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId() ?: NO_USER
            addressRepository.setDefault(uid, addressId)
        }
    }

    fun delete(address: AddressEntity) {
        viewModelScope.launch { addressRepository.delete(address) }
    }
}

@Composable
fun AddressesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddressesViewModel = hiltViewModel(),
) {
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    AddressesContent(
        addresses = addresses,
        onBack = onBack,
        onSetDefault = viewModel::setDefault,
        onDelete = viewModel::delete,
        onAdd = viewModel::add,
        modifier = modifier,
    )
}

@Composable
private fun AddressesContent(
    addresses: List<AddressEntity>,
    onBack: () -> Unit,
    onSetDefault: (Long) -> Unit,
    onDelete: (AddressEntity) -> Unit,
    onAdd: (String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "Addresses", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add address")
            }
        },
    ) { padding ->
        if (addresses.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.LocationOn,
                title = "No addresses",
                subtitle = "Add a shipping address for faster checkout.",
                modifier = Modifier.padding(padding),
                actionLabel = "Add address",
                onAction = { showForm = true },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = ShrineTheme.spacing.screenGutter, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(addresses, key = { it.id }) { address ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().clickable { onSetDefault(address.id) },
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(address.fullName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${address.line1}, ${address.city}, ${address.state} ${address.zip}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (address.isDefault) AssistChip(onClick = {}, label = { Text("Default") })
                            }
                            ShrineIconButton(Icons.Rounded.Delete, contentDescription = "Delete", onClick = { onDelete(address) })
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        AddressFormSheet(
            onDismiss = { showForm = false },
            onSave = { fullName, line1, city, state, zip ->
                onAdd(fullName, line1, city, state, zip)
                showForm = false
            },
        )
    }
}

@Composable
private fun AddressFormSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
) {
    var fullName by remember { mutableStateOf("") }
    var line1 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add address", style = MaterialTheme.typography.titleLarge)
            ShrineTextField(value = fullName, onValueChange = { fullName = it }, label = "Full name")
            ShrineTextField(value = line1, onValueChange = { line1 = it }, label = "Address")
            ShrineTextField(value = city, onValueChange = { city = it }, label = "City")
            ShrineTextField(value = state, onValueChange = { state = it }, label = "State")
            ShrineTextField(value = zip, onValueChange = { zip = it }, label = "ZIP")
            ShrineButton(
                text = "Save address",
                onClick = { onSave(fullName, line1, city, state, zip) },
                enabled = fullName.isNotBlank() && line1.isNotBlank() && city.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Addresses")
@Composable
private fun AddressesPreview() {
    ShrineTheme {
        AddressesContent(
            addresses = listOf(PreviewData.address),
            onBack = {}, onSetDefault = {}, onDelete = {}, onAdd = { _, _, _, _, _ -> },
        )
    }
}
