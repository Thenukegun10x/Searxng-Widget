# SearXNG Android Widget

Privacy-respecting metasearch widget for Android home screen using SearXNG.

## Tech Stack

- **Language:** Kotlin 2.0+
- **Widget Framework:** Jetpack Glance 1.1+
- **Min SDK:** 26 / **Target SDK:** 35
- **Networking:** (not wired — widget uses WebView overlay for search via SearXNG instance directly)
- **Async:** Kotlin Coroutines + WorkManager
- **Preferences:** DataStore Preferences
- **Build:** Gradle 8.7+ / Kotlin DSL / Version Catalog (`libs.versions.toml`)
- **Testing:** JUnit 5 / Kotest / Mockk

## Build & Run

```powershell
# Build debug APK (use 5-min timeout — Gradle daemon may hang)
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

**Build timeouts:** Gradle builds can hang indefinitely (daemon issues, test executor stalls). Always set a 5-minute (300s) shell timeout when invoking Gradle commands from tooling.

## Project Structure

```
app/
├── src/main/java/com/searxng/widget/
│   ├── SearxngWidget.kt           # GlanceAppWidget + GlanceAppWidgetReceiver — renders the homescreen widget UI
│   ├── MainActivity.kt            # Compose activity for configuring instance URL and theme
│   │
│   ├── ui/
│   │   ├── theme/WidgetTheme.kt   # Light/dark widget color schemes + WidgetBackground composable
│   │   ├── SearchBar.kt           # Glance composable — the search pill on your homescreen
│   │   └── SearchOverlayActivity.kt # Transparent overlay with native search input, auto-focus, category tabs
│   │
│   ├── data/
│   │   └── model/
│   │       └── SearchResponse.kt  # Gson-mapped response models for the JSON API
│   │
│   ├── preferences/
│   │   └── WidgetPrefs.kt         # DataStore-backed preferences — instance URL, theme mode, auth token
│   │
│
├── src/main/res/
│   ├── drawable/                   # Widget preview, favicon, launcher icons
│   ├── values/strings.xml         # All user-facing strings for i18n
│   ├── values/colors.xml          # Color resources
│   ├── values/themes.xml          # App theme + translucent overlay theme + transparent theme
│   └── xml/searxng_widget_info.xml # Widget provider metadata (min size, update period, configure activity)
│
├── src/test/java/com/searxng/widget/
│   └── preferences/WidgetPrefsTest.kt   # DataStore read/write tests with in-memory store
│
├── build.gradle.kts               # App module — dependencies, JUnit 5, glance-testing
├── src/main/AndroidManifest.xml
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

1. **`SearxngWidget`** extends `GlanceAppWidget` — defines the `Content()` composable, renders SearchBar and inline cached results
2. **`SearxngWidgetReceiver`** extends `GlanceAppWidgetReceiver` — registers in manifest, sets widget info XML
3. **Actions:** Use `actionStartActivity` (open config/overlay) for user interactions
4. **Update flow:** Glance `update()` reads cached results from DataStore and renders in composable
5. **No lifecycle-aware state:** Widget reads DataStore synchronously in `provideGlance`; no `currentState()`/`updateState()` used

## Testing

- Unit tests in `src/test/` using JUnit 5
- Mock API calls with Mockk
- Verify Glance rendering with `runGlanceAppWidgetUnitTest` (glance-testing)
- Test DataStore with `runTest` + in-memory DataStore
- Name tests: `Subject_action_expectedBehavior` (e.g. `SearchRepository_search_returnsResults()`)

## Code Review Agents

Allocate the following agents to review the codebase in parallel:

| Agent | Area | Files |
|-------|------|-------|
| **Data Layer Agent** | API, models, networking, repository | `data/api/`, `data/model/`, `data/repository/` |
| **UI Layer Agent** | Widget composables, theming, layout | `ui/`, `SearxngWidget.kt`, `MainActivity.kt` |
| **Config & Prefs Agent** | DataStore, widget info, manifest, resources | `preferences/`, `res/`, `AndroidManifest.xml` |
| **Testing Agent** | Unit tests, coverage, test patterns | `src/test/` |
| **Build Agent** | Gradle config, version catalog, dependencies | `build.gradle.kts`, `libs.versions.toml`, `settings.gradle.kts` |

**Review checklist (each agent):**
- Verify architecture follows conventions in this doc
- Check for missing error/edge-case handling
- Identify unused imports, dead code, or redundancy
- Confirm correct use of APIs (Retrofit, Glance, DataStore)
- Look for thread-safety issues (coroutine context, main-safety)
- Ensure theme/dark-mode consistency
- Flag hardcoded strings that belong in `strings.xml`
- Verify test coverage for all public functions

## Important Rules

- Never commit API keys or secrets — SearXNG instances are user-configured, no hardcoded tokens
- Keep the widget simple — single search bar + results list, no tabs/fragments inside the widget
- Always handle empty state (no instance configured, no results, network error)
- Limit widget update frequency to avoid battery drain (respect `updatePeriodMillis` minimum of 30 min)
- Use `LocalSize` in Glance composables to adapt to different widget sizes
- Support both light and dark themes — never hardcode colors without theme reference
- All user-facing strings must go in `strings.xml` for potential i18n
- Follow Material 3 color tokens for theming
