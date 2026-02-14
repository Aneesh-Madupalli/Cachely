# Cachely v1 – Release Readiness Checklist

From TDD §18. Complete before submitting to Play Store.

- [ ] **Accessibility explanation verified** – Permission screen text is calm, clear, and matches store declaration.
- [ ] **Manual fallback works** – With Accessibility off, "Clean cache" opens the system app list; no crash.
- [ ] **No forced permissions** – User can use the app and clean (manually) without enabling Accessibility.
- [ ] **No crashes on denial** – Disabling Accessibility or denying does not crash the app.
- [ ] **APK size validated** – Release APK < 6 MB.
- [ ] **Play Store listing matches behavior** – No claims of automatic/background cleaning or system optimization.
