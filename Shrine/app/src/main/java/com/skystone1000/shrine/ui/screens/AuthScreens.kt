package com.skystone1000.shrine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skystone1000.shrine.core.data.AuthRepository
import com.skystone1000.shrine.core.data.AuthResult
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.designsystem.component.PasswordStrength
import com.skystone1000.shrine.designsystem.component.PasswordStrengthMeter
import com.skystone1000.shrine.designsystem.component.ShrineButton
import com.skystone1000.shrine.designsystem.component.ShrineButtonVariant
import com.skystone1000.shrine.designsystem.component.ShrineCheckboxRow
import com.skystone1000.shrine.designsystem.component.ShrinePasswordField
import com.skystone1000.shrine.designsystem.component.ShrineTextField
import com.skystone1000.shrine.designsystem.theme.ShrineTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------------------------------------------------------------------------
// Login
// ---------------------------------------------------------------------------

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null,
    val submitting: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmail(value: String) = _state.update { it.copy(email = value, emailError = null, formError = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, passwordError = null, formError = null) }

    fun login(onSuccess: () -> Unit) {
        val current = _state.value
        val emailError = if (!current.email.contains("@")) "Enter a valid email" else null
        val passwordError = if (current.password.length < 8) "At least 8 characters" else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        _state.update { it.copy(submitting = true, formError = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(current.email, current.password)) {
                is AuthResult.Success -> {
                    val user = authRepository.getUser(result.userId)
                    sessionRepository.signIn(
                        userId = result.userId,
                        name = user?.name ?: current.email.substringBefore("@"),
                        email = user?.email ?: current.email,
                        phone = user?.phone,
                    )
                    onSuccess()
                }
                else -> _state.update { it.copy(submitting = false, formError = "Incorrect email or password") }
            }
        }
    }

    fun continueAsGuest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.continueAsGuest()
            onSuccess()
        }
    }
}

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LoginContent(
        state = state,
        onEmail = viewModel::onEmail,
        onPassword = viewModel::onPassword,
        onSignIn = { viewModel.login(onSignedIn) },
        onGuest = { viewModel.continueAsGuest(onSignedIn) },
        onCreateAccount = onCreateAccount,
        onForgotPassword = onForgotPassword,
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onSignIn: () -> Unit,
    onGuest: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthScaffold(title = "Welcome back", subtitle = "Sign in to continue shopping", modifier = modifier) {
        ShrineTextField(
            value = state.email,
            onValueChange = onEmail,
            label = "Email",
            placeholder = "you@example.com",
            errorText = state.emailError,
        )
        ShrinePasswordField(value = state.password, onValueChange = onPassword, errorText = state.passwordError)
        if (state.formError != null) {
            Text(state.formError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        ShrineButton(text = "Sign in", onClick = onSignIn, loading = state.submitting, modifier = Modifier.fillMaxWidth())
        ShrineButton(text = "Forgot password?", onClick = onForgotPassword, variant = ShrineButtonVariant.Text, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        ShrineButton(text = "Create account", onClick = onCreateAccount, variant = ShrineButtonVariant.Outlined, modifier = Modifier.fillMaxWidth())
        ShrineButton(text = "Skip — browse as guest", onClick = onGuest, variant = ShrineButtonVariant.Text, modifier = Modifier.fillMaxWidth())
    }
}

// ---------------------------------------------------------------------------
// Register
// ---------------------------------------------------------------------------

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val acceptedTerms: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val termsError: String? = null,
    val formError: String? = null,
    val submitting: Boolean = false,
) {
    val strength: PasswordStrength = when {
        password.length >= 12 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> PasswordStrength.Strong
        password.length >= 8 -> PasswordStrength.Medium
        else -> PasswordStrength.Weak
    }
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onName(value: String) = _state.update { it.copy(name = value, nameError = null, formError = null) }
    fun onEmail(value: String) = _state.update { it.copy(email = value, emailError = null, formError = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, passwordError = null, formError = null) }
    fun onAcceptTerms(value: Boolean) = _state.update { it.copy(acceptedTerms = value, termsError = null) }

    fun register(onSuccess: () -> Unit) {
        val current = _state.value
        val nameError = if (current.name.isBlank()) "Enter your name" else null
        val emailError = if (!current.email.contains("@")) "Enter a valid email" else null
        val passwordError = if (current.password.length < 8) "At least 8 characters" else null
        val termsError = if (!current.acceptedTerms) "Please accept the terms to continue" else null
        if (nameError != null || emailError != null || passwordError != null || termsError != null) {
            _state.update {
                it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError, termsError = termsError)
            }
            return
        }
        _state.update { it.copy(submitting = true, formError = null) }
        viewModelScope.launch {
            when (authRepository.register(current.name, current.email, current.password)) {
                is AuthResult.Success -> {
                    val signedIn = authRepository.login(current.email, current.password)
                    if (signedIn is AuthResult.Success) {
                        sessionRepository.signIn(signedIn.userId, current.name.trim(), current.email.trim().lowercase(), null)
                    }
                    onSuccess()
                }
                AuthResult.EmailTaken ->
                    _state.update { it.copy(submitting = false, emailError = "That email is already registered") }
                AuthResult.InvalidCredentials ->
                    _state.update { it.copy(submitting = false, formError = "Could not create account") }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBackToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    RegisterContent(
        state = state,
        onName = viewModel::onName,
        onEmail = viewModel::onEmail,
        onPassword = viewModel::onPassword,
        onAcceptTerms = viewModel::onAcceptTerms,
        onRegister = { viewModel.register(onRegistered) },
        onBackToSignIn = onBackToSignIn,
        modifier = modifier,
    )
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onAcceptTerms: (Boolean) -> Unit,
    onRegister: () -> Unit,
    onBackToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthScaffold(title = "Create account", subtitle = "Join Shrine in a few seconds", modifier = modifier) {
        ShrineTextField(value = state.name, onValueChange = onName, label = "Full name", errorText = state.nameError)
        ShrineTextField(
            value = state.email,
            onValueChange = onEmail,
            label = "Email",
            placeholder = "you@example.com",
            errorText = state.emailError,
        )
        ShrinePasswordField(value = state.password, onValueChange = onPassword, errorText = state.passwordError)
        if (state.password.isNotEmpty()) PasswordStrengthMeter(strength = state.strength)
        ShrineCheckboxRow(
            checked = state.acceptedTerms,
            onCheckedChange = onAcceptTerms,
            label = "I agree to the Terms and Privacy Policy",
        )
        if (state.termsError != null) {
            Text(state.termsError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (state.formError != null) {
            Text(state.formError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        ShrineButton(text = "Create account", onClick = onRegister, loading = state.submitting, modifier = Modifier.fillMaxWidth())
        ShrineButton(text = "Back to sign in", onClick = onBackToSignIn, variant = ShrineButtonVariant.Text, modifier = Modifier.fillMaxWidth())
    }
}

// ---------------------------------------------------------------------------
// Forgot password (placeholder — no backend in this demo)
// ---------------------------------------------------------------------------

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    AuthScaffold(
        title = "Forgot password",
        subtitle = "Enter your email and we'll send reset instructions. (Placeholder — no backend in this demo.)",
        modifier = modifier,
    ) {
        var email by remember { mutableStateOf("") }
        ShrineTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "you@example.com")
        ShrineButton(text = "Send reset link", onClick = onBack, modifier = Modifier.fillMaxWidth())
        ShrineButton(text = "Back", onClick = onBack, variant = ShrineButtonVariant.Text, modifier = Modifier.fillMaxWidth())
    }
}

/** Shared centered auth layout: brand wordmark, title/subtitle, then the form [content]. */
@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Text("SHRINE", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Preview(name = "Login")
@Composable
private fun LoginPreview() {
    ShrineTheme {
        LoginContent(
            state = LoginUiState(email = "ava@shrine.com", password = "password1"),
            onEmail = {}, onPassword = {}, onSignIn = {}, onGuest = {}, onCreateAccount = {}, onForgotPassword = {},
        )
    }
}

@Preview(name = "Register")
@Composable
private fun RegisterPreview() {
    ShrineTheme {
        RegisterContent(
            state = RegisterUiState(name = "Ava Morgan", email = "ava@shrine.com", password = "Password!23", acceptedTerms = true),
            onName = {}, onEmail = {}, onPassword = {}, onAcceptTerms = {}, onRegister = {}, onBackToSignIn = {},
        )
    }
}

@Preview(name = "Forgot password")
@Composable
private fun ForgotPasswordPreview() {
    ShrineTheme { ForgotPasswordScreen(onBack = {}) }
}
