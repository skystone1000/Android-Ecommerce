@file:OptIn(ExperimentalMaterial3Api::class)

package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import com.skystone1000.shrine.core.data.AuthRepository
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.designsystem.component.ShrineAvatar
import com.skystone1000.shrine.designsystem.component.ShrineButton
import com.skystone1000.shrine.designsystem.component.ShrineDateField
import com.skystone1000.shrine.designsystem.component.ShrineDatePickerDialog
import com.skystone1000.shrine.designsystem.component.ShrineTextField
import com.skystone1000.shrine.designsystem.component.ShrineTopBar
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import com.skystone1000.shrine.ui.NO_USER
import com.skystone1000.shrine.ui.initials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirthMillis: Long? = null,
    val saving: Boolean = false,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val session = sessionRepository.session.first()
            val userId = session?.userId ?: NO_USER
            val user = if (userId != NO_USER) authRepository.getUser(userId) else null
            _state.value = EditProfileUiState(
                name = user?.name ?: session?.name.orEmpty(),
                email = user?.email ?: session?.email.orEmpty(),
                phone = user?.phone ?: session?.phone.orEmpty(),
                dateOfBirthMillis = user?.dateOfBirthMillis,
            )
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun onEmail(v: String) = _state.update { it.copy(email = v) }
    fun onPhone(v: String) = _state.update { it.copy(phone = v) }
    fun onDateOfBirth(millis: Long?) = _state.update { it.copy(dateOfBirthMillis = millis) }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val userId = sessionRepository.currentUserId()
            if (userId != null) {
                authRepository.updateProfile(
                    userId = userId,
                    name = current.name,
                    email = current.email,
                    phone = current.phone.ifBlank { null },
                    dateOfBirthMillis = current.dateOfBirthMillis,
                    avatarUri = null,
                )
                sessionRepository.signIn(userId, current.name, current.email, current.phone.ifBlank { null })
            }
            _state.update { it.copy(saving = false) }
            onSaved()
        }
    }
}

@Composable
fun EditProfileScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    EditProfileContent(
        state = state,
        onName = viewModel::onName,
        onEmail = viewModel::onEmail,
        onPhone = viewModel::onPhone,
        onDateOfBirth = viewModel::onDateOfBirth,
        onSave = { viewModel.save(onSaved) },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun EditProfileContent(
    state: EditProfileUiState,
    onName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPhone: (String) -> Unit,
    onDateOfBirth: (Long?) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrineTopBar(title = "Edit profile", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = ShrineTheme.spacing.screenGutter, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                ShrineAvatar(initials = initials(state.name.ifBlank { "?" }), size = 88)
            }
            ShrineTextField(value = state.name, onValueChange = onName, label = "Full name")
            ShrineTextField(value = state.email, onValueChange = onEmail, label = "Email")
            ShrineTextField(value = state.phone, onValueChange = onPhone, label = "Phone")
            ShrineDateField(
                label = "Date of birth",
                value = state.dateOfBirthMillis?.let { dateFormat.format(Date(it)) } ?: "",
                onClick = { showDatePicker = true },
            )
            ShrineButton(text = "Save", onClick = onSave, loading = state.saving, modifier = Modifier.fillMaxWidth())
        }
    }

    if (showDatePicker) {
        ShrineDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                onDateOfBirth(millis)
                showDatePicker = false
            },
        )
    }
}

@Preview(name = "Edit profile")
@Composable
private fun EditProfilePreview() {
    ShrineTheme {
        EditProfileContent(
            state = EditProfileUiState(name = "Ava Morgan", email = "ava@shrine.com", phone = "+1 555 0100"),
            onName = {}, onEmail = {}, onPhone = {}, onDateOfBirth = {}, onSave = {}, onBack = {},
        )
    }
}
