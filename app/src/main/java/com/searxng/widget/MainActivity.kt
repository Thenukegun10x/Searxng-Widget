package com.searxng.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                            text = "SearXNG Widget",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = instanceUrl,
                            onValueChange = {
                                instanceUrl = it
                                urlError = false
                            },
                            label = { Text("Instance URL") },
                            placeholder = { Text("https://searxng.example.com") },
                            isError = urlError,
                            supportingText = if (urlError) {
                                { Text("Enter a valid URL starting with http:// or https://") }
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
                            text = "Theme",
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
                            Text("Save & Close")
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
        if (trimmed.isNotBlank()) {
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
