# Cachely – Development Context

**One-line:** Minimal, premium Android cache cleaner — one tap to clean app cache, optional Accessibility-assisted flow, manual fallback. No ads, no trackers, no background.

**Stack:** Kotlin · Jetpack Compose · Lean MVVM · minSdk 26, targetSdk 35

---

## 1. Document Map

| Document | Use when |
|----------|----------|
| [CONTEXT.md](CONTEXT.md) (this file) | Starting work, onboarding, AI context |
| [CODEBASE.md](CODEBASE.md) | Finding where to change something |
| [TDD.md](../TDD.md) | Implementation details, APIs, constraints |
| [PRD.md](../PRD.md) | Scope, metrics, Play Store, QA |
| [UI_UX.md](../UI_UX.md) | Colors, typography, motion, screens |
| [RELEASE_CHECKLIST.md](../RELEASE_CHECKLIST.md) | Pre–store submission |
| [.cursor/rules/rule-1.mdc](../../.cursor/rules/rule-1.mdc) | Non-negotiable AI guardrails |

---

## 2. Package & File Map

```
app/src/main/java/com/cachely/app/
├── CachelyApplication.kt    # App entry; no business logic
├── MainActivity.kt         # Compose setContent → NavGraph
├── accessibility/
│   ├── AccessibilityHelper.kt       # isEnabled(), openSettings()
│   ├── CachelyAccessibilityService.kt # Detect App Info, click Clear cache, back
│   └── CleanCoordinator.kt          # notifyCleared() / awaitCleared() app ↔ service
├── data/
│   ├── CacheCleaner.kt     # cleanCache(): assisted or manual; returns CleaningResult
│   ├── CleaningResult.kt   # totalBytesFreed, appsCleaned, appsSkipped
│   └── PreferencesRepository.kt    # DataStore: assisted preferred
├── ui/
│   ├── HomeScreen.kt       # CTA, state, result; no system APIs
│   ├── HomeViewModel.kt    # state, startCleaning(), setAssistedEnabled()
│   ├── SettingsScreen.kt   # Toggle, Configure, transparency
│   ├── SettingsViewModel.kt
│   ├── PermissionScreen.kt  # Explain Accessibility, Enable / Not now
│   └── theme/
│       ├── Color.kt        # BackgroundDark, SurfaceDark, Accent, etc.
│       └── Theme.kt        # CachelyTheme (dark)
├── navigation/
│   └── NavGraph.kt         # Routes: HOME, SETTINGS, PERMISSION
└── util/
    ├── ByteFormatter.kt   # Bytes → KB/MB/GB
    └── TimeFormatter.kt    # Relative time
```

---

## 3. Key Contracts

**CacheCleaner (single public API):**
```kotlin
suspend fun cleanCache(): CleaningResult
```

**CleaningResult:**
```kotlin
data class CleaningResult(totalBytesFreed: Long, appsCleaned: Int, appsSkipped: Int)
```

**HomeUiState:**
```kotlin
data class HomeUiState(
  isCleaning: Boolean, lastCleaned: String?, assistedEnabled: Boolean, result: CleaningResult?
)
```

**Threading:** Cleaning off main thread; UI on main; ViewModel-scoped coroutines only; no global scopes.

---

## 4. Data Flow (Clean Cache)

1. User taps "Clean cache" → **HomeViewModel.startCleaning()**
2. **CacheCleaner.cleanCache()** → checks **AccessibilityHelper.isAccessibilityServiceEnabled()**
3. If enabled: for each app, open App Info → **CachelyAccessibilityService** finds "Clear cache", clicks, back → **CleanCoordinator.notifyCleared()** → next app.
4. If disabled: open system app list (manual flow).
5. **CleaningResult** → ViewModel updates state → UI shows result.

**Rule:** UI never knows how cleaning happens, only the result.

---

## 5. Conventions (Must Follow)

- **Structure:** Do not add/rename packages; keep files under the paths in §2.
- **UI:** State-driven; no business logic or blocking calls in composables. Use semantic colors from `ui/theme/Color.kt`; system font only; motion ≤ 300 ms; calm, no gamification.
- **No:** Ads, analytics, background work, auto-clean, RAM/CPU/battery features, SF Symbols or Apple-copied UI.
- **Accessibility:** Optional, user-initiated, transparent; no screen reading, extraction, or storage.

---

## 6. Build & Run

| Action | Command |
|--------|---------|
| Local build | `.\build.ps1` or `build.bat` or `./build.sh` (needs Gradle wrapper: `gradle wrapper --gradle-version=8.9`) |
| CI build | Push to `dev` → [.github/workflows/build.yml](../../.github/workflows/build.yml); APK artifact: `app-debug.apk` |
| Open project | Android Studio → Open `cachely` folder |

---

## 7. Where to Change What

| Goal | File(s) |
|------|---------|
| Clean flow / orchestration | `data/CacheCleaner.kt` |
| Accessibility detection / click | `accessibility/CachelyAccessibilityService.kt`, `AccessibilityHelper.kt` |
| Home UI / CTA / result | `ui/HomeScreen.kt`, `ui/HomeViewModel.kt` |
| Settings / assisted toggle | `ui/SettingsScreen.kt`, `ui/SettingsViewModel.kt`, `data/PreferencesRepository.kt` |
| Permission explanation | `ui/PermissionScreen.kt` |
| Navigation / routes | `navigation/NavGraph.kt` |
| Theme / colors | `ui/theme/Color.kt`, `ui/theme/Theme.kt` |
| Format bytes / time | `util/ByteFormatter.kt`, `util/TimeFormatter.kt` |
