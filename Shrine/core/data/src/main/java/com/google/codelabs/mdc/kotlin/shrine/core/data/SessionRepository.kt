package com.google.codelabs.mdc.kotlin.shrine.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** The current sign-in state. A guest has no [userId] but may browse. */
data class SessionState(
    val userId: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val isGuest: Boolean,
)

/** Persists the logged-in session in DataStore (replaces the legacy per-Activity SharedPreferences). */
interface SessionRepository {
    /** Emits the active session, or null when nobody is signed in and not browsing as guest. */
    val session: Flow<SessionState?>
    suspend fun currentUserId(): Long?
    suspend fun signIn(userId: Long, name: String, email: String, phone: String?)
    suspend fun continueAsGuest()
    suspend fun signOut()
}

@Singleton
class DataStoreSessionRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SessionRepository {

    private object Keys {
        val userId = longPreferencesKey("session_user_id")
        val name = stringPreferencesKey("session_name")
        val email = stringPreferencesKey("session_email")
        val phone = stringPreferencesKey("session_phone")
        val isGuest = booleanPreferencesKey("session_is_guest")
        val active = booleanPreferencesKey("session_active")
    }

    override val session: Flow<SessionState?> = dataStore.data.map { prefs ->
        if (prefs[Keys.active] != true) return@map null
        SessionState(
            userId = prefs[Keys.userId] ?: NO_USER,
            name = prefs[Keys.name].orEmpty(),
            email = prefs[Keys.email].orEmpty(),
            phone = prefs[Keys.phone],
            isGuest = prefs[Keys.isGuest] ?: false,
        )
    }

    override suspend fun currentUserId(): Long? {
        val state = session.first() ?: return null
        return state.userId.takeIf { it != NO_USER }
    }

    override suspend fun signIn(userId: Long, name: String, email: String, phone: String?) {
        dataStore.edit { prefs ->
            prefs[Keys.active] = true
            prefs[Keys.isGuest] = false
            prefs[Keys.userId] = userId
            prefs[Keys.name] = name
            prefs[Keys.email] = email
            if (phone != null) prefs[Keys.phone] = phone else prefs.remove(Keys.phone)
        }
    }

    override suspend fun continueAsGuest() {
        dataStore.edit { prefs ->
            prefs[Keys.active] = true
            prefs[Keys.isGuest] = true
            prefs[Keys.userId] = NO_USER
            prefs.remove(Keys.name)
            prefs.remove(Keys.email)
            prefs.remove(Keys.phone)
        }
    }

    override suspend fun signOut() {
        dataStore.edit { it.clear() }
    }

    companion object {
        const val NO_USER = -1L
    }
}
