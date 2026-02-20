# Cachely – App Flow (single source of truth)

This document is the **canonical** description of app launch, navigation, screens, and user actions. Use it for implementation and UX consistency.

---

## App Launch

```
App Launch
└── MainActivity
    └── CachelyTheme
        └── Surface
            └── NavGraph
                └── startDestination: HOME
```

---

## Navigation & Scaffold

- **Scaffold**
  - **Bottom Navigation** (visible on Home & Settings only)
    - Home
    - Settings
  - **Content Area** → current route (HOME | SETTINGS | PERMISSION)

- **Routes**
  - `HOME` — default
  - `SETTINGS` — from bottom bar or back from Permission / Usage Access
  - `PERMISSION` — no bottom bar; from Home (“Clean selected”) or Settings (“Configure access”)
  - `USAGE_ACCESS` — no bottom bar; from Settings (“Configure usage access”)

---

## Route: HOME (Default)

**Screen:** `HomeScreen`

### On appear (silent, non-blocking)

- Check Accessibility permission status
- Scan installed apps (`AppScanner.scan`)

### Layout (top to bottom)

| Block | Content |
|-------|---------|
| **Header** | Title: **Cachely** · Subtitle: **Free up space. Carefully.** |
| **App list** | `LazyColumn` of `AppRow` (icon, name, cache size, selection toggle) |
| **Selection summary** | Shown only when ≥1 app selected: *“N apps selected · ~XXX MB reclaimable”* |
| **Primary action** | Button: **Clean selected apps** · Subtext: **You stay in control** |
| **Cleaning state** | Shown only while cleaning: progress chip (“Cleaning X of Y”), “Currently assisting: AppName”, optional **Cancel after current app** |
| **Result summary** | Post-clean: apps cleaned/skipped, approx space reclaimed, last cleaned timestamp |
| **Reassurance** | *“Your apps are lighter — nothing else was touched”* (after a clean) |
| **Footer** | *“No ads • No trackers”* |

### App list row (AppRow)

- App icon
- App name
- Cache size → **“~XXX MB reclaimable”** (or “Ready to clean” when size unknown/zero)
- Selection toggle (user-controlled)

### User actions

- Toggle app selection → update ViewModel
- Tap **Clean selected apps**
  - If Accessibility **not** granted → navigate to **PERMISSION**
  - If granted → start sequential assisted cleaning
- Bottom bar → Settings → navigate to **SETTINGS**

---

## Route: SETTINGS

**Screen:** `SettingsScreen`

### Layout

| Block | Content |
|-------|---------|
| **TopAppBar** | Back (←) · Title: **Settings** |
| **App Information** | App version · About Cachely |
| **Assisted cleaning** | Controlled by system (Accessibility). Row: **Configure access** → PERMISSION |
| **Transparency** | Cache size estimation · Usage access disclosure · Row: **Configure usage access** → USAGE_ACCESS |
| **Support & Legal** | Privacy Policy · Terms & Conditions · Rate app · Share app · Contact support |

### User actions

- Back (←) → **HOME**
- Tap **Configure access** → **PERMISSION**
- Tap **Configure usage access** → **USAGE_ACCESS**

---

## Route: PERMISSION (no bottom bar)

**Screen:** `PermissionScreen`

### Layout

| Block | Content |
|-------|---------|
| **Title** | Assisted cleaning |
| **Subtitle** | *“Only when you ask. Never in the background.”* |
| **Explanation** | **What Cachely does:** Opens app storage screens · Taps “Clear cache” like a human · Stops instantly if access is revoked. **What Cachely never does:** No background actions · No data or file deletion · No action without user intent. |
| **Primary action** | **Enable assisted cleaning** → opens system Accessibility settings |
| **Secondary action** | **Not now** → return without changes |

### Exit behavior

- On return from system settings → `popBackStack` → previous screen
- On **Not now** → `popBackStack` → previous screen

---

## Route: USAGE_ACCESS (no bottom bar)

**Screen:** `UsageAccessScreen`

**Purpose:** Explain why Usage Access is needed (to show app cache sizes) and open system Usage Access settings.

### Layout

| Block | Content |
|-------|---------|
| **Title** | Usage Access |
| **Subtitle** | *“So you can see how much cache each app is using.”* |
| **Explanation** | Why Cachely needs it (display sizes only); without it, sizes show as “Ready to clean”. No collection or sending of usage data; revoke anytime in device settings. |
| **Primary action** | **Open settings** → opens system Usage Access settings |
| **Secondary action** | **Not now** → return without changes |

### Exit behavior

- On **Open settings** or **Not now** → `popBackStack` → previous screen (Settings)

---

## Summary

- **One flow doc:** This file. For implementation details (APIs, threading) use [TDD.md](TDD.md); for visuals use [UI_UX.md](UI_UX.md).
- **Quality:** Calm, minimal, user in control. No coercion; permission and cleaning are explicit and reversible.
