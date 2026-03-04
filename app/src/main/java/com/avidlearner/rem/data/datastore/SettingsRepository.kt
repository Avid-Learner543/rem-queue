package com.avidlearner.rem.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val NOTIFICATION_TITLE = stringPreferencesKey("notification_title")
        val IS_PERSISTENT = booleanPreferencesKey("is_persistent")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val NOTIFICATION_COLOR = longPreferencesKey("notification_color")
        val USE_ADAPTIVE_THEME = booleanPreferencesKey("use_adaptive_theme")
    }

    val notificationTitleFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATION_TITLE] ?: "Quote of the Day"
    }

    val isPersistentFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_PERSISTENT] ?: false
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_MODE] ?: false
    }

    val notificationColorFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATION_COLOR] ?: 0xFF6200EE // Default purple-ish
    }

    // A single class to hold all settings for easier access in ViewModels
    data class AppSettings(
        val title: String,
        val isPersistent: Boolean,
        val isDarkMode: Boolean,
        val color: Long,
        val useAdaptiveTheme: Boolean
    )

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            title = preferences[PreferencesKeys.NOTIFICATION_TITLE] ?: "Quote of the Day",
            isPersistent = preferences[PreferencesKeys.IS_PERSISTENT] ?: false,
            isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: false,
            color = preferences[PreferencesKeys.NOTIFICATION_COLOR] ?: 0xFF6200EE,
            useAdaptiveTheme = preferences[PreferencesKeys.USE_ADAPTIVE_THEME] ?: false
        )
    }

    suspend fun saveNotificationTitle(title: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_TITLE] = title
        }
    }

    suspend fun saveIsPersistent(isPersistent: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PERSISTENT] = isPersistent
        }
    }

    suspend fun saveIsDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode
        }
    }

    suspend fun saveNotificationColor(color: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_COLOR] = color
        }
    }

    suspend fun saveUseAdaptiveTheme(useAdaptive: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_ADAPTIVE_THEME] = useAdaptive
        }
    }
}
