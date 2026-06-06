# SearXNG Widget

A homescreen widget for Android that puts SearXNG search right on your desktop. Tap the widget, type your query, and results load in an in-app WebView — no Chrome, no browser swap, just SearXNG.

## What it does

This is basically a single-purpose widget: one tap, one search bar, straight to your SearXNG instance. I got tired of opening a browser, typing the URL, waiting for the page to load, and *then* typing my search. Now I just tap the widget and go.

It'll keep whatever SearXNG instance you configure — public one, your own self-hosted one, doesn't matter. And since results open in a WebView inside the app, you're never touching Chrome or any other browser at all.

## How it looks

The widget itself is just a search pill on your homescreen. Tapping it brings up a transparent overlay with:

- The SearXNG title
- A search bar that matches the SearXNG look (colors pulled right from the CSS)
- Category chips underneath (General, Images, Videos, etc.)
- Your instance URL as a subtle hint

Hit search and the overlay transitions to a full-screen WebView showing your results. Press back to return to the search bar with your query still intact.

It works in light and dark mode — the overlay respects your saved theme preference (System/Light/Dark), and the WebView gets dark mode treatment too.

## Installing

You can either:

- **Build from source**: `./gradlew installDebug`
- **Grab the APK from Releases** (signed with the debug key)

Requires Android 8.0 (API 26) or newer.

## Setting it up

1. Add the widget to your homescreen (long-press → Widgets → SearXNG Widget)
2. Tap "Tap to configure" or open the app
3. Enter your SearXNG instance URL (e.g. `https://searxng.example.com`)
4. Pick your theme mode (System/Light/Dark)
5. Hit Save & Close

The widget will show "Search for..." — tap it, type, and you're searching.

## The tech stuff

Built because I wanted to see if Jetpack Glance could actually work for something useful. Turns out it can, but with limitations (no text input in widgets, hence the overlay activity).

- Kotlin 2.0+ / Jetpack Glance 1.1+ for the widget
- Compose + Material3 for the overlay/search UI
- In-app WebView for search results (no browser, no Chrome — it's SearXNG, after all)
- DataStore for preferences
- Retrofit + OkHttp for the API layer (built but not wired into the widget yet — future todo)

## Things that could be better

- No search history or recent searches in the widget (Glance doesn't make that easy)
- The category tabs look nice but aren't saved per-search
- API layer exists but isn't connected to the widget UI yet — I wanted to get the UX right first

## License

MIT. Do whatever you want with it.
