# 📘 Cachely – Technical Design Document (TDD)
## Version: v1 (Flow-Based Implementation)

**Product Name:** Cachely  
**Platform:** Android  
**Language:** Kotlin  
**UI Framework:** Jetpack Compose  
**Architecture:** Lean MVVM + Accessibility Automation Engine  
**Author Perspective:** Senior Android / Kotlin Engineer (30+ Years Experience)  
**Status:** FINAL – Flow-Locked for Development  

---

## 1. Purpose of This Document

This Technical Design Document defines the **complete technical implementation** of Cachely v1 **based strictly on the approved project flow tree**.

This document:
- Converts the functional flow into engineering components
- Defines responsibilities per module
- Establishes safety, compliance, and failure handling
- Acts as the **single source of truth** for development

No feature, behavior, or structure outside this document should be implemented in v1.

---

## 2. High-Level System Overview

Cachely is a **user-initiated cache cleaning application** that uses:
- App metadata scanning
- User selection
- Optional Accessibility-based automation
- Sequential, observable, cancellable cleaning

At no point does Cachely:
- Run in background
- Perform silent actions
- Execute without explicit user intent

---

## 3. Application Launch & Root Flow

### 3.1 App Launch Sequence

```
App Launch
↓
Root Screen
↓
Silent Checks
```

### 3.2 Silent Checks (Non-Blocking)

Performed **once at app startup**, without UI interruption.

#### Checks
- Accessibility permission status
- Installed apps availability

#### Rules
- No dialogs
- No blocking UI
- No permission requests

#### Outcome
- Results stored in ViewModel state
- UI adapts passively

---

## 4. Root Screen Architecture

### 4.1 Root Screen Composition

```
Root Screen
├── Bottom Navigation
│   ├── Home
│   └── Settings
```

#### Navigation Rules
- Exactly two destinations
- No nested navigation graphs
- No dynamic tabs
- No deep links in v1

---

## 5. Home Screen – Core Functional Engine

The Home Screen is the **heart of Cachely**.

---

## 6. App Scan Module (Home Screen)

### 6.1 Fetch Installed Apps

**Source**
- Android `PackageManager`

**Data Collected**
- App name
- Package name
- App icon
- Approximate cache size

**Rules**
- Read-only access
- No private data
- No file system traversal

---

### 6.2 Read App Metadata

For each installed app:

```kotlin
data class AppCacheItem(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val approxCacheBytes: Long,
    val isSystemApp: Boolean
)
```

---

### 6.3 Optional Filtering

Filters are **passive and non-destructive**.

#### Available Filters
- Exclude system apps
- Exclude zero-cache apps

Filters:
- Do not alter underlying data
- Only affect UI rendering

---

### 6.4 Sorting Logic

Default sort:

```
Cache Size → Descending (Large → Small)
```

Sorting must be:
- Stable
- Deterministic
- Performed off main thread

---

## 7. App List UI

### 7.1 App Row Structure

Each row displays:
- App icon
- App name
- Cache size (formatted)
- Selection toggle

No secondary actions allowed per row.

---

### 7.2 Selection State Management

Selection is:
- Explicit
- User-controlled
- Stored in ViewModel

Rules:
- No auto-selection
- No "Select All" in v1 (to prevent accidental actions)

---

## 8. User Action – Clean Selected

### 8.1 Clean Entry Point

User taps:

```
Clean Selected
```

This is the **only trigger** for cleaning.

---

## 9. Permission Gate

### 9.1 Accessibility Permission Check

#### Case 1: Permission Granted
→ Start cleaning automation

#### Case 2: Permission Not Granted
→ Show Permission Explanation Screen
→ Redirect to System Accessibility Settings

---

### 9.2 Permission Explanation Screen Rules

- Custom UI (premium)
- Calm language
- No forced action
- Two choices only:
  - Enable assisted cleaning
  - Cancel / Go back

---

## 10. Cleaning Automation Engine (Accessibility)

### 10.1 Execution Strategy

Cleaning is **strictly sequential**.

Parallel execution is **forbidden**.

---

### 10.2 Per-App Cleaning Flow

For each selected app:

```
Open App Info Screen
   ↓
Navigate to Storage / Storage & Cache
   ↓
Locate "Clear Cache" Button
   ↓
Click "Clear Cache"
   ↓
Observe UI Change
   ↓
Navigate Back
   ↓
Move to Next App
```

---

### 10.3 UI Observation Signals

Cleaning success is detected via:
- Cache size becomes 0
- Clear Cache button disabled
- UI refresh detected

At least **one signal must be observed**.

---

## 11. Failure Handling (Critical for Safety)

### 11.1 Failure Types

#### Button Not Found
→ Skip app
→ Log reason internally

#### Timeout Exceeded
→ Skip app
→ Continue next

#### Permission Revoked
→ Immediately stop process
→ Notify user

---

### 11.2 Timeout Rules

- Per app timeout: fixed (e.g., 5–7 seconds)
- No infinite waits
- No retries without user action

---

## 12. Progress Tracking

### 12.1 Progress Model

```kotlin
data class CleaningProgress(
    val currentIndex: Int,
    val totalApps: Int,
    val currentAppName: String
)
```

Displayed as:

```
Cleaning 12 / 50
Currently: WhatsApp
```

---

### 12.2 Cancel Support (Optional)

- User-initiated cancel only
- Graceful stop after current app
- No partial automation loops

---

## 13. Result Summary

### 13.1 Summary Data

Displayed after completion:
- Apps attempted count
- Apps successfully cleaned count
- Apps skipped count
- Approx total cache cleared

---

### 13.2 Honesty Rule

Results must:
- Reflect actual actions
- Avoid exaggeration
- Clearly explain skips

No fake numbers allowed.

---

## 14. Settings Screen

### 14.1 App Information

- App version
- About Cachely

---

### 14.2 Legal

- Privacy Policy
- Terms & Conditions

---

### 14.3 User Actions

- Rate App
- Share App
- Contact Support

All actions must:
- Open system intents
- Never redirect silently

---

## 15. Compliance & Safety Layer (NON-NEGOTIABLE)

### 15.1 Accessibility Compliance

- Usage fully explained
- User-initiated only
- Can be disabled anytime

---

### 15.2 Strict Safety Rules

Cachely must never:
- Run background automation
- Clear cache silently
- Act without user intent
- Misreport results

---

## 16. Threading & Performance

- Scanning → background thread
- Cleaning → background thread
- UI updates → main thread
- No global coroutine scopes
- No background services

---

## 17. Error Handling UX

- Human-readable messages
- No stack traces
- No technical jargon
- No forced retries

---

## 18. Testing Strategy

### Mandatory Manual Tests

- 10+ apps selected
- 50+ apps selected
- Permission revoked mid-process
- OEM variations
- Low-end devices

### Optional Automated Tests

- ViewModel logic
- Selection state handling
- Formatter utilities

---

## 19. Release Readiness Checklist

- Accessibility explanation verified
- Sequential processing confirmed
- Failure handling tested
- No background behavior
- Store listing matches behavior

---

## 20. Final Engineering Principle

> **If the system cannot clearly explain what it is doing, it must not do it.**

Cachely v1 prioritizes:
- Trust
- Predictability
- Safety
- Transparency

---

## End of Document
