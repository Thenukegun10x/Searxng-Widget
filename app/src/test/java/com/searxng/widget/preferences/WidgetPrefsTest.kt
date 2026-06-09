package com.searxng.widget.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WidgetPrefsTest {

    private lateinit var prefs: WidgetPrefs
    private val dataStore: DataStore<Preferences> = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        val context = mockk<Context>(relaxed = true)
        prefs = WidgetPrefs(context, dataStore)
    }

    @Test
    fun `WidgetPrefs_getInstanceUrl_defaultIsNull`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_INSTANCE_URL) } returns null
        })

        prefs.getInstanceUrl() shouldBe null
    }

    @Test
    fun `WidgetPrefs_setAndGetInstanceUrl_doesNotThrow`() = runTest {
        prefs.setInstanceUrl("https://searxng.example.com")
    }

    @Test
    fun `WidgetPrefs_getThemeMode_defaultIsSystem`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_THEME_MODE) } returns null
        })

        prefs.getThemeMode() shouldBe WidgetPrefs.ThemeMode.SYSTEM
    }

    @Test
    fun `WidgetPrefs_getThemeMode_returnsDarkWhenStored`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_THEME_MODE) } returns WidgetPrefs.ThemeMode.DARK.name
        })

        prefs.getThemeMode() shouldBe WidgetPrefs.ThemeMode.DARK
    }

    @Test
    fun `WidgetPrefs_getThemeMode_returnsLightWhenStored`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_THEME_MODE) } returns WidgetPrefs.ThemeMode.LIGHT.name
        })

        prefs.getThemeMode() shouldBe WidgetPrefs.ThemeMode.LIGHT
    }

    @Test
    fun `WidgetPrefs_getAuthToken_defaultIsNull`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_AUTH_TOKEN) } returns null
        })

        prefs.getAuthToken() shouldBe null
    }

    @Test
    fun `WidgetPrefs_instanceUrlFlow_emitsNullWhenEmpty`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_INSTANCE_URL) } returns null
        })

        prefs.instanceUrl.first() shouldBe null
    }

    @Test
    fun `WidgetPrefs_instanceUrlFlow_emitsUrlWhenStored`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_INSTANCE_URL) } returns "https://searxng.example.com"
        })

        prefs.instanceUrl.first() shouldBe "https://searxng.example.com"
    }

    @Test
    fun `WidgetPrefs_themeModeFlow_emitsSystemByDefault`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_THEME_MODE) } returns null
        })

        prefs.themeMode.first() shouldBe WidgetPrefs.ThemeMode.SYSTEM
    }

    @Test
    fun `WidgetPrefs_themeModeFlow_emitsStoredTheme`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_THEME_MODE) } returns WidgetPrefs.ThemeMode.DARK.name
        })

        prefs.themeMode.first() shouldBe WidgetPrefs.ThemeMode.DARK
    }

    @Test
    fun `WidgetPrefs_clearInstanceUrl_callsUpdateData`() = runTest {
        prefs.clearInstanceUrl()
        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `WidgetPrefs_cachedResults_defaultIsNull`() = runTest {
        every { dataStore.data } returns flowOf(mockk {
            every { get(WidgetPrefs.KEY_CACHED_RESULTS) } returns null
        })

        prefs.getCachedResults() shouldBe null
    }
}
