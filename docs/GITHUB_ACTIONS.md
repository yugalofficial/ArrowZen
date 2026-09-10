# Building ArrowZen with GitHub Actions (step by step)

This is the "upload once, click a button, download the APK" path — no
Android Studio, no local Gradle, no local JDK required on your machine.

## 1. Create a GitHub repository

1. Go to https://github.com/new and create a new repository (public or
   private both work — private repos also get free GitHub Actions minutes
   for personal accounts, within GitHub's standard free-tier limits).
2. Do **not** initialize it with a README/gitignore/license — you're
   uploading a complete existing project.

## 2. Upload the project

**Option A — web upload (no git experience needed):**

1. Unzip the ArrowZen project on your computer.
2. On your new GitHub repo's page, click **"uploading an existing file"**.
3. Drag the *contents* of the unzipped `ArrowZen` folder in (not the folder
   itself — the `app/`, `gradle/`, `.github/` etc. folders should sit at the
   repository root, not nested one level down).
4. Commit directly to `master`.

   GitHub's web uploader can be slow/unreliable with many small files (this
   project has ~90). If it stalls or errors partway through, use Option B.

**Option B — git command line (more reliable for this many files):**

```bash
cd ArrowZen
git init
git add .
git commit -m "Initial commit"
git branch -M master
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git push -u origin master
```

## 3. Run the build

The workflow at `.github/workflows/android-build.yml` is already configured
to run automatically on every push to `master` — so simply completing step 2
starts the first build. To trigger it manually instead (or again later):

1. Open your repository on GitHub.
2. Click the **Actions** tab.
3. Click **"Android Build"** in the left sidebar.
4. Click **"Run workflow"** → **"Run workflow"** (green button).

## 4. Watch it build

Click into the running workflow. You'll see two jobs:

- **Build Debug APK & run tests** — always runs, always should succeed.
- **Build Release AAB** — only produces a signed bundle if you've added the
  signing secrets described in [RELEASE.md](RELEASE.md). Without them, this
  job runs and exits cleanly with a message explaining the AAB was skipped —
  it does **not** fail the workflow.

A full run typically takes 3–6 minutes.

## 5. Download your APK / AAB

1. Once the workflow run finishes (green checkmark), scroll to the bottom of
   that run's page to **Artifacts**.
2. You'll see:
   - **ArrowZen-Debug-APK** — always present. Download, unzip, and you have
     `app-debug.apk`, installable on any Android 8.0+ device (see step 6).
   - **ArrowZen-Release-AAB** — only present if signing secrets were
     configured. This `.aab` is the file the Play Store requires.

## 6. Install the debug APK on your phone

1. Download `ArrowZen-Debug-APK` and unzip it to get `app-debug.apk`.
2. Transfer it to your phone (email it to yourself, use a cloud drive, or a
   USB cable).
3. On your phone, open the file. Android will prompt to allow installing
   from this source the first time — allow it.
4. Tap Install.

This debug APK is fully playable — it's the same app, just unsigned for
store distribution and suffixed with `.debug` as its package id.

## 7. Get a real, signed release for the Play Store

The debug APK **cannot** be uploaded to the Play Store — Google requires a
consistently-signed release build. Follow [RELEASE.md](RELEASE.md) to:

1. Generate a signing keystore (one-time, on your own machine).
2. Add four secrets to your GitHub repository.
3. Re-run this same workflow — the Release AAB job will now produce a
   signed `.aab` ready to upload to Play Console.

## Troubleshooting

- **Debug job fails with a Gradle/dependency error**: almost always a
  transient network hiccup reaching Google's or Maven Central's servers —
  re-run the workflow (Actions → the failed run → "Re-run all jobs").
- **Release job says "skipping"**: expected until you've added the four
  signing secrets — see RELEASE.md. This is not a failure.
- **"Run workflow" button doesn't appear**: make sure you're looking at the
  **Actions** tab of *your* repository (not a fork's upstream), and that
  `.github/workflows/android-build.yml` was actually included in your
  upload — check the repo's file list.
