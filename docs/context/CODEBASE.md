# Cachely – Codebase Reference

Quick lookup: path → purpose → do not. Use with [CONTEXT.md](CONTEXT.md).

| Path | Purpose | Do not |
|------|---------|--------|
| `CachelyApplication.kt` | App entry; minimal config | Business logic, permissions, threading |
| `MainActivity.kt` | Compose setContent, CachelyTheme, NavGraph | — |
| `accessibility/AccessibilityHelper.kt` | Is service enabled; open system Accessibility settings | UI automation, node trees, cleaning logic |
| `accessibility/CachelyAccessibilityService.kt` | On App Info: find "Clear cache", click, back; notifyCleared() | Screen reading, extraction, storage, background |
| `accessibility/CleanCoordinator.kt` | Channel: service signals cleared; CacheCleaner awaits | — |
| `data/CacheCleaner.kt` | Orchestrate clean; assisted vs manual; return CleaningResult | UI, node traversal, persistence, logging |
| `data/CleaningResult.kt` | Model: totalBytesFreed, appsCleaned, appsSkipped | — |
| `data/PreferencesRepository.kt` | DataStore: assisted preferred (Flow + set) | — |
| `ui/HomeScreen.kt` | CTA, state, last cleaned, result, Settings link | Call system APIs, perform cleaning, handle permissions |
| `ui/HomeViewModel.kt` | HomeUiState, startCleaning(), setAssistedEnabled() | — |
| `ui/SettingsScreen.kt` | Toggle, Configure → Permission, transparency text | — |
| `ui/SettingsViewModel.kt` | Load/save assisted preferred via PreferencesRepository | — |
| `ui/PermissionScreen.kt` | Explain Accessibility; Enable (opens settings) / Not now | Coercion |
| `ui/theme/Color.kt` | Semantic colors (BackgroundDark, SurfaceDark, Accent, …) | — |
| `ui/theme/Theme.kt` | CachelyTheme (dark Material3) | — |
| `navigation/NavGraph.kt` | Routes HOME, SETTINGS, PERMISSION; ViewModels, composables | Deep links, conditional flows |
| `util/ByteFormatter.kt` | Format bytes → KB/MB/GB, locale-safe | — |
| `util/TimeFormatter.kt` | Relative time (e.g. "2 hours ago") | Timers, background |

**Res (summary):**

| Path | Purpose |
|------|---------|
| `res/values/strings.xml` | app_name, accessibility_service_description |
| `res/values/themes.xml` | Theme.Cachely (dark) |
| `res/xml/accessibility_service_config.xml` | Service config, description ref |
| `res/drawable/ic_launcher_foreground.xml` | Launcher icon |
