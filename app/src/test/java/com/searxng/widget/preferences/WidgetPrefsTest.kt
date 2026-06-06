package com.searxng.widget.preferences

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class WidgetPrefsTest {

    private lateinit var tmpDir: File
    private lateinit var prefs: WidgetPrefs

    @BeforeEach
    fun setUp() {
        tmpDir = File.createTempFile("datastore", null).also { it.delete(); it.mkdirs() }
        val context = mockk<Context> {
            every { applicationContext } returns this
            every { filesDir } returns tmpDir
        }
        prefs = WidgetPrefs(context)
    }

    @AfterEach
    fun tearDown() {
        tmpDir.deleteRecursively()
    }

    @Test
    fun `WidgetPrefs_getInstanceUrl_defaultIsNull`() = runTest {
        prefs.getInstanceUrl() shouldBe null
    }

    @Test
    fun `WidgetPrefs_setAndGetInstanceUrl_returnsStoredUrl`() = runTest {
        val url = "https://searxng.example.com"

        prefs.setInstanceUrl(url)
        prefs.getInstanceUrl() shouldBe url
    }

    @Test
    fun `WidgetPrefs_getThemeMode_defaultIsSystem`() = runTest {
        prefs.getThemeMode() shouldBe WidgetPrefs.ThemeMode.SYSTEM
    }

    @Test
    fun `WidgetPrefs_setAndGetThemeMode_returnsStoredMode`() = runTest {
        prefs.setThemeMode(WidgetPrefs.ThemeMode.DARK)
        prefs.getThemeMode() shouldBe WidgetPrefs.ThemeMode.DARK

        prefs.setThemeMode(WidgetPrefs.ThemeMode.LIGHT)
        prefs.getThemeMode() shouldBe WidgetPrefs.ThemeMode.LIGHT
    }

    @Test
    fun `WidgetPrefs_setAndGetAuthToken_returnsStoredToken`() = runTest {
        prefs.setAuthToken("test-token")
        prefs.getAuthToken() shouldBe "test-token"
    }

    @Test
    fun `WidgetPrefs_instanceUrlFlow_emitsUpdates`() = runTest {
        prefs.instanceUrl.first() shouldBe null

        prefs.setInstanceUrl("https://searxng.example.com")

        prefs.instanceUrl.first() shouldBe "https://searxng.example.com"
    }

    @Test
    fun `WidgetPrefs_themeModeFlow_emitsUpdates`() = runTest {
        prefs.themeMode.first() shouldBe WidgetPrefs.ThemeMode.SYSTEM

        prefs.setThemeMode(WidgetPrefs.ThemeMode.DARK)

        prefs.themeMode.first() shouldBe WidgetPrefs.ThemeMode.DARK
    }
}
