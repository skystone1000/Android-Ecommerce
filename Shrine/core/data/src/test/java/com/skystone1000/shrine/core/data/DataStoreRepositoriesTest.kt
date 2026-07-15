package com.skystone1000.shrine.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.skystone1000.shrine.core.model.ThemePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Session + Settings repositories over a real file-backed Preferences DataStore. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DataStoreRepositoriesTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var session: SessionRepository
    private lateinit var settings: SettingsRepository

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(scope = scope) {
            File(tmp.newFolder(), "test.preferences_pb")
        }
        session = DataStoreSessionRepository(dataStore)
        settings = DataStoreSettingsRepository(dataStore)
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun noSessionByDefault() = runTest {
        assertNull(session.session.first())
        assertNull(session.currentUserId())
    }

    @Test
    fun signInThenSignOut() = runTest {
        session.signIn(userId = 7, name = "Ava", email = "ava@shrine.com", phone = "555")
        val state = session.session.first()!!
        assertEquals(7L, state.userId)
        assertEquals("Ava", state.name)
        assertFalse(state.isGuest)
        assertEquals(7L, session.currentUserId())

        session.signOut()
        assertNull(session.session.first())
    }

    @Test
    fun guestHasActiveSessionButNoUserId() = runTest {
        session.continueAsGuest()
        val state = session.session.first()!!
        assertTrue(state.isGuest)
        assertEquals(DataStoreSessionRepository.NO_USER, state.userId)
        assertNull(session.currentUserId()) // a guest is not a real signed-in user
    }

    @Test
    fun settingsDefaultsAndUpdates() = runTest {
        val defaults = settings.settings.first()
        assertEquals(ThemePreference.SYSTEM, defaults.theme)
        assertTrue(defaults.largeImagery)
        assertTrue(defaults.orderUpdates)
        assertFalse(defaults.promotions)

        settings.setTheme(ThemePreference.DARK)
        settings.setPromotions(true)
        val updated = settings.settings.first()
        assertEquals(ThemePreference.DARK, updated.theme)
        assertTrue(updated.promotions)
    }
}
