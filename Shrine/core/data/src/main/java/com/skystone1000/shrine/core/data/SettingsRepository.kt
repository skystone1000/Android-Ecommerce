package com.skystone1000.shrine.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skystone1000.shrine.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** User-controllable app settings (figma Settings screen). */
data class SettingsState(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val largeImagery: Boolean = true,
    val orderUpdates: Boolean = true,
    val promotions: Boolean = false,
)

/** Persists app settings in DataStore. */
interface SettingsRepository {
    val settings: Flow<SettingsState>
    suspend fun setTheme(theme: ThemePreference)
    suspend fun setLargeImagery(enabled: Boolean)
    suspend fun setOrderUpdates(enabled: Boolean)
    suspend fun setPromotions(enabled: Boolean)
}

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private object Keys {
        val theme = stringPreferencesKey("settings_theme")
        val largeImagery = booleanPreferencesKey("settings_large_imagery")
        val orderUpdates = booleanPreferencesKey("settings_order_updates")
        val promotions = booleanPreferencesKey("settings_promotions")
    }

    override val settings: Flow<SettingsState> = dataStore.data.map { prefs ->
        SettingsState(
            theme = prefs[Keys.theme]?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
                ?: ThemePreference.SYSTEM,
            largeImagery = prefs[Keys.largeImagery] ?: true,
            orderUpdates = prefs[Keys.orderUpdates] ?: true,
            promotions = prefs[Keys.promotions] ?: false,
        )
    }

    override suspend fun setTheme(theme: ThemePreference) {
        dataStore.edit { it[Keys.theme] = theme.name }
    }

    override suspend fun setLargeImagery(enabled: Boolean) {
        dataStore.edit { it[Keys.largeImagery] = enabled }
    }

    override suspend fun setOrderUpdates(enabled: Boolean) {
        dataStore.edit { it[Keys.orderUpdates] = enabled }
    }

    override suspend fun setPromotions(enabled: Boolean) {
        dataStore.edit { it[Keys.promotions] = enabled }
    }
}
