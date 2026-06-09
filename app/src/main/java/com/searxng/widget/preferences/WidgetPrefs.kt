package com.searxng.widget.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "widget_prefs")

data class WidgetState(
    val instanceUrl: String?,
    val themeMode: WidgetPrefs.ThemeMode,
    val cachedResults: String?
)

class WidgetPrefs(
    private val context: Context,
    val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context, context.dataStore)

    enum class ThemeMode { SYSTEM, LIGHT, DARK }

    companion object {
        val KEY_INSTANCE_URL = stringPreferencesKey("instance_url")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_CACHED_RESULTS = stringPreferencesKey("cached_results")
    }

    val instanceUrl: Flow<String?>
        get() = dataStore.data
            .map { prefs -> prefs[KEY_INSTANCE_URL] }
            .catch { emit(null) }

    val themeMode: Flow<ThemeMode>
        get() = dataStore.data
            .map { prefs ->
                try {
                    ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
                } catch (_: IllegalArgumentException) {
                    ThemeMode.SYSTEM
                }
            }
            .catch { emit(ThemeMode.SYSTEM) }

    suspend fun getWidgetState(): WidgetState {
        return try {
            val prefs = dataStore.data.first()
            WidgetState(
                instanceUrl = prefs[KEY_INSTANCE_URL],
                themeMode = try {
                    ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
                } catch (_: IllegalArgumentException) {
                    ThemeMode.SYSTEM
                },
                cachedResults = prefs[KEY_CACHED_RESULTS]
            )
        } catch (_: IOException) {
            WidgetState(null, ThemeMode.SYSTEM, null)
        }
    }

    suspend fun getInstanceUrl(): String? {
        return try {
            dataStore.data.first()[KEY_INSTANCE_URL]
        } catch (_: IOException) {
            null
        }
    }

    suspend fun setInstanceUrl(url: String) {
        try {
            dataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply {
                    this[KEY_INSTANCE_URL] = url
                }
            }
        } catch (_: IOException) { }
    }

    suspend fun clearInstanceUrl() {
        try {
            dataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply {
                    remove(KEY_INSTANCE_URL)
                    remove(KEY_CACHED_RESULTS)
                }
            }
        } catch (_: IOException) { }
    }

    suspend fun getCachedResults(): String? {
        return try {
            dataStore.data.first()[KEY_CACHED_RESULTS]
        } catch (_: IOException) { null }
    }

    suspend fun setCachedResults(json: String) {
        try {
            dataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply {
                    this[KEY_CACHED_RESULTS] = json
                }
            }
        } catch (_: IOException) { }
    }

    suspend fun getAuthToken(): String? {
        return try {
            dataStore.data.first()[KEY_AUTH_TOKEN]
        } catch (_: IOException) {
            null
        }
    }

    suspend fun setAuthToken(token: String) {
        try {
            dataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply {
                    this[KEY_AUTH_TOKEN] = token
                }
            }
        } catch (_: IOException) { }
    }

    suspend fun getThemeMode(): ThemeMode {
        return try {
            val value = dataStore.data.first()[KEY_THEME_MODE]
            try {
                ThemeMode.valueOf(value ?: ThemeMode.SYSTEM.name)
            } catch (_: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        } catch (_: IOException) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        try {
            dataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply {
                    this[KEY_THEME_MODE] = mode.name
                }
            }
        } catch (_: IOException) { }
    }
}
