package com.searxng.widget

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
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "SearXNG Widget",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = instanceUrl,
                            onValueChange = { instanceUrl = it },
                            label = { Text("Instance URL") },
                            placeholder = { Text("https://searxng.example.com") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    scope.launch {
                                        saveAndClose(prefs, instanceUrl, selectedTheme)
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
                                    saveAndClose(prefs, instanceUrl, selectedTheme)
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

    private suspend fun saveAndClose(
        prefs: WidgetPrefs,
        url: String,
        theme: WidgetPrefs.ThemeMode
    ) {
        if (url.isNotBlank()) {
            val normalized = url.trimEnd('/')
            prefs.setInstanceUrl(normalized)
        }
        prefs.setThemeMode(theme)
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
