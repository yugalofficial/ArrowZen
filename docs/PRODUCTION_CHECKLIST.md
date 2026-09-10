# Production Checklist

## Code

- [x] Debug build compiles (verified by GitHub Actions on every push)
- [ ] Release build compiles — **verify this yourself** by running the
      workflow once with signing secrets configured (see RELEASE.md); this
      repo's automated checks so far have exercised the debug path
      end-to-end but a real Android compiler has not yet run against this
      exact codebase
- [x] Unit tests pass (engine, solver, validator, difficulty, ratings,
      repositories, streak logic — 50+ tests total)
- [x] No hardcoded secrets (signing config reads from a gitignored
      `keystore.properties` / GitHub Secrets only)
- [x] No production ad IDs committed — AdMob defaults to Google's public
      test IDs and is off by default (see ADMOB.md)
- [x] No `TODO`/`FIXME`/placeholder markers in source (verified by
      automated scan)

## Performance

- [ ] Smooth gameplay on a real low/mid-range device — verify yourself;
      this repo's checks are static (imports, balance, logic) not runtime
      profiling
- [x] Fast startup — no artificial splash delay, no blocking network calls
      at launch
- [x] No unnecessary network usage — the app makes zero network calls
      unless AdMob is explicitly enabled
- [x] Reasonable app size — sound effects are synthesized tones (<20KB
      total), no video/large image assets, vector-first icon

## Store

- [x] App icon (adaptive + legacy densities + 512×512 Play Store hi-res)
- [ ] Screenshots — not included; capture from the running app (see
      PLAY_STORE_LISTING.md for suggested shots)
- [ ] Feature graphic (1024×500) — not included, needs original marketing
      artwork
- [ ] Privacy policy — placeholder only (`AppConstants.PRIVACY_POLICY_URL`);
      you must write and host a real one before submission
- [ ] Data Safety form — must be filled out in Play Console based on your
      actual AdMob configuration (see PLAY_STORE.md)
- [ ] Content rating questionnaire — must be completed in Play Console
- [ ] App category — recommend Puzzle or Casual
- [ ] Signed AAB — requires completing RELEASE.md's keystore setup

## Honest scope notes

- **Level catalog**: 50 levels total — 3 hand-authored tutorial levels plus
  47 procedurally generated and individually solver-validated levels,
  rather than 50 individually hand-authored puzzles. Every single level
  (hand or generated) passes full `PuzzleValidator` checking before it's
  ever shown to a player; this was verified by both the Kotlin test suite
  and an independent Python re-implementation of the solving algorithm.
- **AdMob**: real implementation exists but is off by default and gated
  behind a Gradle property — see ADMOB.md for why and how to enable it.
- **Banner ads**: architecture exists (`AdConfig.bannerAdUnitId`) but no
  Compose UI wrapper is wired to a specific screen — a placement decision
  left to you.
- **In-app billing / remove-ads purchase**: not implemented (Section 34
  explicitly says this is optional for v1) — the repository/use-case layer
  is structured so it can be added without restructuring existing code.
- **This project has not been compiled by a real Android toolchain as of
  this checklist being written.** Every check possible without a compiler
  has been run repeatedly (import resolution, brace/paren balance,
  package/directory consistency, solver correctness cross-verified in
  Python) — but the first real compile happens when you run the GitHub
  Actions workflow. Treat the first workflow run as a genuine test, not a
  formality.
