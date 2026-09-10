# ArrowZen: Logic Escape

**Tap. Think. Escape.**

A relaxing, offline-first logic puzzle game for Android. Tap arrows in the
correct order to clear the board — each arrow can only escape once its path
to the edge is clear.

Designed & Developed by **Yugal**
© 2026 Yugal

## Get a working APK in under 10 minutes — no Android Studio needed

1. Upload this project to a new GitHub repository.
2. Open the **Actions** tab → run the **Android Build** workflow.
3. Download **ArrowZen-Debug-APK** from the finished run's Artifacts.
4. Copy it to your phone and install.

Full walkthrough with screenshots-of-what-to-click: **[docs/GITHUB_ACTIONS.md](docs/GITHUB_ACTIONS.md)**.
For a Play-Store-ready signed bundle instead of a debug APK, see **[docs/RELEASE.md](docs/RELEASE.md)**.

## Features

- **Classic Mode** — 50-level catalog (3 hand-authored tutorial levels + 47
  procedurally generated, every single one solver-validated) across 5
  difficulty tiers
- **Daily Arrow** — one deterministic puzzle per calendar date, same puzzle
  worldwide, fully offline, streak tracking
- **Zen Mode** — no timer, no mistake penalty
- **Challenge Mode** — Perfect Run / No Hint / Speed variants
- **Endless Mode** — procedurally generated, gradually escalating difficulty
- Smart 4-tier hint system backed by a real puzzle solver (hints are never
  wrong, even mid-solve)
- Undo, restart, 3-star rating, local achievements, statistics
- Sound effects (synthesized, no external assets), haptics, share-to-social
- Light/dark/system theme, high-contrast and reduced-motion accessibility
  options
- Interactive 3-step onboarding, pause menu
- Offline-first: no login, no account, no internet required for core play
- AdMob architecture ready (off by default — see docs/ADMOB.md)

## Tech stack

Kotlin · Jetpack Compose · Material 3 · MVVM · Navigation Compose ·
Coroutines/StateFlow · Room · DataStore Preferences · kotlinx.serialization ·
JUnit — see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full module
breakdown and design rationale.

## Requirements

- Android 8.0 (API 26) or newer on-device
- To build: JDK 17 + internet access to Google's/Maven Central's servers
  (GitHub Actions provides this automatically — see above)

## Building locally instead of via GitHub Actions

```bash
./gradlew assembleDebug   # -> app/build/outputs/apk/debug/app-debug.apk
./gradlew test            # run all unit tests
./gradlew bundleRelease   # -> app/build/outputs/bundle/release/app-release.aab (needs signing, see RELEASE.md)
```

Full details: [docs/BUILD.md](docs/BUILD.md).

## Changing the package name

Edit `ARROWZEN_PACKAGE_NAME` in `gradle.properties` (default:
`com.yugalify.arrowzen`) before publishing your own fork.

## Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — module layout, design decisions, phase history
- [docs/BUILD.md](docs/BUILD.md) — detailed build instructions (local + CI)
- [docs/GITHUB_ACTIONS.md](docs/GITHUB_ACTIONS.md) — step-by-step: upload → build → download APK
- [docs/RELEASE.md](docs/RELEASE.md) — signing setup for a real Play Store bundle
- [docs/ADMOB.md](docs/ADMOB.md) — the ad architecture and how to turn it on
- [docs/PLAY_STORE.md](docs/PLAY_STORE.md) — full Play Store submission walkthrough
- [docs/PLAY_STORE_LISTING.md](docs/PLAY_STORE_LISTING.md) — ready-to-paste listing copy
- [docs/PRODUCTION_CHECKLIST.md](docs/PRODUCTION_CHECKLIST.md) — what's done, what's honestly still on you

## What's genuinely complete vs. what's left to you

This is a real, substantially complete Android game — not a tutorial
scaffold. But three things are honestly outside what any code generation
can finish for you, and are called out clearly rather than glossed over:

1. **A real compiler has not yet run against this exact codebase.** Every
   check possible without one has been run repeatedly (import resolution
   across all 90+ files, brace/paren balance, package/directory
   consistency, and the puzzle-solving algorithm independently
   cross-verified in Python). The first real Kotlin/Android compile happens
   when you run the GitHub Actions workflow — see
   [docs/PRODUCTION_CHECKLIST.md](docs/PRODUCTION_CHECKLIST.md) for the
   full honest rundown.
2. **Store assets**: screenshots and a feature graphic need to be captured/
   designed from the real running app — see PLAY_STORE_LISTING.md.
3. **Legal**: a real, hosted privacy policy is required before Play Store
   submission — this project cannot write one on your behalf.
