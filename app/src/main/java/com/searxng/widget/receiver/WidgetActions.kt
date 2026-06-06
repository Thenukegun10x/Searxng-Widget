package com.searxng.widget.receiver

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.action.ActionCallback
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.state.updateAppWidgetState
import com.searxng.widget.SearxngWidget
import com.searxng.widget.data.api.AppGson
import com.searxng.widget.data.repository.SearchRepository
import com.searxng.widget.preferences.WidgetPrefs

val queryParam = ActionParameters.Key<String>("query")

class SearchActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        action: String,
        intent: Intent?
    ) {
        val query = ActionParameters.from(intent)[queryParam] ?: return
        if (query.isBlank()) return

        val prefs = WidgetPrefs(context)
        val instanceUrl = prefs.getInstanceUrl() ?: return
        val authToken = prefs.getAuthToken()

        updateAppWidgetState(context, glanceId) { state ->
            state[SearxngWidget.KEY_WIDGET_STATE] = SearxngWidget.STATE_LOADING
            state[SearxngWidget.KEY_QUERY] = query
        }
        SearxngWidget().update(context, glanceId)

        val repository = SearchRepository(instanceUrl, authToken)
        val result = repository.search(query)

        updateAppWidgetState(context, glanceId) { state ->
            state[SearxngWidget.KEY_QUERY] = query
            result.onSuccess { results ->
                state[SearxngWidget.KEY_WIDGET_STATE] = SearxngWidget.STATE_RESULTS
                state[SearxngWidget.KEY_RESULTS_JSON] = AppGson.instance.toJson(results)
            }.onFailure { error ->
                state[SearxngWidget.KEY_WIDGET_STATE] = SearxngWidget.STATE_ERROR
                state[SearxngWidget.KEY_ERROR_MESSAGE] = error.message ?: "Search failed"
            }
        }
        SearxngWidget().update(context, glanceId)
    }
}

class OpenResultAction : ActionCallback {

    companion object {
        val urlParam = ActionParameters.Key<String>("url")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        action: String,
        intent: Intent?
    ) {
        val url = ActionParameters.from(intent)[urlParam] ?: return

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    }
}
