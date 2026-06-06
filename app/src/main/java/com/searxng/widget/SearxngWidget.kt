package com.searxng.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.LocalSize
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.searxng.widget.preferences.WidgetPrefs
import com.searxng.widget.ui.SearchBar
import com.searxng.widget.ui.theme.WidgetBackground
import com.searxng.widget.ui.theme.WidgetColors
import com.searxng.widget.ui.theme.isDarkTheme

class SearxngWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = WidgetPrefs(context)
        val instanceUrl = prefs.getInstanceUrl()
        val themeMode = prefs.getThemeMode()
        provideContent {
            Content(instanceUrl = instanceUrl, themeMode = themeMode)
        }
    }

    @Composable
    fun Content(instanceUrl: String?, themeMode: WidgetPrefs.ThemeMode) {
        val context = LocalContext.current
        val isDark = when (themeMode) {
            WidgetPrefs.ThemeMode.LIGHT -> false
            WidgetPrefs.ThemeMode.DARK -> true
            WidgetPrefs.ThemeMode.SYSTEM -> isDarkTheme(context.resources.configuration)
        }
        val size = LocalSize.current
        val isCompact = size.width < 200.dp

        WidgetBackground(isDark = isDark) {
            if (instanceUrl.isNullOrBlank()) {
                UnconfiguredState(isDark, isCompact)
            } else {
                ReadyState(isDark, isCompact)
            }
        }
    }

    @Composable
    private fun UnconfiguredState(isDark: Boolean, isCompact: Boolean) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = if (isCompact) "SearXNG" else "SearXNG Widget",
                style = TextStyle(
                    color = ColorProvider(
                        if (isDark) WidgetColors.primaryDark else WidgetColors.primary
                    ),
                    fontSize = if (isCompact) 13.sp else 16.sp
                )
            )
            if (!isCompact) {
                Spacer(modifier = GlanceModifier.height(6.dp))
                Text(
                    text = "Tap to configure",
                    style = TextStyle(
                        color = ColorProvider(
                            if (isDark) WidgetColors.onSurfaceDark else WidgetColors.onSurfaceLight
                        ),
                        fontSize = 12.sp
                    ),
                    modifier = GlanceModifier.clickable(
                        actionStartActivity<MainActivity>()
                    )
                )
            }
        }
    }

    @Composable
    private fun ReadyState(isDark: Boolean, isCompact: Boolean) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(6.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            SearchBar(isDark = isDark, isCompact = isCompact)
        }
    }
}

class SearxngWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SearxngWidget()
}
