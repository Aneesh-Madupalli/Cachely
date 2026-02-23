 # Cachely Docs – Start Here
 
 ## 1. What Cachely Is
 
 - **Product:** Minimal, premium Android cache cleaner.
 - **Core action:** User selects apps → Cachely helps clear their cache through system screens.
 - **Guardrails:** No ads, no analytics, no background work, no fake “boosting”.
 
 If you only read one document, **this is enough to understand the app and know where to look next.**
 
 ---
 
 ## 2. How the App Is Structured
 
 - **Language & stack:** Kotlin, Jetpack Compose, lean MVVM.
 - **Key packages:**
   - `accessibility` – optional Accessibility-based engine that presses “Clear cache” for the user on App Info screens.
   - `data` – scanning apps, orchestrating cleaning, progress and result models.
   - `ui` – composables, screens, and ViewModel state only (no system APIs here).
   - `navigation` – simple NavGraph with a small set of routes.
   - `util` – formatting utilities (bytes, time).
 - **Rule:** UI never knows how cleaning happens, only the result and current progress.
 
 ---
 
 ## 3. Main User Flows
 
 - **Launch**
   - App launches into `MainActivity` → `CachelyTheme` → `NavGraph` (start: Home).
 - **Home**
   - Scans installed apps (silently, non-blocking).
   - Shows list of apps with approximate cache size and a selection toggle.
   - User selects apps and taps **“Clean selected apps”**.
 - **Permission gate**
   - If Accessibility service is not enabled, user is taken to a calm explanation screen, then to system Accessibility settings if they choose.
   - If user declines, app falls back to manual cleaning expectations.
 - **Cleaning**
   - For each selected app, Cachely opens App Info and (optionally) assists pressing “Clear cache”, then returns.
   - Progress is reported to the UI (current app, index, total).
 - **Result**
   - UI shows apps cleaned/skipped, estimated bytes freed, and last cleaned time.
 
 Everything is **user-initiated, transparent, and reversible.**
 
 ---
 
 ## 4. Files You Actually Need (Most of the Time)
 
 - **High-level context & map**
   - `docs/context/CONTEXT.md` – deeper project overview, package map, and “where to change what”.
 - **Implementation details**
   - `docs/TDD.md` – technical design: contracts, flows, threading, testing strategy.
 - **Product requirements**
   - `docs/PRD.md` – scope, constraints, and what v1 must / must not do.
 - **UI & motion**
   - `docs/UI_UX.md` – colors, typography, motion rules, and screen-level UX guidance.
 
 For day-to-day coding, **start with this `README.md` and dip into the four docs above only when you need more detail.**
 
 ---
 
 ## 5. Less-Common but Still Useful Docs
 
 - `docs/context/CODEBASE.md` – quick table of “path → purpose → do not”; use when you forget what a file is for.
 - `docs/APP_FLOW.md` – verbose app-flow breakdown (navigation, screens, copy).
 - `docs/RELEASE_CHECKLIST.md` – pre–Play Store submission checks.
- `docs/KEYSTORE_SETUP.md` – how to set up signing for release builds.
 
 You can safely ignore these until you are:
 - Polishing navigation or wording (`APP_FLOW.md`).
 - Preparing a store release (`RELEASE_CHECKLIST.md`, `KEYSTORE_SETUP.md`).
- Auditing or refactoring architecture (`CODEBASE.md`).
 
 ---
 
 ## 6. How to Extend v1 Safely
 
 When you add or change behavior:
 - **Stay within the existing packages** (`accessibility`, `data`, `ui`, `navigation`, `util`).
 - **Keep UI declarative and calm:** no business logic in composables, no blocking work on the main thread.
 - **Respect guardrails:** no background cleaning, no data collection, no extra “optimizer” features.
 - **Prefer clarity over cleverness:** small, well-named functions and data classes.
 
 If you are unsure where something should live, check `docs/context/CONTEXT.md`’s “Where to change what” section, then come back here to keep the big picture in mind.
 
