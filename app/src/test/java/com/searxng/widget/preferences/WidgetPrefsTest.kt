package com.searxng.widget.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class WidgetPrefsTest {

    @Test
    fun `default instance url is null`() = runTest {
        val prefs = WidgetPrefs(ApplicationProvider.getApplicationContext())
        prefs.getInstanceUrl() shouldBe null
    }

    @Test
    fun `set and get instance url`() = runTest {
        val prefs = WidgetPrefs(ApplicationProvider.getApplicationContext())
        val url = "https://searxng.example.com"

        prefs.setInstanceUrl(url)

        prefs.getInstanceUrl() shouldBe url
    }

    @Test
    fun `default theme mode is system`() = runTest {
        val prefs = WidgetPrefs(ApplicationProvider.getApplicationContext())

        prefs.getThemeMode() shouldBe WidgetPrefs.ThemeMode.SYSTEM
    }

    @Test
    fun `set and get theme mode`() = runTest {
        val prefs = WidgetPrefs(ApplicationProvider.getApplicationContext())

        prefs.setThemeMode(WidgetPrefs.ThemeMode.DARK)
        prefs.getThemeMode() shouldBe WidgetPrefs.ThemeMode.DARK

        prefs.setThemeMode(WidgetPrefs.ThemeMode.LIGHT)
        prefs.getThemeMode() shouldBe WidgetPrefs.ThemeMode.LIGHT
    }
}
