# ArrowZen Architecture

## Layering

```
Presentation (Compose UI, ViewModels)
        ↓
Domain / Game Logic (pure Kotlin models + engine)
        ↓
Data (Room, DataStore, repositories)
        ↓
Local Storage
```

The `game` package (`engine`, `generator`, `validator`, `hint`, `difficulty`)
has zero Android framework dependencies so it can be unit tested as plain
JVM code — see `app/src/test/java/.../game/engine/`.

## Package structure

```
com.yugalify.arrowzen
├── core
│   ├── constants   AppConstants  ✅ implemented
│   ├── di          AppContainer (manual DI)  ✅ implemented
│   ├── audio       SoundManager, HapticsManager  ✅ implemented
│   ├── ads         AdManager, AdConfig, NoOpAdManager  ✅ implemented
│   └── util        ShareUtil  ✅ implemented
├── data            Room database, DataStore, repositories  ✅ implemented
├── domain          model, repository interfaces, usecases  ✅ implemented
├── game
│   ├── engine      MoveValidator, GameEngine  ✅ implemented + tested
│   ├── generator   LevelCatalog (50 levels), PuzzleGenerator, DailyPuzzleProvider  ✅ implemented + tested
│   ├── validator   PuzzleSolver, PuzzleValidator  ✅ implemented + tested
│   ├── hint        HintEngine  ✅ implemented + tested
│   ├── rating      StarRatingCalculator  ✅ implemented + tested
│   └── difficulty  DifficultyCalculator  ✅ implemented + tested (recalibrated, see below)
├── feature         one package per screen/mode: home, game, levels, freeplay
│                   (shared by zen/daily/challenge/endless), onboarding,
│                   stats, achievements, settings, about  ✅ implemented
├── ui
│   ├── theme       ArrowZenTheme, light/dark/system  ✅ implemented
│   └── navigation  NavHost + routes  ✅ implemented (all screens wired)
└── MainActivity.kt ✅ implemented (onboarding gate + theme applied pre-first-paint)
```

Two Gradle-level source set variants exist outside the main tree for AdMob
(see docs/ADMOB.md): `app/src/noAdmob/java` (default, compiled in) and
`app/src/admob/java` (opt-in). Exactly one is ever included in a given
build.

## Phase roadmap

| Phase | Scope | Status |
|---|---|---|
| 1 | Foundation: Gradle, architecture, entry point, theme, nav, core models, engine + tests | **Done** |
| 2 | Full game engine: puzzle solver, difficulty calc, expanded unit tests | **Done** |
| 3 | Room database, DAOs, repositories, DataStore settings, migrations | **Done** |
| 4 | All Compose screens: onboarding, levels, level-complete, stats, achievements, settings, about, pause menu | **Done** |
| 5 | Daily Challenge, Zen/Challenge/Endless modes, deterministic generation, Smart Hint Engine, 50-level catalog | **Done** |
| 6 | Sound, haptics, animations, accessibility, share, AdMob abstraction | **Done** (AdMob opt-in, see ADMOB.md) |
| 7 | Full static-consistency audit (no real compiler available in the build sandbox) | **Done** — see note below |
| 8 | GitHub Actions workflow, all docs (BUILD/RELEASE/ADMOB/PLAY_STORE/PRODUCTION_CHECKLIST) | **Done** |

**On Phase 7**: "audit" here means every check possible without an actual
Android/Kotlin compiler — import resolution across every source file,
brace/paren balance, package/directory consistency, BuildConfig field and
gradle.properties key cross-referencing, and independent Python
re-verification of every puzzle-solving algorithm. It does not mean a real
compile has succeeded. That first happens in GitHub Actions.

## Why the puzzle solver is fast enough for budget devices

Arrows never move except to instantly leave the board, so a puzzle "state"
is fully described by *which arrow ids remain* — not by re-deriving grid
positions. `PuzzleSolver` searches over subsets of remaining arrows with a
memoized dead-state set, so it never re-explores a branch it already proved
unsolvable. This keeps solving fast even as board size grows, without
needing background threading for the level counts targeted in Section 24
(up to 7x7).

## Why no DI framework yet

Section 68 of the spec allows either Hilt or a manual container. Phase 1 has
exactly one ViewModel with one constructor argument, so a
`viewModelFactory { initializer { ... } }` block is used directly — adding
Hilt now would be premature. Hilt (or a manual `AppContainer`) should be
introduced once the dependency graph outgrows a hand-written container —
repositories now exist (Phase 3) and it's still small enough to read at a
glance in `AppContainer.kt`.

## Data layer (Phase 3)

- **Room** (`data/database`) owns structured gameplay records: per-level
  progress, player profile, daily challenge completions, achievements, and
  aggregate statistics. `exportSchema = true` with a documented
  no-destructive-fallback policy — see the doc comment on `ArrowZenDatabase`
  for why (Section 21: never erase progress on a schema bump).
- **DataStore Preferences** (`data/datastore/SettingsDataStore`) owns user
  toggles: theme, sound, music, haptics, reduced animations, high contrast,
  onboarding completion.
- **Repositories** (`data/repository`) implement the `domain/repository`
  interfaces and are the only classes that touch Room/DataStore directly —
  ViewModels and use cases never import `androidx.room` or
  `androidx.datastore` themselves.
- **`CompleteLevelUseCase`** is the single entry point for "a puzzle was
  just solved": it computes stars via `StarRatingCalculator`, persists the
  best record (never overwriting a better historical result — verified by
  `LevelRepositoryImplTest`), rolls statistics forward, and advances
  achievements.
- **`AppContainer`** wires all of the above as process-scoped singletons and
  is created once in `ArrowZenApplication`.

## Difficulty calibration data (Phase 4)

`DifficultyCalculator`'s thresholds were originally chosen without checking
real score distributions, and it showed: every `PuzzleGenerator` tier from
BEGINNER through EXPERT was scoring MEDIUM or above, because board-area
alone contributes several points even on a 4x4 board. Measured score ranges
across 300 generated boards per tier (mean shown):

| Generation tier | Mean score |
|---|---|
| BEGINNER | 12.2 |
| EASY | 14.1 |
| MEDIUM | 19.1 |
| HARD | 25.6 |
| EXPERT | 32.8 |

Thresholds were set at the midpoints between consecutive means
(13 / 17 / 22 / 29), which was verified (via an offline simulation of the
full 50-level catalog) to produce all five `Difficulty` values across the
real catalog rather than collapsing everything into MEDIUM–EXPERT.
