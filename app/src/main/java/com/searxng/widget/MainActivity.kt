package com.searxng.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.searxng.widget.preferences.WidgetPrefs
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = WidgetPrefs(this)

        setContent {
            val scope = rememberCoroutineScope()

            var instanceUrl by remember { mutableStateOf("") }
            var selectedTheme by remember { mutableStateOf(WidgetPrefs.ThemeMode.SYSTEM) }
            var urlError by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                instanceUrl = prefs.getInstanceUrl() ?: ""
                selectedTheme = prefs.getThemeMode()
            }

            val isDark = when (selectedTheme) {
                WidgetPrefs.ThemeMode.LIGHT -> false
                WidgetPrefs.ThemeMode.DARK -> true
                WidgetPrefs.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            SearxngWidgetTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = instanceUrl,
                            onValueChange = {
                                instanceUrl = it
                                urlError = false
                            },
                            label = { Text(stringResource(R.string.instance_url_label)) },
                            placeholder = { Text(stringResource(R.string.instance_url_hint)) },
                            isError = urlError,
                            supportingText = if (urlError) {
                                { Text(stringResource(R.string.invalid_url)) }
                            } else null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    scope.launch {
                                        saveIfValid(prefs, instanceUrl, selectedTheme) { urlError = it }
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = stringResource(R.string.theme_label),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WidgetPrefs.ThemeMode.entries.forEach { theme ->
                                FilterChip(
                                    selected = selectedTheme == theme,
                                    onClick = { selectedTheme = theme },
                                    label = {
                                        Text(theme.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                scope.launch {
                                    saveIfValid(prefs, instanceUrl, selectedTheme) { urlError = it }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.save_close))
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveIfValid(
        prefs: WidgetPrefs,
        url: String,
        theme: WidgetPrefs.ThemeMode,
        onError: (Boolean) -> Unit
    ) {
        val trimmed = url.trim()
        if (trimmed.isNotBlank() && !trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            onError(true)
            return
        }
        if (trimmed.isBlank()) {
            prefs.clearInstanceUrl()
        } else {
            prefs.setInstanceUrl(trimmed.trimEnd('/'))
        }
        prefs.setThemeMode(theme)
        GlanceAppWidgetManager(this@MainActivity)
            .getGlanceIds(SearxngWidget::class.java)
            .forEach { id -> SearxngWidget().update(this@MainActivity, id) }

        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

@Composable
fun SearxngWidgetTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF0057B7),
            secondary = androidx.compose.ui.graphics.Color(0xFF4FC3F7)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
