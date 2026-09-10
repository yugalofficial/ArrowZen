# AdMob Configuration

ArrowZen ships with a complete AdMob architecture (Section 33/70 of the
original spec), but it is **off by default**. This document explains why,
and how to turn it on.

## Why off by default

This project builds via GitHub Actions in an environment where the exact
current AdMob SDK version and its API surface can't be independently
verified ahead of time. Rather than risk the entire build on a third-party
dependency, the architecture is split into two Gradle source sets:

- `app/src/noAdmob/java` — the default. Provides `NoOpAdManager`, which
  reports every ad as unavailable. Zero third-party ad dependency is
  compiled in.
- `app/src/admob/java` — the real thing. `GoogleMobileAdsManager`, backed by
  `com.google.android.gms:play-services-ads`. Only compiled in when you
  explicitly opt in below.

Exactly one of these is included in any given build — controlled by a
single Gradle property. The app is 100% functional either way; ads are
purely additive monetization, never required for gameplay (Section 33: "the
game must feel enjoyable even without spending money").

## Turning AdMob on

1. Create an AdMob account and app at https://apps.admob.com if you haven't
   already, and create ad units for Rewarded and (optionally) Interstitial
   and Banner formats.

2. Edit `gradle.properties`:

   ```properties
   ARROWZEN_ENABLE_ADMOB=true
   ARROWZEN_ADS_ENABLED=true
   ARROWZEN_ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
   ARROWZEN_ADMOB_REWARDED_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
   ARROWZEN_ADMOB_INTERSTITIAL_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
   ARROWZEN_ADMOB_BANNER_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
   ```

   Leave any unit ID blank if you don't intend to use that ad format yet —
   `AdConfig` falls back to Google's official public test IDs for anything
   left blank, so the app never ships production traffic against an empty
   ad unit by accident.

3. Rebuild. `com.google.android.gms:play-services-ads` is now pulled in,
   `GoogleMobileAdsManager` is compiled, and the manifest's
   `com.google.android.gms.ads.APPLICATION_ID` meta-data tag (already
   present, previously inert) becomes live.

4. **If the specified SDK version fails to resolve or a compile error
   surfaces** (SDK API surfaces do shift over major versions), open
   `app/build.gradle.kts`, find the line:

   ```kotlin
   implementation("com.google.android.gms:play-services-ads:23.6.0")
   ```

   and update the version number to whatever is current at
   https://developers.google.com/admob/android/quick-start — the calling
   code in `GoogleMobileAdsManager.kt` uses the `RewardedAd.load` /
   `InterstitialAd.load` static-loader pattern, which has been stable across
   many SDK versions, so a version bump alone should not require code
   changes.

## What's implemented

- **Rewarded ads** (prioritized per spec): `AdManager.showRewardedAd(...)`.
  Wired into the Game screen as a "Watch Ad for Free Hint" button — visible
  only when `AdConfig.isAdsEnabled` is true, so it never appears as a dead
  button when ads are off.
- **Interstitial ads**: `AdManager.maybeShowInterstitial(...)`, with a
  minimum-levels-between-shows frequency cap
  (`AdConfig.INTERSTITIAL_MIN_LEVELS_BETWEEN`, default 4) — not wired to a
  specific trigger point in the UI yet; call it from wherever you want
  interstitials to appear (e.g. after returning to the level list), keeping
  Section 33's rules in mind (never on startup, never during onboarding,
  never during the first few levels, never mid-puzzle).
- **Banner ads**: `AdConfig.bannerAdUnitId` is defined and ready, but no
  Compose `AndroidView` wrapper is wired into a specific screen yet — this
  is the one piece left as an extension point rather than a finished UI,
  since banner placement is a product/design decision (which screens, which
  position) rather than a technical one.

## Data Safety declaration

If you enable ads, your Play Console **Data Safety** section and privacy
policy must accurately reflect what the Mobile Ads SDK actually collects
and shares (advertising ID, etc.) in your specific configuration. This
project cannot fill that out for you — see [PLAY_STORE.md](PLAY_STORE.md).
