# Cachely – Codebase Reference

Quick lookup: path → purpose → do not. Use with [CONTEXT.md](CONTEXT.md).

| Path | Purpose | Do not |
|------|---------|--------|
| `CachelyApplication.kt` | App entry; minimal config | Business logic, permissions, threading |
| `MainActivity.kt` | Compose setContent, CachelyTheme, NavGraph | — |
| `accessibility/AccessibilityHelper.kt` | Is service enabled; open system Accessibility settings | UI automation, node trees, cleaning logic |
| `accessibility/CachelyAccessibilityService.kt` | On App Info: find "Clear cache", click, back; notifyCleared() | Screen reading, extraction, storage, background |
| `accessibility/CleanCoordinator.kt` | Channel: service signals cleared; CacheCleaner awaits | — |
| `data/AppCacheItem.kt` | Model: appName, packageName, approxCacheBytes, isSystemApp | — |
| `data/AppScanner.kt` | Scan installed apps; filter (system/zero cache); sort by cache size | UI, persistence |
| `data/CacheCleaner.kt` | Orchestrate clean for selected packages; progress callback; assisted vs manual | UI, node traversal, persistence, logging |
| `data/CleaningProgress.kt` | Model: currentIndex, totalApps, currentAppName | — |
| `data/CleaningResult.kt` | Model: totalBytesFreed, appsCleaned, appsSkipped | — |
| `data/PreferencesRepository.kt` | DataStore: assisted preferred (Flow + set) | — |
| `ui/HomeScreen.kt` | App list, selection toggles, Clean Selected, progress, result summary | Call system APIs, perform cleaning, handle permissions |
| `ui/HomeViewModel.kt` | HomeUiState, startCleaning(), setAssistedEnabled() | — |
| `ui/SettingsScreen.kt` | Toggle, Configure → Permission, transparency text | — |
| `ui/SettingsViewModel.kt` | Load/save assisted preferred via PreferencesRepository | — |
| `ui/PermissionScreen.kt` | Explain Accessibility; Enable (opens settings) / Not now | Coercion |
| `ui/theme/Color.kt` | Semantic colors (BackgroundDark, SurfaceDark, Accent, …) | — |
| `ui/theme/Theme.kt` | CachelyTheme (dark Material3) | — |
| `navigation/NavGraph.kt` | Root Scaffold + Bottom Nav (Home, Settings); routes HOME, SETTINGS, PERMISSION | Deep links |
| `util/ByteFormatter.kt` | Format bytes → KB/MB/GB, locale-safe | — |
| `util/TimeFormatter.kt` | Relative time (e.g. "2 hours ago") | Timers, background |

**Res (summary):**

| Path | Purpose |
|------|---------|
| `res/values/strings.xml` | app_name, accessibility_service_description |
| `res/values/themes.xml` | Theme.Cachely (dark) |
| `res/xml/accessibility_service_config.xml` | Service config, description ref |
| `res/drawable/ic_launcher_foreground.xml` | Launcher icon |
