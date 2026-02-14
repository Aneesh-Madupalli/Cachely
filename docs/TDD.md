# Cachely – Technical Design Document (TDD)

**Document type:** Technical Design Document (architecture and implementation spec).  
**Product name:** Cachely  
**Platform:** Android  
**Language:** Kotlin  
**UI framework:** Jetpack Compose  
**Architecture:** Lean MVVM (pragmatic)  
**Version:** v1 (Initial Release)  
**Status:** Final – approved for development  

---

## 1. Purpose of This Document

This document is the **single source of truth** for implementing Cachely v1.

It consolidates:

- Product vision and v1 scope  
- Confirmed project structure  
- Technical design (components, flows, constraints)  
- Accessibility rules and compliance  
- UI, performance, testing, and release readiness  

It is **explicit, conservative, and audit-friendly**.

---

## 2. Product Summary (Context)

Cachely is a **minimal, premium Android cache cleaner** with one core capability:

> Safely and transparently cleaning application cache.

**Provides:**

- Manual cache cleaning (via system App Info screens)  
- Optional **Accessibility-assisted cleaning** for speed and usability  

**Explicitly avoids:**

- Fake optimizations (RAM booster, CPU cooler, battery saver)  
- Background automation  
- Ads or analytics (v1)  
- Any form of user data collection  

---

## 3. Core Product & Engineering Principles

1. **One action, one purpose**  
2. **User trust over automation**  
3. **Accessibility as an engine, not a feature**  
4. **No background behavior**  
5. **Minimal files, clear responsibilities**  
6. **Play Store safety > technical cleverness**  

Any implementation that violates these principles **must not ship**.

---

## 4. v1 Feature Scope (Locked)

### 4.1 Included

- Home screen with one-tap cache cleaning  
- Settings screen  
- Accessibility explanation screen  
- Optional Accessibility-assisted cleaning  
- Manual fallback cleaning  
- Cleaning result summary  
- Last cleaned timestamp  

### 4.2 Explicitly Excluded

- Ads  
- Analytics  
- Monetization  
- Background services  
- Auto-clean scheduling  
- RAM / CPU / Battery features  
- Storage graphs or dashboards  

---

## 5. v1 Project Structure (Final)

```text
com.cachely.app
│
├── CachelyApplication.kt
│
├── accessibility/
│   ├── CachelyAccessibilityService.kt
│   └── AccessibilityHelper.kt
│
├── data/
│   └── CacheCleaner.kt
│
├── ui/
│   ├── HomeScreen.kt
│   ├── HomeViewModel.kt
│   ├── SettingsScreen.kt
│   └── PermissionScreen.kt
│
├── navigation/
│   └── NavGraph.kt
│
└── util/
    ├── ByteFormatter.kt
    └── TimeFormatter.kt
```

**Rationale:**

- Smallest safe structure  
- Easy to audit and reason about  
- No over-engineering  
- Clean upgrade path post-v1  

---

## 6. Application Lifecycle

### 6.1 CachelyApplication.kt

**Responsibilities:**

- Application startup  
- Minimal global configuration  

**Constraints:**

- No business logic  
- No permission logic  
- No background work  
- No threading logic  

This class must remain **boring and predictable**.

---

## 7. High-Level System Flow

```text
User taps "Clean Cache"
        ↓
HomeViewModel
        ↓
CacheCleaner
        ↓
┌─────────────────────────┐
│ Accessibility enabled   │ → Accessibility Service
└─────────────────────────┘
┌─────────────────────────┐
│ Accessibility disabled  │ → Manual App Info flow
└─────────────────────────┘
        ↓
CleaningResult
        ↓
UI update
```

**Rule:** The UI never knows *how* cleaning happens — only *what* the result is.

---

## 8. Core Logic Layer

### 8.1 CacheCleaner.kt

**Purpose:** Single orchestration point for all cache cleaning operations.

**Responsibilities:**

- Check Accessibility availability  
- Select assisted or manual execution path  
- Coordinate cleaning  
- Aggregate results  
- Return summary  

**Must NOT:**

- Contain UI code  
- Traverse Accessibility nodes  
- Persist user data  
- Perform logging  

**Public API:**

```kotlin
suspend fun cleanCache(): CleaningResult
```

### 8.2 CleaningResult Model

```kotlin
data class CleaningResult(
    val totalBytesFreed: Long,
    val appsCleaned: Int,
    val appsSkipped: Int
)
```

Simple by design. Extend only after v1.

---

## 9. Accessibility Layer (Execution Engine)

### 9.1 Accessibility Philosophy

Accessibility in Cachely is:

- Optional  
- User-initiated  
- Transparent  
- Narrowly scoped  
- Never persistent  

Accessibility is not a marketing feature.

### 9.2 AccessibilityHelper.kt

**Responsibilities:**

- Check if Accessibility service is enabled  
- Open system Accessibility settings  

**Must NOT:**

- Perform UI automation  
- Access Accessibility node trees  
- Perform cleaning logic  

### 9.3 CachelyAccessibilityService.kt

**Responsibilities:**

- Observe window changes  
- Detect App Info screen  
- Locate "Clear Cache" button  
- Execute click action  
- Navigate back safely  

**Hard constraints:**

- No screen reading  
- No content extraction  
- No data storage  
- No background execution  
- Abort on unexpected UI  

**Safety guards:**

- Per-app timeout  
- Retry limit  
- Immediate abort on user cancellation  

---

## 10. UI Layer (Jetpack Compose)

### 10.1 UI Rules

- State-driven UI  
- No business logic in composables  
- No blocking calls  
- Predictable recomposition  

### 10.2 HomeViewModel.kt

**Responsibilities:**

- Own Home screen state  
- Trigger cleaning  
- Handle loading and results  

**State model:**

```kotlin
data class HomeUiState(
    val isCleaning: Boolean = false,
    val lastCleaned: String? = null,
    val assistedEnabled: Boolean = false,
    val result: CleaningResult? = null
)
```

### 10.3 HomeScreen.kt

**Responsibilities:**

- Display primary CTA  
- Observe UI state  
- Show result summary  

**Must NOT:**

- Call system APIs  
- Perform cleaning  
- Handle permissions  

### 10.4 SettingsScreen.kt

**Responsibilities:**

- Toggle assisted mode  
- Navigate to permission explanation  
- Display transparency information  

### 10.5 PermissionScreen.kt

**Purpose:** Explain Accessibility usage before redirecting to system settings.

**Rules:**

- Calm language  
- Clear scope  
- Optional action  
- No coercion  

---

## 11. Navigation

### 11.1 NavGraph.kt

**Routes:**

- Home  
- Settings  
- Permission explanation  

**Constraints:**

- Simple navigation only  
- No deep links  
- No conditional flows  

---

## 12. Utility Layer

### 12.1 ByteFormatter.kt

- Bytes → KB / MB / GB  
- Locale-safe  
- Lightweight  

### 12.2 TimeFormatter.kt

- Relative time (e.g. "2 hours ago")  
- No timers or background work  

---

## 13. Threading & Concurrency

| Concern            | Rule                          |
|--------------------|--------------------------------|
| Cleaning           | Runs off main thread           |
| UI updates         | On main thread only            |
| Coroutines         | ViewModel-scoped only          |
| Global scopes      | None                           |

---

## 14. Error Handling Strategy

### 14.1 Error Categories

- Accessibility disabled  
- Permission denied  
- Timeout  
- OEM UI mismatch  
- User cancellation  

### 14.2 UX Handling

- Honest messages  
- No technical jargon  
- No automatic retries  

---

## 15. Performance Constraints

| Area           | Requirement   |
|----------------|---------------|
| App launch     | < 300 ms      |
| UI response    | Immediate     |
| Background work| None          |
| Battery usage  | Negligible    |
| APK size       | < 6 MB        |

---

## 16. Security & Privacy

- No data collection  
- No network calls  
- No analytics  
- No background tracking  

**Accessibility declaration must clearly state:**

- Purpose  
- Scope  
- User control  
- Ability to disable anytime  

---

## 17. Testing Strategy

### 17.1 Mandatory Manual Tests

- Accessibility ON / OFF  
- Permission denial  
- OEM variations  
- Low-end devices  

### 17.2 Optional Automated Tests

- ViewModel logic  
- Utility functions  

---

## 18. Release Readiness Checklist

- [ ] Accessibility explanation verified  
- [ ] Manual fallback works  
- [ ] No forced permissions  
- [ ] No crashes on denial  
- [ ] APK size validated  
- [ ] Play Store listing matches behavior  

---

## 19. Final Engineering Principles

- **Simplicity reduces bugs.**  
- **v1 must be boring, predictable, and trustworthy.**  
- **Good architecture stays out of the way.**
