# SearXNG Android Widget

Privacy-respecting metasearch widget for Android home screen using SearXNG.

## Tech Stack

- **Language:** Kotlin 2.0+
- **Widget Framework:** Jetpack Glance 1.1+
- **Min SDK:** 26 / **Target SDK:** 35
- **Networking:** Retrofit 2.11+ / OkHttp 4.12+
- **Async:** Kotlin Coroutines + WorkManager
- **Preferences:** DataStore Preferences
- **Build:** Gradle 8.7+ / Kotlin DSL / Version Catalog (`libs.versions.toml`)
- **Testing:** JUnit 5 / Kotest / Mockk

## Build & Run

```powershell
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test

# Run lint
./gradlew lint

# Check for dependency updates
./gradlew dependencyUpdates
```

## Project Structure

```
app/
├── src/main/java/com/searxng/widget/
│   ├── SearxngWidget.kt           # GlanceAppWidget + GlanceAppWidgetReceiver
│   ├── MainActivity.kt            # Configuration screen
│   │
│   ├── ui/
│   │   ├── theme/WidgetTheme.kt   # Light/dark color schemes
│   │   ├── SearchBar.kt           # Glance search input composable
│   │   └── SearchResults.kt       # Glance results list composable
│   │
│   ├── data/
│   │   ├── api/
│   │   │   ├── SearxngApi.kt      # Retrofit interface
│   │   │   └── ApiClient.kt       # OkHttp client with configurable base URL
│   │   ├── model/
│   │   │   ├── SearchQuery.kt     # Request params
│   │   │   └── SearchResponse.kt  # JSON API response mapping
│   │   └── repository/
│   │       └── SearchRepository.kt
│   │
│   ├── preferences/
│   │   └── WidgetPrefs.kt         # DataStore for instance URL, theme mode
│   │
│   └── receiver/
│       └── WidgetActions.kt       # Glance action callbacks
│
├── src/main/res/
│   ├── drawable/                   # Widget preview icon
│   ├── values/strings.xml
│   ├── values/colors.xml
│   └── xml/searxng_widget_info.xml
│
├── src/test/java/com/searxng/widget/
│   ├── data/api/SearxngApiTest.kt
│   ├── data/repository/SearchRepositoryTest.kt
│   └── preferences/WidgetPrefsTest.kt
│
├── build.gradle.kts
└── src/main/AndroidManifest.xml
```

## SearXNG JSON API

**Endpoint:** `{instance_url}/search?format=json&q={query}&categories={cats}`

**Key response fields:**
```
{
  "query": "...",
  "results": [
    {
      "title": "Result Title",
      "url": "https://...",
      "content": "Snippet text...",
      "engine": "google",
      "category": "general",
      "parsed_url": { "parts": [...], "url": "..." }
    }
  ],
  "answers": [],
  "infoboxes": [],
  "suggestions": ["related", "queries"],
  "unresponsive_engines": []
}
```

**Useful params:** `categories` (general, images, news, etc.), `language` (auto by default), `pageno` (pagination), `time_range` (day, week, month, year).

## Code Conventions

- **Naming:** `camelCase` for functions/vals, `PascalCase` for classes, `UPPER_SNAKE_CASE` for constants
- **Formatting:** Kotlin official style (ktfmt or ktlint with standard rules)
- **Glance composables:** `@Composable` functions prefixed with the component name (e.g. `SearchBar`, `ResultRow`)
- **State hoisting:** Widget state managed via `currentState()` / `updateState()` in Glance composables
- **No Hilt/DI library** — keep it manual with simple constructor injection or a small `ServiceLocator`
- **No AndroidX Navigation** — just a single configuration Activity with Compose View
- **No Jetpack Compose in the widget** — Glance DSL only (Glance is NOT Jetpack Compose)
- **Dark mode:** Detect via `LocalContext.current.resources.configuration.uiMode`; store override in DataStore as enum (`SYSTEM`, `LIGHT`, `DARK`)

## Widget Architecture (Jetpack Glance)

1. **`SearxngWidget`** extends `GlanceAppWidget` — defines the `Content()` composable with `LazyColumn`, search input, results rendering
2. **`SearxngWidgetReceiver`** extends `GlanceAppWidgetReceiver` — registers in manifest, sets widget info XML
3. **Actions:** Use `actionStartActivity` (open config), `actionRunCallback<SearchActionCallback>` (execute search), `actionStartActivity<ResultActivity>` (open result in browser via URI)
4. **Update flow:** Glance `update()` is triggered by receiver → fetches results via coroutine → renders in composable
5. **WorkManager:** Optional for background periodic refresh of recent searches

## Testing

- Unit tests in `src/test/` using JUnit 5
- Mock API calls with Mockk
- Verify Glance rendering with `runGlanceAppWidgetUnitTest` (glance-testing)
- Test DataStore with `runTest` + in-memory DataStore
- Name tests: `Subject_action_expectedBehavior` (e.g. `SearchRepository_search_returnsResults()`)

## Important Rules

- Never commit API keys or secrets — SearXNG instances are user-configured, no hardcoded tokens
- Keep the widget simple — single search bar + results list, no tabs/fragments inside the widget
- Always handle empty state (no instance configured, no results, network error)
- Limit widget update frequency to avoid battery drain (respect `updatePeriodMillis` minimum of 30 min)
- Use `LocalSize` in Glance composables to adapt to different widget sizes
- Support both light and dark themes — never hardcode colors without theme reference
- All user-facing strings must go in `strings.xml` for potential i18n
- Follow Material 3 color tokens for theming
