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
│   │   ├── SearchBar.kt           # Glance search bar composable
│   │   ├── SearchOverlayActivity.kt # Transparent overlay with native search input
│   │   └── SearchActivity.kt       # [deprecated] Redirect activity (kept for reference)
│   │
│   ├── data/
│   │   ├── api/
│   │   │   ├── SearxngApi.kt      # Retrofit interface
│   │   │   └── ApiClient.kt       # OkHttp client with configurable base URL
│   │   ├── model/
│   │   │   └── SearchResponse.kt  # JSON API response mapping
│   │   └── repository/
│   │       └── (empty)            # Not yet implemented
│   │
│   ├── preferences/
│   │   └── WidgetPrefs.kt         # DataStore for instance URL, theme mode
│   │
│   └── receiver/
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

## Code Review Results (June 2026)

### Data Layer — `data/api/`, `data/model/`, `data/repository/`

**Critical:** No `SearchRepository.kt` exists — API is never wired to the widget. `SearchQuery.kt` also missing despite being documented.

**High:**
- `SearxngApi.kt:10-17` — `search()` returns raw `SearchResponse`, no error handling for network failures or HTTP errors. Should wrap in `Result<T>` or `Response<T>`.
- `SearchResponse.kt:8-12` — `answers`, `infoboxes`, `suggestions`, `unresponsiveEngines` were nullable (now fixed — non-null with `emptyList()` defaults).

**Medium:**
- `ApiClient.kt:19-42` — No URL validation (now fixed — added `require()` for `http://`/`https://` scheme check).
- `ApiClient.kt:35` — Trailing slash normalization correct but diverges from `MainActivity` which strips trailing slash before storage.

**Low:** Missing `questions`, `metadata`, `tags` API fields not mapped. No logging interceptor.

### UI Layer — `ui/`, `SearxngWidget.kt`, `MainActivity.kt`

**Critical:**
- `ui/SearchResults.kt` — Does not exist. Widget has no results list, loading state, empty state, or error state.
- `receiver/WidgetActions.kt` — Does not exist. No search action callbacks implemented.
- `SearxngWidget.kt:54-57` — `ReadyState` only renders a `SearchBar`, no API calls are executed from the widget.

**High:**
- `SearchBar.kt:42,52` — Hardcoded strings `"SearXNG"` and `"Open SearXNG"` should reference `strings.xml`.
- `SearxngWidget.kt:72,82` — Hardcoded strings `"SearXNG Widget"` and `"Tap to configure"`.
- `MainActivity.kt:60,71,72,75,93,118` — Six hardcoded strings when `strings.xml` resources exist.

**Medium:**
- `SearchBar.kt:25-27` — All colors hardcoded (`0xFF3C3C3C`, `0xFFF0F0F0`, etc.) instead of using `WidgetColors`.
- `widget_initial.xml:7,15` — Hardcoded `#FFFFFF` background, `#888888` text — no dark variant.
- `search_pill.xml:4` — Hardcoded `#F0F0F0` color.
- `MainActivity.kt:163-170` — `darkColorScheme()` uses default M3 colors (brand blue `#0057B7` lost in dark mode).
- `SearxngWidget.kt:33-39` — State passed via parameters instead of `currentState()`/`updateState()`.

**Low:** `strings.xml:16-19` — `no_results`, `error_network`, `error_config`, `loading` defined but never referenced.

### Config & Prefs — `preferences/`, `res/`, `AndroidManifest.xml`

**High:**
- `WidgetPrefs.kt` — All DataStore reads/writes lacked `IOException` handling (now fixed — added try-catch and `.catch {}`).

**Medium:**
- `WidgetPrefs.kt:20,45-52` — `authToken` getter/setter/key defined but never called by any UI code.
- `strings.xml:6-22` — 15 of 23 string resources defined but never used, while `MainActivity.kt` hardcodes the same strings.
- `themes.xml:3` — Uses Material 2 (`android:Theme.Material.Light.NoActionBar`) instead of Material 3.
- `colors.xml:3-14` — Missing Material 3 color tokens (`error`, `tertiary`, `outline`, etc.), uses `_dark` suffix instead of `values-night/`.

**Low:**
- `WidgetTheme.kt:16` — `primaryDark` value (`0xFF4FC3F7`) is lighter than `primary` (`0xFF0057B7`) — misleading naming.
- `colors.xml:13-14` — `widget_background` duplicates `background_light`; `widget_background_dark` duplicates `background_dark`.

### Testing — `src/test/`

**Critical:**
- `app/build.gradle.kts` — `useJUnitPlatform()` not configured (now fixed). `junit-platform-launcher` missing (now added).
- `glance-testing` dependency missing entirely — widget unit tests impossible to write (now added to catalog).

**High:**
- Only 2 of ~10 source files have any tests. `ApiClient`, `SearxngWidget`, `MainActivity.saveIfValid()`, `SearchBar` have zero coverage.
- No tests for error/edge cases (network exceptions, null responses, corrupted DataStore).
- `SearxngApiTest.kt` — Tests use `mockk<SearchResponse>()` tautological mock instead of real objects (now fixed).

**Medium:**
- `SearxngApiTest.kt` — All test names deviate from `Subject_action_expectedBehavior` convention (now fixed).
- `SearxngApiTest.kt:47` — Redundant `response.results shouldNotBe null` on non-nullable type (now removed).
- `WidgetPrefsTest.kt:13-21` — Uses disk-backed DataStore with no cleanup (now fixed — in-memory with `@AfterEach` cleanup).
- `WidgetPrefsTest.kt` — No tests for `authToken`, `instanceUrl` Flow, `themeMode` Flow, or invalid stored values (now added).

**Low:** `WidgetPrefsTest.kt:5-9` — Imports clean, no unused imports.

### Build — `build.gradle.kts`, `libs.versions.toml`, `settings.gradle.kts`

**Medium:**
- `app/build.gradle.kts:64-67` — Missing `junit-platform-launcher` for JUnit 5 test execution (now fixed).
- `app/build.gradle.kts:64-67` — Missing `glance-testing` dependency for widget unit testing (now fixed).
- `libs.versions.toml` — WorkManager dependency referenced in architecture docs but not declared in catalog.

**Low:**
- `app/build.gradle.kts:21` — Release minification disabled (`isMinifyEnabled = false`).
- `app/build.gradle.kts:7-38` — No explicit lint configuration block.

**Info:** All versions are modern and compatible (AGP 8.7.3, Kotlin 2.0.21, Glance 1.1.1, Retrofit 2.11.0). Gradle Kotlin DSL conventions properly followed. ProGuard rules correctly keep Retrofit/Gson classes.

## Important Rules

- Never commit API keys or secrets — SearXNG instances are user-configured, no hardcoded tokens
- Keep the widget simple — single search bar + results list, no tabs/fragments inside the widget
- Always handle empty state (no instance configured, no results, network error)
- Limit widget update frequency to avoid battery drain (respect `updatePeriodMillis` minimum of 30 min)
- Use `LocalSize` in Glance composables to adapt to different widget sizes
- Support both light and dark themes — never hardcode colors without theme reference
- All user-facing strings must go in `strings.xml` for potential i18n
- Follow Material 3 color tokens for theming
