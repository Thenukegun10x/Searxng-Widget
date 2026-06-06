package com.searxng.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionRunCallback
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionSetText
import androidx.glance.appwidget.state.PreferencesGlanceStateDefinition
import androidx.glance.currentState
import androidx.glance.layout.Align
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.searxng.widget.data.model.SearchResult
import com.searxng.widget.preferences.WidgetPrefs
import com.searxng.widget.receiver.OpenResultAction
import com.searxng.widget.receiver.SearchActionCallback
import com.searxng.widget.receiver.queryParam
import com.searxng.widget.ui.SearchBar
import com.searxng.widget.ui.SearchResults
import com.searxng.widget.ui.theme.WidgetBackground
import com.searxng.widget.ui.theme.WidgetColors
import com.searxng.widget.ui.theme.isDarkTheme

class SearxngWidget : GlanceAppWidget() {

    companion object {
        const val KEY_QUERY = "current_query"
        const val KEY_RESULTS_JSON = "results_json"

        private val gson = Gson()
    }

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val instanceUrl = WidgetPrefs(context).getInstanceUrl()
        provideContent {
            Content(instanceUrl = instanceUrl)
        }
    }

    @Composable
    fun Content(instanceUrl: String?) {
        val context = LocalContext.current
        val prefs = currentState()
        val isDark = isDarkTheme(context.resources.configuration)

        WidgetBackground(isDark = isDark) {
            if (instanceUrl.isNullOrBlank()) {
                UnconfiguredState(isDark)
            } else {
                ReadyState(prefs, isDark, instanceUrl)
            }
        }
    }

    @Composable
    private fun UnconfiguredState(isDark: Boolean) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Align.CenterVertically,
            horizontalAlignment = Align.CenterHorizontally
        ) {
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "SearXNG Widget",
                style = TextStyle(
                    color = ColorProvider(
                        if (isDark) WidgetColors.primaryDark else WidgetColors.primary
                    ),
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Tap to configure your instance",
                style = TextStyle(
                    color = ColorProvider(
                        if (isDark) WidgetColors.onSurfaceDark else WidgetColors.onSurfaceLight
                    ),
                    fontSize = 13.sp
                ),
                modifier = GlanceModifier.clickable(
                    actionStartActivity<MainActivity>()
                )
            )
        }
    }

    @Composable
    private fun ReadyState(
        prefs: androidx.glance.session.Session?,
        isDark: Boolean,
        instanceUrl: String
    ) {
        val query = prefs?.getString(KEY_QUERY, "") ?: ""
        val resultsJson = prefs?.getString(KEY_RESULTS_JSON, null)
        val results: List<SearchResult> = if (!resultsJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<SearchResult>>() {}.type
                gson.fromJson(resultsJson, type)
            } catch (_: Exception) {
                emptyList()
            }
        } else emptyList()

        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Align.CenterVertically
            ) {
                SearchBar(
                    query = query,
                    onValueChange = actionSetText(KEY_QUERY),
                    onSearch = actionRunCallback<SearchActionCallback>(
                        parameters = ActionParameters().apply {
                            queryParam = query
                        }
                    ),
                    isDark = isDark
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = "\u2699",
                    style = TextStyle(fontSize = 18.sp),
                    modifier = GlanceModifier.clickable(
                        actionStartActivity<MainActivity>()
                    )
                )
            }

            if (results.isNotEmpty()) {
                SearchResults(
                    results = results,
                    onResultClicked = { url ->
                        actionRunCallback<OpenResultAction>(
                            parameters = ActionParameters().apply {
                                OpenResultAction.urlParam = url
                            }
                        )
                    },
                    isDark = isDark
                )
            }
        }
    }
}

class SearxngWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SearxngWidget()
}
