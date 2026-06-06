package com.searxng.widget.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "widget_prefs")

class WidgetPrefs(private val context: Context) {

    enum class ThemeMode { SYSTEM, LIGHT, DARK }

    companion object {
        val KEY_INSTANCE_URL = stringPreferencesKey("instance_url")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    val instanceUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_INSTANCE_URL]
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        try {
            ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun getInstanceUrl(): String? {
        return context.dataStore.data.first()[KEY_INSTANCE_URL]
    }

    suspend fun setInstanceUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INSTANCE_URL] = url
        }
    }

    suspend fun getAuthToken(): String? {
        return context.dataStore.data.first()[KEY_AUTH_TOKEN]
    }

    suspend fun setAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
        }
    }

    suspend fun getThemeMode(): ThemeMode {
        val value = context.dataStore.data.first()[KEY_THEME_MODE]
        return try {
            ThemeMode.valueOf(value ?: ThemeMode.SYSTEM.name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }
}
