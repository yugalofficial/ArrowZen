# Play Store Submission Checklist

This walks through what's needed to actually publish, beyond having a
signed `.aab` in hand.

## 1. Google Play Console account

A one-time $25 USD registration fee at https://play.google.com/console if
you don't already have a developer account.

## 2. Create the app listing

Play Console → **Create app** → fill in:
- App name: **ArrowZen: Logic Escape**
- Default language
- App or game: **Game**
- Free or paid: your choice (this project has no billing wired up — see
  docs/ARCHITECTURE.md's premium-architecture notes if you want to add
  in-app purchases later)

## 3. Store listing content

Use [PLAY_STORE_LISTING.md](PLAY_STORE_LISTING.md) for the short/full
description text. You still need to supply, yourself:

- **App icon**: already generated at `docs/store-assets/play_store_icon_512.png`
  (512×512, no alpha) — upload as-is.
- **Feature graphic** (1024×500 PNG/JPG): not included in this repo — this
  is a marketing banner, not an app resource, and needs original artwork
  sized specifically for that slot.
- **Screenshots**: minimum 2, recommended 4–8, phone screenshots at least
  320px on the short side. Section 57 of the original spec describes six
  suggested screenshot concepts (tagline, sequence-solving, daily puzzle,
  mastery, play-your-way, completion) — capture these from the actual
  running app rather than mockups, since Play policy requires screenshots
  to represent real app UI.
- **Privacy policy URL**: required for any app requesting `INTERNET`
  permission (this app does, for the optional AdMob path). Fill in
  `AppConstants.PRIVACY_POLICY_URL` and host an actual policy page — this
  project cannot generate a legally valid privacy policy for you.

## 4. Data Safety form

Play Console → your app → **Policy** → **App content** → **Data safety**.
Answer honestly based on your actual configuration:

- **If AdMob is disabled** (the default — see [ADMOB.md](ADMOB.md)): this
  app collects no personal data, requires no account, and makes no network
  calls beyond nothing at all. The form should reflect essentially no data
  collection.
- **If AdMob is enabled**: the Mobile Ads SDK collects advertising
  identifiers and device information as part of ad serving. Google's own
  Data Safety guidance for AdMob publishers at
  https://support.google.com/admob/answer/10787506 walks through exactly
  what to declare — this varies by your ad format choices and cannot be
  answered generically here.

## 5. Content rating questionnaire

Play Console → **Policy** → **App content** → **Content rating**. ArrowZen
is a puzzle game with no violence, no user-generated content, and no
real-money gambling mechanics — expect an "Everyone" rating, but you must
still complete the questionnaire yourself since Google requires the
publisher's own attestation.

## 6. App category & tags

Category: **Puzzle** (or **Casual**, if you prefer — both fit). Add
relevant tags matching the keywords in PLAY_STORE_LISTING.md.

## 7. Upload the AAB

Play Console → your app → **Production** (or **Internal testing** to try it
with a small group first, which is strongly recommended for a first
release) → **Create new release** → upload `app-release.aab` from either
the GitHub Actions artifact or your local `bundleRelease` output.

## 8. Review and rollout

Google's review typically takes a few hours to a few days for a new app.
Internal testing releases are near-instant. Once approved, you control the
rollout percentage from Play Console.

## Before you submit: production checklist

See [PRODUCTION_CHECKLIST.md](PRODUCTION_CHECKLIST.md) for the full
pre-submission checklist (code, performance, store assets).
