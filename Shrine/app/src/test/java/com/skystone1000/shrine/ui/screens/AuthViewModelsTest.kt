package com.skystone1000.shrine.ui.screens

import com.skystone1000.shrine.core.data.AuthResult
import com.skystone1000.shrine.testing.FakeAuthRepository
import com.skystone1000.shrine.testing.FakeSessionRepository
import com.skystone1000.shrine.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelsTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    // --- Login ---

    @Test
    fun login_withInvalidInput_setsFieldErrorsAndDoesNotSucceed() = runTest(mainRule.dispatcher) {
        val vm = LoginViewModel(FakeAuthRepository(), FakeSessionRepository())
        vm.onEmail("not-an-email")
        vm.onPassword("short")
        var succeeded = false
        vm.login { succeeded = true }
        assertFalse(succeeded)
        assertNotNull(vm.state.value.emailError)
        assertNotNull(vm.state.value.passwordError)
    }

    @Test
    fun login_withValidCredentials_signsInAndCallsOnSuccess() = runTest(mainRule.dispatcher) {
        val auth = FakeAuthRepository().also { it.register("Ava", "ava@shrine.com", "secret123") }
        val session = FakeSessionRepository()
        val vm = LoginViewModel(auth, session)
        vm.onEmail("ava@shrine.com")
        vm.onPassword("secret123")
        var succeeded = false
        vm.login { succeeded = true }

        assertTrue(succeeded)
        val state = session.session.first()!!
        assertEquals("ava@shrine.com", state.email)
        assertFalse(state.isGuest)
    }

    @Test
    fun login_withWrongPassword_setsFormErrorAndStaysSignedOut() = runTest(mainRule.dispatcher) {
        val auth = FakeAuthRepository().also { it.register("Ava", "ava@shrine.com", "secret123") }
        val session = FakeSessionRepository()
        val vm = LoginViewModel(auth, session)
        vm.onEmail("ava@shrine.com")
        vm.onPassword("wrongpass")
        var succeeded = false
        vm.login { succeeded = true }

        assertFalse(succeeded)
        assertNotNull(vm.state.value.formError)
        assertFalse(vm.state.value.submitting)
        assertNull(session.session.first())
    }

    @Test
    fun continueAsGuest_startsAGuestSession() = runTest(mainRule.dispatcher) {
        val session = FakeSessionRepository()
        val vm = LoginViewModel(FakeAuthRepository(), session)
        var succeeded = false
        vm.continueAsGuest { succeeded = true }
        assertTrue(succeeded)
        assertTrue(session.session.first()!!.isGuest)
    }

    // --- Register ---

    @Test
    fun register_withoutAcceptingTerms_blocksAndSetsTermsError() = runTest(mainRule.dispatcher) {
        val vm = RegisterViewModel(FakeAuthRepository(), FakeSessionRepository())
        vm.onName("Ava")
        vm.onEmail("ava@shrine.com")
        vm.onPassword("secret123")
        var succeeded = false
        vm.register { succeeded = true }
        assertFalse(succeeded)
        assertNotNull(vm.state.value.termsError)
    }

    @Test
    fun register_validForm_createsAccountSignsInAndSucceeds() = runTest(mainRule.dispatcher) {
        val auth = FakeAuthRepository()
        val session = FakeSessionRepository()
        val vm = RegisterViewModel(auth, session)
        vm.onName("Ava")
        vm.onEmail("Ava@Shrine.com")
        vm.onPassword("secret123")
        vm.onAcceptTerms(true)
        var succeeded = false
        vm.register { succeeded = true }

        assertTrue(succeeded)
        assertEquals(AuthResult.InvalidCredentials, auth.login("ava@shrine.com", "nope"))
        assertTrue(auth.login("ava@shrine.com", "secret123") is AuthResult.Success)
        assertEquals("ava@shrine.com", session.session.first()!!.email)
    }

    @Test
    fun register_duplicateEmail_setsEmailError() = runTest(mainRule.dispatcher) {
        val auth = FakeAuthRepository().also { it.register("Existing", "ava@shrine.com", "secret123") }
        val vm = RegisterViewModel(auth, FakeSessionRepository())
        vm.onName("Ava")
        vm.onEmail("ava@shrine.com")
        vm.onPassword("secret123")
        vm.onAcceptTerms(true)
        var succeeded = false
        vm.register { succeeded = true }

        assertFalse(succeeded)
        assertNotNull(vm.state.value.emailError)
    }
}
