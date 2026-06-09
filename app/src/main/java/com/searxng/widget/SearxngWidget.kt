package com.searxng.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.gson.Gson
import com.searxng.widget.data.model.SearchResponse
import com.searxng.widget.preferences.WidgetPrefs
import com.searxng.widget.ui.SearchBar
import com.searxng.widget.ui.SearchOverlayActivity
import com.searxng.widget.ui.theme.WidgetBackground
import com.searxng.widget.ui.theme.WidgetColors
import com.searxng.widget.ui.theme.isDarkTheme

class SearxngWidget : GlanceAppWidget() {

    companion object {
        private val gson = Gson()
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = WidgetPrefs(context)
        val state = prefs.getWidgetState()
        val cachedResponse = if (!state.cachedResults.isNullOrBlank()) {
            try {
                gson.fromJson(state.cachedResults, SearchResponse::class.java)
            } catch (_: Exception) { null }
        } else null

        provideContent {
            Content(
                instanceUrl = state.instanceUrl,
                themeMode = state.themeMode,
                cachedResponse = cachedResponse
            )
        }
    }

    @Composable
    fun Content(
        instanceUrl: String?,
        themeMode: WidgetPrefs.ThemeMode,
        cachedResponse: SearchResponse?
    ) {
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
                UnconfiguredState(isDark = isDark, isCompact = isCompact)
            } else {
                ReadyState(
                    instanceUrl = instanceUrl,
                    isDark = isDark,
                    isCompact = isCompact,
                    cachedResponse = cachedResponse
                )
            }
        }
    }

    @Composable
    private fun UnconfiguredState(isDark: Boolean, isCompact: Boolean) {
        val cxt = LocalContext.current
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = if (isCompact) cxt.getString(R.string.widget_title_compact)
                    else cxt.getString(R.string.widget_title_full),
                style = TextStyle(
                    color = ColorProvider(
                        if (isDark) WidgetColors.primaryDark else WidgetColors.primary
                    ),
                    fontSize = if (isCompact) 13.sp else 16.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
            Text(
                text = cxt.getString(R.string.tap_to_configure),
                style = TextStyle(
                    color = ColorProvider(
                        if (isDark) WidgetColors.onSurfaceDark else WidgetColors.onSurfaceLight
                    ),
                    fontSize = if (isCompact) 10.sp else 12.sp
                ),
                modifier = GlanceModifier.clickable(
                    actionStartActivity<MainActivity>()
                )
            )
        }
    }

    @Composable
    private fun ReadyState(
        instanceUrl: String,
        isDark: Boolean,
        isCompact: Boolean,
        cachedResponse: SearchResponse?
    ) {
        if (cachedResponse != null && cachedResponse.results.isNotEmpty()) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalAlignment = Alignment.Vertical.Top
            ) {
                SearchBar(instanceUrl = instanceUrl, isDark = isDark, isCompact = isCompact)
                Spacer(modifier = GlanceModifier.height(4.dp))
                cachedResponse.results.take(6).forEach { result ->
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 3.dp)
                            .clickable(
                                actionStartActivity<SearchOverlayActivity>()
                            ),
                        horizontalAlignment = Alignment.Horizontal.Start
                    ) {
                        Text(
                            text = result.title,
                            style = TextStyle(
                                color = ColorProvider(
                                    if (isDark) WidgetColors.primaryDark
                                    else WidgetColors.primary
                                ),
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                        if (!result.content.isNullOrBlank()) {
                            Text(
                                text = result.content,
                                style = TextStyle(
                                    color = ColorProvider(
                                        if (isDark) WidgetColors.onSurfaceDark
                                        else WidgetColors.onSurfaceLight
                                    ),
                                    fontSize = if (isCompact) 9.sp else 10.sp
                                ),
                                modifier = GlanceModifier.padding(top = 1.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                SearchBar(instanceUrl = instanceUrl, isDark = isDark, isCompact = isCompact)
            }
        }
    }
}

class SearxngWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SearxngWidget()
}
